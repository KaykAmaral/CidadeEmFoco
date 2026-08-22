import { Info, RotateCw } from 'lucide-react'
import { useEffect, useState } from 'react'
import ClimateAlertCard from '../components/alerts/ClimateAlertCard'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import useAuth from '../hooks/useAuth'
import { getActiveAlerts } from '../services/alertService'
import './AlertsPage.css'

function AlertsPage() {
  const { logout, token } = useAuth()
  const [alerts, setAlerts] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let isCurrent = true

    getActiveAlerts(token)
      .then((data) => {
        if (isCurrent) setAlerts(data)
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
  }, [logout, reloadKey, token])

  function retry() {
    setError('')
    setIsLoading(true)
    setReloadKey((currentKey) => currentKey + 1)
  }

  return (
    <section className="alerts-page">
      <header className="alerts-page__heading">
        <div>
          <span>Informação climática</span>
          <h1>Alertas ativos</h1>
          <p>Acompanhe os alertas demonstrativos válidos neste momento.</p>
        </div>
        {!isLoading && !error && (
          <span className="alerts-page__count">
            {alerts.length} {alerts.length === 1 ? 'alerta ativo' : 'alertas ativos'}
          </span>
        )}
      </header>

      <div className="alerts-disclaimer">
        <Info size={22} aria-hidden="true" />
        <div>
          <strong>Informação demonstrativa do MVP</strong>
          <p>
            Estes alertas são cadastrados manualmente e não substituem avisos
            oficiais, orientações da Defesa Civil ou serviços meteorológicos.
          </p>
        </div>
      </div>

      {isLoading ? (
        <FeedbackState type="loading" message="Buscando alertas ativos..." />
      ) : error ? (
        <div className="alerts-page__error">
          <FeedbackState type="error" message={error} />
          <Button onClick={retry} variant="outline">
            <RotateCw size={17} aria-hidden="true" />
            Tentar novamente
          </Button>
        </div>
      ) : alerts.length === 0 ? (
        <FeedbackState
          type="empty"
          title="Nenhum alerta ativo"
          message="Não há alertas demonstrativos válidos neste momento."
        />
      ) : (
        <div className="alerts-page__list">
          {alerts.map((alert) => (
            <ClimateAlertCard alert={alert} key={alert.id} />
          ))}
        </div>
      )}
    </section>
  )
}

export default AlertsPage
