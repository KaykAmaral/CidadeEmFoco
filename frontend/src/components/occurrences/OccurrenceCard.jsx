import { MapPin } from 'lucide-react'
import { getOccurrenceTypeLabel } from '../../constants/occurrencePresentation'
import { formatDateTime } from '../../utils/date'
import StatusBadge from '../ui/StatusBadge'

function OccurrenceCard({ occurrence }) {
  const location =
    occurrence.address || occurrence.neighborhood || 'Localização no mapa'

  return (
    <article className="occurrence-card">
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
    </article>
  )
}

export default OccurrenceCard
