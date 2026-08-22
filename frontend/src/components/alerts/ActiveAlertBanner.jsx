import { AlertTriangle, ArrowRight, ShieldCheck } from 'lucide-react'
import { Link } from 'react-router'
import {
  getAlertSeverityLabel,
  getAlertTypeLabel,
} from '../../constants/alertPresentation'

function ActiveAlertBanner({ alerts }) {
  if (alerts.length === 0) {
    return (
      <div className="no-alert-banner">
        <ShieldCheck size={22} aria-hidden="true" />
        <div>
          <strong>Nenhum alerta climático ativo</strong>
          <span>Continue acompanhando as atualizações da cidade.</span>
        </div>
      </div>
    )
  }

  const alert = alerts[0]

  return (
    <article
      className={`active-alert active-alert--${alert.severity.toLowerCase()}`}
    >
      <AlertTriangle size={28} aria-hidden="true" />
      <div className="active-alert__content">
        <div className="active-alert__meta">
          <span>{getAlertTypeLabel(alert.type)}</span>
          <span>Severidade {getAlertSeverityLabel(alert.severity)}</span>
        </div>
        <h2>{alert.title}</h2>
        <p>{alert.description}</p>
        <small>{alert.disclaimer}</small>
      </div>
      <Link to="/app/alertas">
        Ver {alerts.length > 1 ? `${alerts.length} alertas` : 'detalhes'}
        <ArrowRight size={17} aria-hidden="true" />
      </Link>
    </article>
  )
}

export default ActiveAlertBanner
