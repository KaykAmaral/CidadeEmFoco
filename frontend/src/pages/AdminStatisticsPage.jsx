import { BarChart3, Clock3, FilterX, RotateCw, Search, SlidersHorizontal } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import occurrenceStatusConfig from '../constants/occurrenceStatus'
import {
  occurrenceCategoryLabels,
  occurrenceCategoryPresentation,
  occurrenceTypeLabels,
  occurrenceTypesByCategory,
} from '../constants/occurrencePresentation'
import useAuth from '../hooks/useAuth'
import { getAdminOccurrenceMetrics } from '../services/adminService'
import {
  aggregateEvolution,
  buildStatisticsApiFilters,
  emptyStatisticsFilters,
  formatResolutionTime,
} from '../utils/adminStatistics'
import './AdminDashboardPage.css'
import './AdminStatisticsPage.css'

function donutBackground(categories, total) {
  if (!total) return 'var(--color-background)'
  let accumulated = 0
  return `conic-gradient(${categories.flatMap(({ color, count }) => {
    const start = (accumulated / total) * 100
    accumulated += count
    return [`${color} ${start}%`, `${color} ${(accumulated / total) * 100}%`]
  }).join(', ')})`
}

function EmptyChart({ children, title }) {
  return <div className="admin-chart-empty"><strong>{title}</strong><span>{children}</span></div>
}

