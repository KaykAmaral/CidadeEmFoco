import {
  Bell,
  CheckCircle2,
  ClipboardList,
  Clock3,
  FilterX,
  RotateCw,
  Search,
  SearchCheck,
} from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router'
import LiveOccurrenceMap from '../components/map/LiveOccurrenceMap'
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
import { buildDashboardFilters, emptyDashboardFilters } from '../utils/adminDashboardFilters'
import './AdminDashboardPage.css'

const metricConfig = [
  { key: 'total', label: 'Total de ocorrências', icon: ClipboardList, tone: 'blue' },
  { key: 'REGISTRADA', label: 'Registradas', icon: Clock3, tone: 'blue' },
  { key: 'EM_ATENDIMENTO', label: 'Em atendimento', icon: SearchCheck, tone: 'orange' },
  { key: 'RESOLVIDA', label: 'Resolvidas', icon: CheckCircle2, tone: 'green' },
]

function AdminDashboardPage() {
  const { logout, token, user } = useAuth()
  const [occurrences, setOccurrences] = useState([])
  const [alerts, setAlerts] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [filters, setFilters] = useState(emptyDashboardFilters)
  const [appliedFilters, setAppliedFilters] = useState(() => buildDashboardFilters(emptyDashboardFilters))
  const [filterError, setFilterError] = useState('')
  const alertCache = useRef(null)
  const availableTypes = filters.category
    ? occurrenceTypesByCategory[filters.category]
    : Object.keys(occurrenceTypeLabels)

  useEffect(() => {
    const controller = new AbortController()
    const { signal } = controller

    async function loadDashboard() {
      try {
        const cached = alertCache.current
        const alertRequest = cached?.token === token && cached.reloadKey === reloadKey
          ? Promise.resolve(cached.data)
          : getAdminAlerts(token, signal).then((data) => {
            if (!signal.aborted) alertCache.current = { token, reloadKey, data }
            return data
          })
        const [occurrenceList, alertList] = await Promise.all([
          getAdminOccurrences(token, appliedFilters, signal),
          alertRequest,
        ])

        if (!signal.aborted) {
          setOccurrences(occurrenceList)
          setAlerts(alertList)
        }
      } catch (requestError) {
        if (signal.aborted) return
        if (requestError.status === 401) {
          logout()
          return
        }
        setError(requestError.message)
      } finally {
        if (!signal.aborted) setIsLoading(false)
      }
    }

    // Allow StrictMode's initial cleanup to cancel before any HTTP request starts.
    const timer = window.setTimeout(loadDashboard, 0)
    return () => {
      window.clearTimeout(timer)
      controller.abort()
    }
  }, [appliedFilters, logout, reloadKey, token])

  function handleFilterChange(event) {
    const { name, value } = event.target
    setFilterError('')
    setFilters((current) => ({
      ...current,
      [name]: value,
      ...(name === 'category' && value && current.type && !occurrenceTypesByCategory[value].includes(current.type)
        ? { type: '' } : {}),
    }))
  }

  function updateFilters(nextFilters) {
    try {
      const next = buildDashboardFilters(nextFilters)
      setFilterError('')
      if (JSON.stringify(next) === JSON.stringify(appliedFilters)) return
      setError('')
      setIsLoading(true)
      setAppliedFilters(next)
    } catch (validationError) {
      setFilterError(validationError.message)
    }
  }

  function applyFilters(event) {
    event.preventDefault()
    updateFilters(filters)
  }

  function clearFilters() {
    setFilters(emptyDashboardFilters)
    updateFilters(emptyDashboardFilters)
  }

  function retry() {
    setError('')
    setIsLoading(true)
    setReloadKey((currentKey) => currentKey + 1)
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
        <Button disabled={isLoading} onClick={retry} variant="outline">
          <RotateCw size={17} aria-hidden="true" />
          Atualizar dados
        </Button>
      </header>

      <form className="admin-dashboard-filters" onSubmit={applyFilters} aria-label="Filtros de ocorrências">
        <p className="admin-dashboard-filters__description" id="dashboard-filter-help">
          Filtre as ocorrências pela data de cadastro. O período inclui o dia final, no seu horário local.
          Os indicadores, o mapa e as ocorrências recentes seguem os filtros aplicados. O mapa oculta resolvidas após o prazo configurado; o histórico permanece nos indicadores e na lista. Os alertas climáticos são gerais.
        </p>
        <div className="form-field">
          <label htmlFor="dashboard-start-date">Data inicial</label>
          <input className="form-control" id="dashboard-start-date" name="startDate" type="date" max={filters.endDate || undefined} value={filters.startDate} onChange={handleFilterChange} aria-describedby="dashboard-filter-help" />
        </div>
        <div className="form-field">
          <label htmlFor="dashboard-end-date">Data final</label>
          <input className="form-control" id="dashboard-end-date" name="endDate" type="date" min={filters.startDate || undefined} value={filters.endDate} onChange={handleFilterChange} aria-describedby="dashboard-filter-help" />
        </div>
        <div className="form-field">
          <label htmlFor="dashboard-neighborhood">Bairro</label>
          <input className="form-control" id="dashboard-neighborhood" name="neighborhood" maxLength={100} placeholder="Ex.: Boqueirão" value={filters.neighborhood} onChange={handleFilterChange} />
        </div>
        <div className="form-field">
          <label htmlFor="dashboard-category">Categoria</label>
          <select className="form-control" id="dashboard-category" name="category" value={filters.category} onChange={handleFilterChange}>
            <option value="">Todas</option>
            {Object.entries(occurrenceCategoryLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </div>
        <div className="form-field">
          <label htmlFor="dashboard-type">Tipo de ocorrência</label>
          <select className="form-control" id="dashboard-type" name="type" value={filters.type} onChange={handleFilterChange}>
            <option value="">Todos</option>
            {availableTypes.map((value) => <option key={value} value={value}>{occurrenceTypeLabels[value]}</option>)}
          </select>
        </div>
        <div className="form-field">
          <label htmlFor="dashboard-status">Status</label>
          <select className="form-control" id="dashboard-status" name="status" value={filters.status} onChange={handleFilterChange}>
            <option value="">Todos</option>
            {Object.entries(occurrenceStatusConfig).map(([value, config]) => <option key={value} value={value}>{config.label}</option>)}
          </select>
        </div>
        <div className="admin-dashboard-filters__actions">
          <Button disabled={isLoading} type="submit"><Search size={17} aria-hidden="true" />Aplicar filtros</Button>
          <Button onClick={clearFilters} variant="outline"><FilterX size={17} aria-hidden="true" />Limpar filtros</Button>
        </div>
        {filterError && <p className="admin-dashboard-filters__error" role="alert">{filterError}</p>}
      </form>

      {isLoading ? (
        <FeedbackState type="loading" title="Atualizando o painel administrativo" message="Buscando os registros para os filtros aplicados." />
      ) : error ? (
        <div className="admin-dashboard-error">
          <FeedbackState type="error" message={error} />
          <Button onClick={retry} variant="outline"><RotateCw size={17} aria-hidden="true" />Tentar novamente</Button>
        </div>
      ) : (
        <>
          <p className="admin-dashboard__result-count" role="status">
            {occurrences.length} {occurrences.length === 1 ? 'ocorrência encontrada' : 'ocorrências encontradas'} nos filtros aplicados.
          </p>
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
              <LiveOccurrenceMap filters={appliedFilters} />
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
                <FeedbackState type="empty" title="Nenhuma ocorrência encontrada" message="Altere ou limpe os filtros para consultar outros registros." />
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
                  <span>Clima · visão geral, sem filtros</span>
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
        </>
      )}
    </section>
  )
}

export default AdminDashboardPage
