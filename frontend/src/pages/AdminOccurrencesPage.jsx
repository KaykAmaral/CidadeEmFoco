import { Eye, FilterX, Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import StatusBadge from '../components/ui/StatusBadge'
import occurrenceStatusConfig from '../constants/occurrenceStatus'
import {
  getOccurrenceCategoryLabel,
  getOccurrenceTypeLabel,
  occurrenceCategoryLabels,
  occurrenceTypeLabels,
  occurrenceTypesByCategory,
} from '../constants/occurrencePresentation'
import useAuth from '../hooks/useAuth'
import { getAdminOccurrences } from '../services/adminService'
import { formatDateTime } from '../utils/date'
import './AdminOccurrencesPage.css'

const emptyFilters = {
  category: '',
  type: '',
  status: '',
  neighborhood: '',
}

function AdminOccurrencesPage() {
  const { logout, token } = useAuth()
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

    getAdminOccurrences(token, appliedFilters)
      .then((data) => {
        if (isCurrent) setOccurrences(data)
      })
      .catch((requestError) => {
        if (!isCurrent) return
        if (requestError.status === 401) {
          logout()
          return
        }
        setError(requestError.message)
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false)
      })

    return () => {
      isCurrent = false
    }
  }, [appliedFilters, logout, reloadKey, token])

  function handleChange(event) {
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

  function applyFilters(event) {
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

  function retry() {
    setError('')
    setIsLoading(true)
    setReloadKey((currentKey) => currentKey + 1)
  }

  return (
    <section className="admin-occurrences-page">
      <header className="admin-page-heading">
        <div>
          <span>Administração</span>
          <h1>Gerenciar ocorrências</h1>
          <p>Consulte os registros e abra os detalhes para atualizar o status.</p>
        </div>
        {!isLoading && !error && (
          <span className="admin-page-count">
            {occurrences.length} {occurrences.length === 1 ? 'resultado' : 'resultados'}
          </span>
        )}
      </header>

      <form className="admin-occurrence-filters" onSubmit={applyFilters}>
        <div className="form-field">
          <label htmlFor="admin-filter-category">Categoria</label>
          <select className="form-control" id="admin-filter-category" name="category" onChange={handleChange} value={filters.category}>
            <option value="">Todas</option>
            {Object.entries(occurrenceCategoryLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </div>
        <div className="form-field">
          <label htmlFor="admin-filter-type">Tipo</label>
          <select className="form-control" id="admin-filter-type" name="type" onChange={handleChange} value={filters.type}>
            <option value="">Todos</option>
            {availableTypes.map((value) => <option key={value} value={value}>{occurrenceTypeLabels[value]}</option>)}
          </select>
        </div>
        <div className="form-field">
          <label htmlFor="admin-filter-status">Status</label>
          <select className="form-control" id="admin-filter-status" name="status" onChange={handleChange} value={filters.status}>
            <option value="">Todos</option>
            {Object.entries(occurrenceStatusConfig).map(([value, config]) => <option key={value} value={value}>{config.label}</option>)}
          </select>
        </div>
        <div className="form-field">
          <label htmlFor="admin-filter-neighborhood">Bairro</label>
          <input className="form-control" id="admin-filter-neighborhood" maxLength={100} name="neighborhood" onChange={handleChange} placeholder="Ex.: Boqueirão" value={filters.neighborhood} />
        </div>
        <div className="admin-occurrence-filters__actions">
          <Button type="submit"><Search size={17} aria-hidden="true" />Filtrar</Button>
          <Button onClick={clearFilters} variant="outline"><FilterX size={17} aria-hidden="true" />Limpar</Button>
        </div>
      </form>

      {isLoading ? (
        <FeedbackState type="loading" message="Buscando ocorrências..." />
      ) : error ? (
        <div className="admin-page-error">
          <FeedbackState type="error" message={error} />
          <Button onClick={retry} variant="outline">Tentar novamente</Button>
        </div>
      ) : occurrences.length === 0 ? (
        <FeedbackState type="empty" title="Nenhuma ocorrência encontrada" />
      ) : (
        <div className="admin-occurrences-table-wrapper">
          <table className="admin-occurrences-table">
            <thead>
              <tr><th>ID</th><th>Ocorrência</th><th>Local</th><th>Data</th><th>Status</th><th><span className="visually-hidden">Ações</span></th></tr>
            </thead>
            <tbody>
              {occurrences.map((occurrence) => (
                <tr key={occurrence.id}>
                  <td data-label="ID">#{occurrence.id}</td>
                  <td data-label="Ocorrência"><strong>{getOccurrenceTypeLabel(occurrence.type)}</strong><span>{getOccurrenceCategoryLabel(occurrence.category)}</span></td>
                  <td data-label="Local">{occurrence.address || occurrence.neighborhood || 'Somente coordenadas'}</td>
                  <td data-label="Data">{formatDateTime(occurrence.createdAt)}</td>
                  <td data-label="Status"><StatusBadge status={occurrence.status} /></td>
                  <td><Link aria-label={`Analisar ocorrência ${occurrence.id}`} to={`/admin/ocorrencias/${occurrence.id}`}><Eye size={18} aria-hidden="true" /><span>Analisar</span></Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}

export default AdminOccurrencesPage
