import { MapPin } from 'lucide-react'
import { Link } from 'react-router'
import { getOccurrenceTypeLabel } from '../../constants/occurrencePresentation'
import { formatDateTime } from '../../utils/date'
import StatusBadge from '../ui/StatusBadge'
import './OccurrenceCard.css'

function OccurrenceCard({ occurrence, to }) {
  const location =
    occurrence.address || occurrence.neighborhood || 'Localização no mapa'

  const content = (
    <>
      <div
        className={`occurrence-card__symbol occurrence-card__symbol--${occurrence.category.toLowerCase()}`}
        aria-hidden="true"
      >
        <MapPin size={20} />
      </div>

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
