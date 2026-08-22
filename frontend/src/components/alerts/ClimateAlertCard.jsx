import {
  AlertCircle,
  CalendarClock,
  CloudRain,
  Mountain,
  Waves,
  Wind,
} from 'lucide-react'
import {
  getAlertSeverityLabel,
  getAlertTypeLabel,
} from '../../constants/alertPresentation'
import { formatDateTime } from '../../utils/date'
import './ClimateAlertCard.css'

const alertIcons = {
  CHUVA_INTENSA: CloudRain,
  ALAGAMENTO: Waves,
  VENTOS_FORTES: Wind,
  RESSACA_MARITIMA: Waves,
  DESLIZAMENTO: Mountain,
  OUTRO: AlertCircle,
}

function ClimateAlertCard({ alert }) {
  const Icon = alertIcons[alert.type] ?? AlertCircle
  const severityClass = alert.severity.toLowerCase()

  return (
    <article className={`climate-alert-card climate-alert-card--${severityClass}`}>
      <div className="climate-alert-card__icon">
        <Icon size={26} aria-hidden="true" />
      </div>

      <div className="climate-alert-card__content">
        <div className="climate-alert-card__meta">
          <span>{getAlertTypeLabel(alert.type)}</span>
          <span>Severidade {getAlertSeverityLabel(alert.severity)}</span>
        </div>

        <h2>{alert.title}</h2>
        <p>{alert.description}</p>

        <div className="climate-alert-card__validity">
          <CalendarClock size={17} aria-hidden="true" />
          <div>
            <span>Válido de {formatDateTime(alert.startAt)}</span>
            <span>até {formatDateTime(alert.endAt)}</span>
          </div>
        </div>

        <small>{alert.disclaimer}</small>
      </div>
    </article>
  )
}

export default ClimateAlertCard