function AdminStatisticsPage() {
  const { logout, token, user } = useAuth()
  const [filters, setFilters] = useState(emptyStatisticsFilters)
  const [appliedFilters, setAppliedFilters] = useState(() => buildStatisticsApiFilters(emptyStatisticsFilters))
  const [metrics, setMetrics] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [filterError, setFilterError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const availableTypes = filters.category ? occurrenceTypesByCategory[filters.category] : Object.keys(occurrenceTypeLabels)

  useEffect(() => {
    let isCurrent = true
    const controller = new AbortController()
    getAdminOccurrenceMetrics(token, appliedFilters, controller.signal)
      .then((data) => { if (isCurrent) setMetrics(data) })
      .catch((requestError) => {
        if (!isCurrent || requestError.name === 'AbortError') return
        if (requestError.status === 401) return logout()
        setError(requestError.message)
      })
      .finally(() => { if (isCurrent) setIsLoading(false) })
    return () => { isCurrent = false; controller.abort() }
  }, [appliedFilters, logout, reloadKey, token])

  function changeFilter(event) {
    const { name, value } = event.target
    setFilterError('')
    setFilters((current) => {
      const resetType = name === 'category' && value && current.type && !occurrenceTypesByCategory[value].includes(current.type)
      return { ...current, [name]: value, ...(resetType ? { type: '' } : {}) }
    })
  }

  function applyFilters(event) {
    event.preventDefault()
    try {
      setAppliedFilters(buildStatisticsApiFilters(filters))
      setFilterError('')
      setError('')
      setIsLoading(true)
    } catch (filterValidationError) {
      setFilterError(filterValidationError.message)
    }
  }

  function clearFilters() {
    const cleared = { ...emptyStatisticsFilters, period: 'ALL' }
    setFilters(cleared)
    setAppliedFilters(buildStatisticsApiFilters(cleared))
    setFilterError('')
    setError('')
    setIsLoading(true)
  }

  function retry() {
    setError('')
    setIsLoading(true)
    setReloadKey((current) => current + 1)
  }

  const evolution = useMemo(() => aggregateEvolution(
    metrics?.dailyEvolution ?? [], metrics?.createdFrom, metrics?.createdTo,
  ), [metrics])
  const categoryDistribution = Object.entries(occurrenceCategoryLabels).map(([category, label]) => ({
    category, label, color: occurrenceCategoryPresentation[category].color,
    count: metrics?.casesByCategory?.[category] ?? 0,
  }))
  const neighborhoods = metrics?.casesByNeighborhood ?? []
  const maxNeighborhood = Math.max(...neighborhoods.map(({ count }) => count), 1)
  const maxEvolution = Math.max(...evolution.flatMap(({ registeredCases, resolvedCases }) => [registeredCases, resolvedCases]), 1)
  const averageResolution = formatResolutionTime(metrics?.averageResolutionTimeMinutes)

  return (
    <section className="admin-statistics">
      <header className="admin-dashboard__heading">
        <div><span>Inteligência urbana</span><h1>Estatísticas</h1><p>Indicadores consolidados de <strong>{user.city?.name ?? 'sua cidade'}</strong>, considerando somente casos principais.</p></div>
        <Button onClick={retry} variant="outline"><RotateCw size={17} aria-hidden="true" />Atualizar dados</Button>
      </header>

      <form className="admin-dashboard-filters" onSubmit={applyFilters}>
        <div className="admin-dashboard-filters__title"><SlidersHorizontal size={20} aria-hidden="true" /><div><strong>Filtrar estatísticas</strong><span>Todos os cartões seguem os mesmos filtros.</span></div></div>
        <div className="form-field"><label htmlFor="statistics-period">Período</label><select className="form-control" id="statistics-period" name="period" onChange={changeFilter} value={filters.period}><option value="7">Últimos 7 dias</option><option value="30">Últimos 30 dias</option><option value="90">Últimos 90 dias</option><option value="ALL">Todo o período</option><option value="CUSTOM">Personalizado</option></select></div>
        {filters.period === 'CUSTOM' && <><div className="form-field"><label htmlFor="statistics-start">Data inicial</label><input className="form-control" id="statistics-start" name="startDate" onChange={changeFilter} type="date" value={filters.startDate} /></div><div className="form-field"><label htmlFor="statistics-end">Data final</label><input className="form-control" id="statistics-end" name="endDate" onChange={changeFilter} type="date" value={filters.endDate} /></div></>}
        <div className="form-field"><label htmlFor="statistics-category">Categoria</label><select className="form-control" id="statistics-category" name="category" onChange={changeFilter} value={filters.category}><option value="">Todas</option>{Object.entries(occurrenceCategoryLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></div>
        <div className="form-field"><label htmlFor="statistics-type">Tipo</label><select className="form-control" id="statistics-type" name="type" onChange={changeFilter} value={filters.type}><option value="">Todos</option>{availableTypes.map((value) => <option key={value} value={value}>{occurrenceTypeLabels[value]}</option>)}</select></div>
        <div className="form-field"><label htmlFor="statistics-status">Status</label><select className="form-control" id="statistics-status" name="status" onChange={changeFilter} value={filters.status}><option value="">Todos</option>{Object.entries(occurrenceStatusConfig).map(([value, config]) => <option key={value} value={value}>{config.label}</option>)}</select></div>
        <div className="form-field"><label htmlFor="statistics-neighborhood">Bairro</label><input className="form-control" id="statistics-neighborhood" maxLength={100} name="neighborhood" onChange={changeFilter} placeholder="Ex.: Boqueirão" value={filters.neighborhood} /></div>
        <div className="admin-dashboard-filters__actions"><Button type="submit"><Search size={17} aria-hidden="true" />Aplicar</Button><Button onClick={clearFilters} type="button" variant="outline"><FilterX size={17} aria-hidden="true" />Limpar</Button></div>
        {filterError && <p className="admin-dashboard-filters__error" role="alert">{filterError}</p>}
      </form>

      {isLoading ? <FeedbackState type="loading" title="Calculando estatísticas" message="Consolidando os casos da cidade gerenciada." /> : error ? <div className="admin-dashboard-error"><FeedbackState type="error" message={error} /><Button onClick={retry} variant="outline">Tentar novamente</Button></div> : <>
        <div className="admin-statistics__summary">
          <article><BarChart3 size={22} aria-hidden="true" /><span>Casos no período</span><strong>{metrics?.totalCases ?? 0}</strong><small>{metrics?.totalReports ?? 0} relatos recebidos</small></article>
          <article className="admin-statistics__resolution"><Clock3 size={22} aria-hidden="true" /><span>Tempo médio de solução</span>{averageResolution ? <><strong>{averageResolution.value}</strong><small>{averageResolution.unit} · somente casos resolvidos</small></> : <><strong>—</strong><small>Sem casos resolvidos com datas válidas</small></>}</article>
        </div>

        <div className="admin-statistics__grid">
          <section className="admin-dashboard-card admin-statistics-card admin-statistics-card--wide"><div className="admin-card-heading"><div><span>Evolução</span><h2>Ocorrências ao longo do tempo</h2></div></div>{evolution.length ? <div className="admin-time-chart" role="img" aria-label="Registros de ocorrências ao longo do período"><div className="admin-time-chart__plot" style={{ '--chart-columns': evolution.length }}>{evolution.map(({ key, label, fullLabel, registeredCases }) => <div className="admin-time-chart__column" key={key} title={`${fullLabel}: ${registeredCases} registros`}><span className="admin-time-chart__value">{registeredCases || ''}</span><span className={`admin-time-chart__bar${registeredCases ? '' : ' admin-time-chart__bar--empty'}`} style={{ height: `${(registeredCases / maxEvolution) * 100}%` }} /><span className="admin-time-chart__label">{label}</span></div>)}</div></div> : <EmptyChart title="Nenhuma ocorrência no período">Ajuste os filtros para visualizar a evolução.</EmptyChart>}</section>

          <section className="admin-dashboard-card admin-statistics-card"><div className="admin-card-heading"><div><span>Distribuição</span><h2>Ocorrências por categoria</h2></div></div>{metrics?.totalCases ? <div className="admin-category-chart"><div className="admin-category-chart__donut" role="img" aria-label={`${metrics.totalCases} casos por categoria`} style={{ background: donutBackground(categoryDistribution, metrics.totalCases) }}><div><strong>{metrics.totalCases}</strong><span>Total</span></div></div><div className="admin-category-chart__legend">{categoryDistribution.map(({ category, label, color, count }) => <div key={category}><span className="admin-category-chart__swatch" style={{ background: color }} /><span>{label}</span><strong>{count}</strong></div>)}</div></div> : <EmptyChart title="Nenhuma categoria para exibir">Não há casos nos filtros atuais.</EmptyChart>}</section>

          <section className="admin-dashboard-card admin-statistics-card"><div className="admin-card-heading"><div><span>Território</span><h2>Ocorrências por bairro</h2></div></div>{neighborhoods.length ? <div className="admin-neighborhood-chart">{neighborhoods.map(({ name, count }) => <div key={name}><div><strong>{name || 'Não informado'}</strong><span>{count} · {metrics.totalCases ? ((count / metrics.totalCases) * 100).toLocaleString('pt-BR', { maximumFractionDigits: 1 }) : 0}%</span></div><span className="admin-neighborhood-chart__track"><span style={{ width: `${(count / maxNeighborhood) * 100}%` }} /></span></div>)}</div> : <EmptyChart title="Nenhum bairro para exibir">Os bairros aparecerão quando houver casos nos filtros atuais.</EmptyChart>}</section>

          <section className="admin-dashboard-card admin-statistics-card admin-statistics-card--wide"><div className="admin-card-heading"><div><span>Comparativo</span><h2>Registradas × resolvidas ao longo do tempo</h2></div></div>{evolution.length ? <div className="admin-comparison-chart"><div className="admin-comparison-chart__legend"><span><i className="admin-comparison-chart__swatch admin-comparison-chart__swatch--registered" />Registradas</span><span><i className="admin-comparison-chart__swatch admin-comparison-chart__swatch--resolved" />Resolvidas</span></div><div className="admin-comparison-chart__plot" style={{ '--chart-columns': evolution.length }}>{evolution.map(({ key, label, registeredCases, resolvedCases }) => <div className="admin-comparison-chart__column" key={key} title={`${label}: ${registeredCases} registradas e ${resolvedCases} resolvidas`}><div><span className="admin-comparison-chart__bar admin-comparison-chart__bar--registered" style={{ height: `${(registeredCases / maxEvolution) * 100}%` }} /><span className="admin-comparison-chart__bar admin-comparison-chart__bar--resolved" style={{ height: `${(resolvedCases / maxEvolution) * 100}%` }} /></div><span>{label}</span></div>)}</div></div> : <EmptyChart title="Comparação indisponível">Não há registros ou resoluções no período selecionado.</EmptyChart>}</section>
        </div>
      </>}
    </section>
  )
}

export default AdminStatisticsPage
