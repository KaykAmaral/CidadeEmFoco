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
  occurrenceCategoryPresentation,
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

function startOfLocalDay(value) {
  const date = new Date(value)
  date.setHours(0, 0, 0, 0)
  return date
}

function startOfWeek(value) {
  const date = startOfLocalDay(value)
  date.setDate(date.getDate() - ((date.getDay() + 6) % 7))
  return date
}

function getTimeGrouping(period, appliedFilters) {
  if (period === '7' || period === '30') return 'day'
  if (period !== 'CUSTOM') return 'week'

  const duration = new Date(appliedFilters.createdTo) - new Date(appliedFilters.createdFrom)
  return duration <= 31 * 24 * 60 * 60 * 1000 ? 'day' : 'week'
}

function buildOccurrenceTimeline(occurrences, period, appliedFilters) {
  if (occurrences.length === 0) return []

  const grouping = getTimeGrouping(period, appliedFilters)
  const occurrenceDates = occurrences.map((occurrence) => new Date(occurrence.createdAt))
  const firstDate = appliedFilters.createdFrom
    ? new Date(appliedFilters.createdFrom)
    : new Date(Math.min(...occurrenceDates))
  const lastDate = appliedFilters.createdTo
    ? new Date(appliedFilters.createdTo)
    : new Date(Math.max(...occurrenceDates))
  const start = grouping === 'day' ? startOfLocalDay(firstDate) : startOfWeek(firstDate)
  const end = grouping === 'day' ? startOfLocalDay(lastDate) : startOfWeek(lastDate)
  const increment = grouping === 'day' ? 1 : 7
  const counts = new Map()

  occurrences.forEach((occurrence) => {
    const bucketDate = grouping === 'day'
      ? startOfLocalDay(occurrence.createdAt)
      : startOfWeek(occurrence.createdAt)
    const key = bucketDate.toISOString().slice(0, 10)
    counts.set(key, (counts.get(key) ?? 0) + 1)
  })

  const timeline = []
  for (const date = new Date(start); date <= end; date.setDate(date.getDate() + increment)) {
    const bucketDate = new Date(date)
    const key = bucketDate.toISOString().slice(0, 10)
    timeline.push({
      key,
      count: counts.get(key) ?? 0,
      label: grouping === 'day'
        ? bucketDate.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
        : bucketDate.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' }).replace('.', ''),
      fullLabel: grouping === 'day'
        ? bucketDate.toLocaleDateString('pt-BR')
        : `Semana de ${bucketDate.toLocaleDateString('pt-BR')}`,
    })
  }

  return timeline
}

function buildCategoryDistribution(occurrences) {
  return Object.entries(occurrenceCategoryLabels).map(([category, label]) => ({
    category,
    label,
    color: occurrenceCategoryPresentation[category].color,
    count: occurrences.filter((occurrence) => occurrence.category === category).length,
  }))
}

function buildDonutBackground(categories, total) {
  if (total === 0) return 'var(--color-background)'

  let accumulated = 0
  const stops = categories.flatMap(({ color, count }) => {
    const start = (accumulated / total) * 100
    accumulated += count
    const end = (accumulated / total) * 100
    return [`${color} ${start}%`, `${color} ${end}%`]
  })

  return `conic-gradient(${stops.join(', ')})`
}

