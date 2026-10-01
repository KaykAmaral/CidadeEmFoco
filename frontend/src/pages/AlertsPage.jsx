import { Info, RotateCw } from 'lucide-react'
import { useState } from 'react'
import ClimateAlertCard from '../components/alerts/ClimateAlertCard'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import useAlerts from '../hooks/useAlerts'
import './AlertsPage.css'

function AlertsPage() {
  const { alerts, error, isLoading, refreshAlerts } = useAlerts()
  const [isRefreshing, setIsRefreshing] = useState(false)

  async function retry() {
    setIsRefreshing(true)
    await refreshAlerts()
    setIsRefreshing(false)
  }

  return (
    <section className="alerts-page">
      <header className="alerts-page__heading">
        <div>
          <span>Informação climática</span>
          <h1>Alertas ativos</h1>
          <p>Acompanhe os alertas válidos neste momento.</p>
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
          <strong>Informação de acompanhamento</strong>
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
          <Button disabled={isRefreshing} onClick={retry} variant="outline">
            <RotateCw size={17} aria-hidden="true" />
            {isRefreshing ? 'Atualizando...' : 'Tentar novamente'}
          </Button>
        </div>
      ) : alerts.length === 0 ? (
        <FeedbackState
          type="empty"
          title="Nenhum alerta ativo"
          message="Não há alertas válidos neste momento."
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
