import {
  Bell,
  CheckCircle2,
  ClipboardList,
  Clock3,
  FilterX,
  RotateCw,
  Search,
  SearchCheck,
  SlidersHorizontal,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import OccurrenceMap from '../components/map/OccurrenceMap'
import OccurrenceCard from '../components/occurrences/OccurrenceCard'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import occurrenceStatusConfig from '../constants/occurrenceStatus'
import {
  occurrenceCategoryLabels,
  occurrenceTypeLabels,
  occurrenceTypesByCategory,
} from '../constants/occurrencePresentation'
import {
  getAlertSeverityLabel,
  getAlertTypeLabel,
} from '../constants/alertPresentation'
import useAuth from '../hooks/useAuth'
import { getAdminAlerts, getAdminOccurrences } from '../services/adminService'
import { formatDateTime } from '../utils/date'
import './AdminDashboardPage.css'

const metricConfig = [
  { key: 'total', label: 'Total de ocorrências', icon: ClipboardList, tone: 'blue' },
  { key: 'REGISTRADA', label: 'Registradas', icon: Clock3, tone: 'blue' },
  { key: 'EM_ATENDIMENTO', label: 'Em atendimento', icon: SearchCheck, tone: 'orange' },
  { key: 'RESOLVIDA', label: 'Resolvidas', icon: CheckCircle2, tone: 'green' },
]

const emptyFilters = {
  period: '30',
  startDate: '',
  endDate: '',
  category: '',
  type: '',
  status: '',
  neighborhood: '',
}

function buildApiFilters(filters) {
  const apiFilters = {
    category: filters.category,
    type: filters.type,
    status: filters.status,
    neighborhood: filters.neighborhood.trim(),
  }

  if (filters.period === 'CUSTOM') {
    apiFilters.createdFrom = new Date(`${filters.startDate}T00:00:00`).toISOString()
    apiFilters.createdTo = new Date(`${filters.endDate}T23:59:59.999`).toISOString()
  } else if (filters.period !== 'ALL') {
    const createdFrom = new Date()
    createdFrom.setHours(0, 0, 0, 0)
    createdFrom.setDate(createdFrom.getDate() - (Number(filters.period) - 1))
    apiFilters.createdFrom = createdFrom.toISOString()
    apiFilters.createdTo = new Date().toISOString()
  }

  return apiFilters
}

function AdminDashboardPage() {
  const { logout, token, user } = useAuth()
  const [filters, setFilters] = useState(emptyFilters)
  const [appliedFilters, setAppliedFilters] = useState(() => buildApiFilters(emptyFilters))
  const [occurrences, setOccurrences] = useState([])
  const [alerts, setAlerts] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [filterError, setFilterError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const availableTypes = filters.category
    ? occurrenceTypesByCategory[filters.category]
    : Object.keys(occurrenceTypeLabels)

  useEffect(() => {
    let isCurrent = true

    async function loadDashboard() {
      try {
        const [occurrenceList, alertList] = await Promise.all([
          getAdminOccurrences(token, appliedFilters),
          getAdminAlerts(token),
        ])

        if (isCurrent) {
          setOccurrences(occurrenceList)
          setAlerts(alertList)
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

    loadDashboard()
    return () => {
      isCurrent = false
    }
  }, [appliedFilters, logout, reloadKey, token])

  function handleFilterChange(event) {
    const { name, value } = event.target
    setFilterError('')
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

    if (filters.period === 'CUSTOM' && (!filters.startDate || !filters.endDate)) {
      setFilterError('Informe as datas inicial e final do período.')
      return
    }
    if (filters.period === 'CUSTOM' && filters.startDate > filters.endDate) {
      setFilterError('A data inicial deve ser anterior à data final.')
      return
    }

    setError('')
    setFilterError('')
    setIsLoading(true)
    setAppliedFilters(buildApiFilters(filters))
  }

  function clearFilters() {
    const clearedFilters = { ...emptyFilters, period: 'ALL' }
    setFilters(clearedFilters)
    setAppliedFilters(buildApiFilters(clearedFilters))
    setFilterError('')
    setError('')
    setIsLoading(true)
  }

  function retry() {
    setError('')
    setIsLoading(true)
    setReloadKey((currentKey) => currentKey + 1)
  }

  if (isLoading) {
    return (
      <FeedbackState
        type="loading"
        title="Preparando o painel administrativo"
        message="Buscando ocorrências e alertas cadastrados."
      />
    )
  }

  if (error) {
    return (
      <div className="admin-dashboard-error">
        <FeedbackState type="error" message={error} />
        <Button onClick={retry} variant="outline">
          <RotateCw size={17} aria-hidden="true" />
          Tentar novamente
        </Button>
      </div>
    )
  }

  const statusCounts = Object.fromEntries(
    Object.keys(occurrenceStatusConfig).map((status) => [
      status,
      occurrences.filter((occurrence) => occurrence.status === status).length,
    ]),
  )
  const metrics = { ...statusCounts, total: occurrences.length }
  const maxStatusCount = Math.max(...Object.values(statusCounts), 1)
  const recentOccurrences = occurrences.slice(0, 5)
  const recentAlerts = alerts.slice(0, 3)
  const enabledAlerts = alerts.filter((alert) => alert.active).length
  const firstName = user.name.split(' ')[0]

  return (
    <section className="admin-dashboard">
      <header className="admin-dashboard__heading">
        <div>
          <span>Painel administrativo</span>
          <h1>Visão geral</h1>
          <p>Olá, {firstName}. Acompanhe os registros do Cidade em Foco.</p>
        </div>
        <Button onClick={retry} variant="outline">
          <RotateCw size={17} aria-hidden="true" />
          Atualizar dados
        </Button>
      </header>

      <form className="admin-dashboard-filters" onSubmit={applyFilters}>
        <div className="admin-dashboard-filters__title">
          <SlidersHorizontal size={20} aria-hidden="true" />
          <div>
            <strong>Personalizar visão</strong>
            <span>Os indicadores, gráfico, mapa e registros usam os mesmos filtros.</span>
          </div>
        </div>

        <div className="form-field">
          <label htmlFor="dashboard-filter-period">Período</label>
          <select className="form-control" id="dashboard-filter-period" name="period" onChange={handleFilterChange} value={filters.period}>
            <option value="7">Últimos 7 dias</option>
            <option value="30">Últimos 30 dias</option>
            <option value="90">Últimos 90 dias</option>
            <option value="ALL">Todo o período</option>
            <option value="CUSTOM">Personalizado</option>
          </select>
        </div>

        {filters.period === 'CUSTOM' && (
          <>
            <div className="form-field">
              <label htmlFor="dashboard-filter-start">Data inicial</label>
              <input className="form-control" id="dashboard-filter-start" name="startDate" onChange={handleFilterChange} type="date" value={filters.startDate} />
            </div>
            <div className="form-field">
              <label htmlFor="dashboard-filter-end">Data final</label>
              <input className="form-control" id="dashboard-filter-end" name="endDate" onChange={handleFilterChange} type="date" value={filters.endDate} />
            </div>
          </>
        )}

        <div className="form-field">
          <label htmlFor="dashboard-filter-category">Categoria</label>
          <select className="form-control" id="dashboard-filter-category" name="category" onChange={handleFilterChange} value={filters.category}>
            <option value="">Todas</option>
            {Object.entries(occurrenceCategoryLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </div>

        <div className="form-field">
          <label htmlFor="dashboard-filter-type">Tipo</label>
          <select className="form-control" id="dashboard-filter-type" name="type" onChange={handleFilterChange} value={filters.type}>
            <option value="">Todos</option>
            {availableTypes.map((value) => <option key={value} value={value}>{occurrenceTypeLabels[value]}</option>)}
          </select>
        </div>

        <div className="form-field">
          <label htmlFor="dashboard-filter-status">Status</label>
          <select className="form-control" id="dashboard-filter-status" name="status" onChange={handleFilterChange} value={filters.status}>
            <option value="">Todos</option>
            {Object.entries(occurrenceStatusConfig).map(([value, config]) => <option key={value} value={value}>{config.label}</option>)}
          </select>
        </div>

        <div className="form-field">
          <label htmlFor="dashboard-filter-neighborhood">Bairro</label>
          <input className="form-control" id="dashboard-filter-neighborhood" maxLength={100} name="neighborhood" onChange={handleFilterChange} placeholder="Ex.: Boqueirão" value={filters.neighborhood} />
        </div>

        <div className="admin-dashboard-filters__actions">
          <Button type="submit"><Search size={17} aria-hidden="true" />Aplicar</Button>
          <Button onClick={clearFilters} type="button" variant="outline"><FilterX size={17} aria-hidden="true" />Limpar</Button>
        </div>

        {filterError && <p className="admin-dashboard-filters__error" role="alert">{filterError}</p>}
      </form>

      <div className="admin-metrics">
        {metricConfig.map(({ key, label, icon: Icon, tone }) => (
          <article className={`admin-metric admin-metric--${tone}`} key={key}>
            <div><Icon size={22} aria-hidden="true" /></div>
            <span>{label}</span>
            <strong>{metrics[key]}</strong>
          </article>
        ))}
      </div>

      <div className="admin-dashboard__main-grid">
        <section className="admin-dashboard-card admin-map-card">
          <div className="admin-card-heading">
            <div>
              <span>Distribuição geográfica</span>
              <h2>Mapa de ocorrências</h2>
            </div>
            <Link to="/admin/ocorrencias">Gerenciar</Link>
          </div>
          <OccurrenceMap occurrences={occurrences} />
        </section>

        <section className="admin-dashboard-card admin-recent-card">
          <div className="admin-card-heading">
            <div>
              <span>Últimos registros</span>
              <h2>Ocorrências recentes</h2>
            </div>
            <Link to="/admin/ocorrencias">Ver todas</Link>
          </div>

          {recentOccurrences.length > 0 ? (
            <div className="admin-recent-list">
              {recentOccurrences.map((occurrence) => (
                <OccurrenceCard
                  occurrence={occurrence}
                  key={occurrence.id}
                  to={`/admin/ocorrencias/${occurrence.id}`}
                />
              ))}
            </div>
          ) : (
            <FeedbackState type="empty" title="Nenhuma ocorrência cadastrada" />
          )}
        </section>
      </div>

      <div className="admin-dashboard__secondary-grid">
        <section className="admin-dashboard-card">
          <div className="admin-card-heading">
            <div>
              <span>Acompanhamento</span>
              <h2>Ocorrências por status</h2>
            </div>
          </div>

          <div className="status-distribution">
            {Object.entries(occurrenceStatusConfig).map(([status, config]) => (
              <div className="status-distribution__item" key={status}>
                <div>
                  <span>{config.label}</span>
                  <strong>{statusCounts[status]}</strong>
                </div>
                <div className="status-distribution__track">
                  <span
                    className={`status-distribution__bar status-distribution__bar--${config.tone}`}
                    style={{ width: `${(statusCounts[status] / maxStatusCount) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="admin-dashboard-card">
          <div className="admin-card-heading">
            <div>
              <span>Clima</span>
              <h2>Alertas cadastrados</h2>
            </div>
            <Link to="/admin/alertas">Gerenciar</Link>
          </div>

          <div className="admin-alert-summary">
            <div className="admin-alert-summary__count">
              <Bell size={21} aria-hidden="true" />
              <div><strong>{enabledAlerts}</strong><span>marcados como ativos</span></div>
            </div>

            {recentAlerts.length > 0 ? (
              <div className="admin-alert-list">
                {recentAlerts.map((alert) => (
                  <article key={alert.id}>
                    <div>
                      <strong>{alert.title}</strong>
                      <span>{getAlertTypeLabel(alert.type)} · {getAlertSeverityLabel(alert.severity)}</span>
                    </div>
                    <time dateTime={alert.createdAt}>{formatDateTime(alert.createdAt)}</time>
                  </article>
                ))}
              </div>
            ) : (
              <p className="admin-alert-summary__empty">Nenhum alerta cadastrado.</p>
            )}
          </div>
        </section>
      </div>
    </section>
  )
}

export default AdminDashboardPage