function AdminDashboardPage() {
  const { logout, token, user } = useAuth()
  const [filters, setFilters] = useState(emptyFilters)
  const [appliedFilters, setAppliedFilters] = useState(() => buildApiFilters(emptyFilters))
  const [appliedPeriod, setAppliedPeriod] = useState(emptyFilters.period)
  const [occurrences, setOccurrences] = useState([])
  const [alerts, setAlerts] = useState([])
  const [isOccurrencesLoading, setIsOccurrencesLoading] = useState(true)
  const [isAlertsLoading, setIsAlertsLoading] = useState(true)
  const [occurrencesError, setOccurrencesError] = useState('')
  const [alertsError, setAlertsError] = useState('')
  const [filterError, setFilterError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [lastUpdatedAt, setLastUpdatedAt] = useState(null)
  const availableTypes = filters.category
    ? occurrenceTypesByCategory[filters.category]
    : Object.keys(occurrenceTypeLabels)

  useEffect(() => {
    let isCurrent = true

    async function loadOccurrences() {
      try {
        const occurrenceList = await getAdminOccurrences(token, appliedFilters)

        if (isCurrent) setOccurrences(occurrenceList)
      } catch (requestError) {
        if (!isCurrent) return
        if (requestError.status === 401) {
          logout()
          return
        }
        setOccurrencesError(requestError.message)
      } finally {
        if (isCurrent) setIsOccurrencesLoading(false)
      }
    }

    loadOccurrences()
    return () => {
      isCurrent = false
    }
  }, [appliedFilters, logout, reloadKey, token])

  useEffect(() => {
    let isCurrent = true

    async function loadAlerts() {
      try {
        const alertList = await getAdminAlerts(token)

        if (isCurrent) {
          setAlerts(alertList)
          setLastUpdatedAt(new Date())
        }
      } catch (requestError) {
        if (!isCurrent) return
        if (requestError.status === 401) {
          logout()
          return
        }
        setAlertsError(requestError.message)
      } finally {
        if (isCurrent) setIsAlertsLoading(false)
      }
    }

    loadAlerts()
    return () => {
      isCurrent = false
    }
  }, [logout, reloadKey, token])

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

    setOccurrencesError('')
    setFilterError('')
    setIsOccurrencesLoading(true)
    setAppliedFilters(buildApiFilters(filters))
    setAppliedPeriod(filters.period)
  }

  function clearFilters() {
    const clearedFilters = { ...emptyFilters, period: 'ALL' }
    setFilters(clearedFilters)
    setAppliedFilters(buildApiFilters(clearedFilters))
    setAppliedPeriod(clearedFilters.period)
    setFilterError('')
    setOccurrencesError('')
    setIsOccurrencesLoading(true)
  }

  function retry() {
    setOccurrencesError('')
    setAlertsError('')
    setIsOccurrencesLoading(true)
    setIsAlertsLoading(true)
    setReloadKey((currentKey) => currentKey + 1)
  }

  if (isOccurrencesLoading || isAlertsLoading) {
    return (
      <FeedbackState
        type="loading"
        title="Preparando o painel administrativo"
        message="Buscando ocorrências e alertas cadastrados."
      />
    )
  }

  const error = occurrencesError || alertsError

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
  const recentOccurrences = occurrences.slice(0, 5)
  const recentAlerts = alerts.slice(0, 3)
  const enabledAlerts = alerts.filter((alert) => alert.active).length
  const firstName = user.name.split(' ')[0]
  const occurrenceTimeline = buildOccurrenceTimeline(occurrences, appliedPeriod, appliedFilters)
  const maxTimelineCount = Math.max(...occurrenceTimeline.map(({ count }) => count), 1)
  const categoryDistribution = buildCategoryDistribution(occurrences)
  const donutBackground = buildDonutBackground(categoryDistribution, occurrences.length)

  return (
    <section className="admin-dashboard">
      <header className="admin-dashboard__heading">
        <div>
          <span>Painel administrativo</span>
          <h1>Visão geral</h1>
          <p>Olá, {firstName}. Acompanhe os registros de <strong>{user.city?.name ?? 'sua cidade'}</strong>.</p>
        </div>
        <div className="admin-dashboard__refresh">
          {lastUpdatedAt && <span>Atualizado em {formatDateTime(lastUpdatedAt.toISOString())}</span>}
          <Button onClick={retry} variant="outline">
            <RotateCw size={17} aria-hidden="true" />
            Atualizar dados
          </Button>
        </div>
      </header>

      <form className="admin-dashboard-filters" onSubmit={applyFilters}>
        <div className="admin-dashboard-filters__title">
          <SlidersHorizontal size={20} aria-hidden="true" />
          <div>
            <strong>Personalizar visão</strong>
            <span>Indicadores, mapa e registros seguem os mesmos filtros.</span>
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
            <FeedbackState
              type="empty"
              title="Nenhuma ocorrência encontrada"
              message="Ajuste ou limpe os filtros para consultar outros registros."
            />
          )}
        </section>
      </div>

      <div className="admin-dashboard__secondary-grid">
        <section className="admin-dashboard-card admin-chart-card">
          <div className="admin-card-heading">
            <div>
              <span>Evolução</span>
              <h2>Ocorrências ao longo do tempo</h2>
            </div>
          </div>

          {occurrenceTimeline.length > 0 ? (
            <div className="admin-time-chart" role="img" aria-label={`Gráfico com ${occurrences.length} ocorrências distribuídas ao longo do tempo`}>
              <div className="admin-time-chart__plot" style={{ '--chart-columns': occurrenceTimeline.length }}>
                {occurrenceTimeline.map(({ key, count, label, fullLabel }, index) => {
                  const labelInterval = Math.max(1, Math.ceil(occurrenceTimeline.length / 6))
                  const showLabel = index === 0 || index === occurrenceTimeline.length - 1 || index % labelInterval === 0

                  return (
                    <div className="admin-time-chart__column" key={key} title={`${fullLabel}: ${count} ocorrência${count === 1 ? '' : 's'}`}>
                      <span className="admin-time-chart__value">{count || ''}</span>
                      <span
                        className={`admin-time-chart__bar${count === 0 ? ' admin-time-chart__bar--empty' : ''}`}
                        style={{ height: `${(count / maxTimelineCount) * 100}%` }}
                      />
                      <span className="admin-time-chart__label">{showLabel ? label : ''}</span>
                    </div>
                  )
                })}
              </div>
            </div>
          ) : (
            <div className="admin-chart-empty">
              <strong>Nenhuma ocorrência no período</strong>
              <span>Ajuste ou limpe os filtros para visualizar a evolução.</span>
            </div>
          )}
        </section>

        <section className="admin-dashboard-card admin-chart-card">
          <div className="admin-card-heading">
            <div>
              <span>Distribuição</span>
              <h2>Ocorrências por categoria</h2>
            </div>
          </div>

          {occurrences.length > 0 ? (
            <div className="admin-category-chart">
              <div
                className="admin-category-chart__donut"
                role="img"
                aria-label={`${occurrences.length} ocorrências distribuídas por categoria`}
                style={{ background: donutBackground }}
              >
                <div>
                  <strong>{occurrences.length}</strong>
                  <span>Total</span>
                </div>
              </div>

              <div className="admin-category-chart__legend">
                {categoryDistribution.map(({ category, label, color, count }) => (
                  <div key={category}>
                    <span className="admin-category-chart__swatch" style={{ background: color }} />
                    <span>{label}</span>
                    <strong>{count}</strong>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="admin-chart-empty">
              <strong>Nenhuma categoria para exibir</strong>
              <span>As categorias aparecerão quando houver ocorrências nos filtros atuais.</span>
            </div>
          )}
        </section>

        <section className="admin-dashboard-card admin-alerts-card">
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
