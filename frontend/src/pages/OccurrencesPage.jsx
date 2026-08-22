import { FilterX, Plus, Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import OccurrenceCard from '../components/occurrences/OccurrenceCard'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import occurrenceStatusConfig from '../constants/occurrenceStatus'
import {
  occurrenceCategoryLabels,
  occurrenceTypeLabels,
  occurrenceTypesByCategory,
} from '../constants/occurrencePresentation'
import useAuth from '../hooks/useAuth'
import {
  getMyOccurrences,
  getOccurrences,
} from '../services/occurrenceService'
import './Occurrences.css'

const emptyFilters = {
  category: '',
  type: '',
  status: '',
  neighborhood: '',
}

function OccurrencesPage() {
  const { logout, token } = useAuth()
  const [mode, setMode] = useState('all')
  const [filters, setFilters] = useState(emptyFilters)
  const [appliedFilters, setAppliedFilters] = useState(emptyFilters)
  const [occurrences, setOccurrences] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const availableTypes = filters.category
    ? occurrenceTypesByCategory[filters.category]
    : Object.keys(occurrenceTypeLabels)

  useEffect(() => {
    let isCurrent = true

    async function loadOccurrences() {
      try {
        const data =
          mode === 'mine'
            ? await getMyOccurrences(token)
            : await getOccurrences(token, appliedFilters)

        if (isCurrent) {
          setOccurrences(data)
        }
      } catch (requestError) {
        if (!isCurrent) return

        if (requestError.status === 401) {
          logout()
          return
        }

        setError(requestError.message)
      } finally {
        if (isCurrent) setIsLoading(false)
      }
    }

    loadOccurrences()
    return () => {
      isCurrent = false
    }
  }, [appliedFilters, logout, mode, reloadKey, token])

  function handleFilterChange(event) {
    const { name, value } = event.target
    setFilters((currentFilters) => {
      const shouldResetType =
        name === 'category' &&
        value &&
        currentFilters.type &&
        !occurrenceTypesByCategory[value].includes(currentFilters.type)

      return {
        ...currentFilters,
        [name]: value,
        ...(shouldResetType ? { type: '' } : {}),
      }
    })
  }

  function handleFilterSubmit(event) {
    event.preventDefault()
    setError('')
    setIsLoading(true)
    setAppliedFilters({ ...filters })
  }

  function clearFilters() {
    setFilters({ ...emptyFilters })
    setAppliedFilters({ ...emptyFilters })
    setError('')
    setIsLoading(true)
  }

  function changeMode(newMode) {
    if (newMode === mode) return

    setMode(newMode)
    setError('')
    setIsLoading(true)
  }

  function retry() {
    setError('')
    setIsLoading(true)
    setReloadKey((currentKey) => currentKey + 1)
  }

  return (
    <section className="occurrences-page">
      <header className="occurrences-page__heading">
        <div>
          <span>Ocorrências</span>
          <h1>Acompanhe a cidade</h1>
          <p>Consulte os registros mais recentes ou somente os seus.</p>
        </div>
        <Link className="button button--primary" to="/app/ocorrencias/nova">
          <Plus size={18} aria-hidden="true" />
          Nova ocorrência
        </Link>
      </header>

      <div className="occurrence-tabs" role="tablist" aria-label="Tipo de listagem">
        <button
          aria-selected={mode === 'all'}
          className={mode === 'all' ? 'active' : ''}
          onClick={() => changeMode('all')}
          role="tab"
          type="button"
        >
          Todas
        </button>
        <button
          aria-selected={mode === 'mine'}
          className={mode === 'mine' ? 'active' : ''}
          onClick={() => changeMode('mine')}
          role="tab"
          type="button"
        >
          Minhas ocorrências
        </button>
      </div>

      {mode === 'all' && (
        <form className="occurrence-filters" onSubmit={handleFilterSubmit}>
          <div className="form-field">
            <label htmlFor="filter-category">Categoria</label>
            <select
              className="form-control"
              id="filter-category"
              name="category"
              onChange={handleFilterChange}
              value={filters.category}
            >
              <option value="">Todas</option>
              {Object.entries(occurrenceCategoryLabels).map(([value, label]) => (
                <option key={value} value={value}>{label}</option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="filter-type">Tipo</label>
            <select
              className="form-control"
              id="filter-type"
              name="type"
              onChange={handleFilterChange}
              value={filters.type}
            >
              <option value="">Todos</option>
              {availableTypes.map((value) => (
                <option key={value} value={value}>{occurrenceTypeLabels[value]}</option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="filter-status">Status</label>
            <select
              className="form-control"
              id="filter-status"
              name="status"
              onChange={handleFilterChange}
              value={filters.status}
            >
              <option value="">Todos</option>
              {Object.entries(occurrenceStatusConfig).map(([value, config]) => (
                <option key={value} value={value}>{config.label}</option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="filter-neighborhood">Bairro</label>
            <input
              className="form-control"
              id="filter-neighborhood"
              maxLength={100}
              name="neighborhood"
              onChange={handleFilterChange}
              placeholder="Ex.: Guilhermina"
              value={filters.neighborhood}
            />
          </div>

          <div className="occurrence-filters__actions">
            <Button type="submit">
              <Search size={17} aria-hidden="true" />
              Filtrar
            </Button>
            <Button onClick={clearFilters} variant="outline">
              <FilterX size={17} aria-hidden="true" />
              Limpar
            </Button>
          </div>
        </form>
      )}

      {isLoading ? (
        <FeedbackState type="loading" message="Buscando ocorrências..." />
      ) : error ? (
        <div className="occurrences-error">
          <FeedbackState type="error" message={error} />
          <Button onClick={retry} variant="outline">Tentar novamente</Button>
        </div>
      ) : occurrences.length === 0 ? (
        <FeedbackState
          type="empty"
          title={mode === 'mine' ? 'Você ainda não registrou ocorrências' : undefined}
        />
      ) : (
        <div className="occurrences-list">
          {occurrences.map((occurrence) => (
            <OccurrenceCard
              occurrence={occurrence}
              key={occurrence.id}
              to={`/app/ocorrencias/${occurrence.id}`}
            />
          ))}
        </div>
      )}
    </section>
  )
}

export default OccurrencesPage
