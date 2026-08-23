import {
  Bell,
  CheckCircle2,
  ClipboardList,
  Clock3,
  RotateCw,
  SearchCheck,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import OccurrenceMap from '../components/map/OccurrenceMap'
import OccurrenceCard from '../components/occurrences/OccurrenceCard'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import occurrenceStatusConfig from '../constants/occurrenceStatus'
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

function AdminDashboardPage() {
  const { logout, token, user } = useAuth()
  const [occurrences, setOccurrences] = useState([])
  const [alerts, setAlerts] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let isCurrent = true

    async function loadDashboard() {
      try {
        const [occurrenceList, alertList] = await Promise.all([
          getAdminOccurrences(token),
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
  }, [logout, reloadKey, token])

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
