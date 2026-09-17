import { Link } from 'react-router'
import { getOccurrenceTypeLabel } from '../../constants/occurrencePresentation'
import { formatDateTime } from '../../utils/date'
import StatusBadge from '../ui/StatusBadge'
import OccurrenceTypeIcon from './OccurrenceTypeIcon'
import './OccurrenceCard.css'

function OccurrenceCard({ occurrence, to }) {
  const location =
    occurrence.address || occurrence.neighborhood || 'Localização no mapa'

  const content = (
    <>
      <OccurrenceTypeIcon className="occurrence-card__symbol" type={occurrence.type} />

      <div className="occurrence-card__content">
        <strong>{getOccurrenceTypeLabel(occurrence.type)}</strong>
        <span>{location}</span>
        <time dateTime={occurrence.createdAt}>
          {formatDateTime(occurrence.createdAt)}
        </time>
      </div>

      <StatusBadge status={occurrence.status} />
    </>
  )

  if (to) {
    return (
      <Link className="occurrence-card occurrence-card--link" to={to}>
        {content}
      </Link>
    )
  }

  return <article className="occurrence-card">{content}</article>
}

export default OccurrenceCard
