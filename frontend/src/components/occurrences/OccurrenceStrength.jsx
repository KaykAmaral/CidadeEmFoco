import { Users } from 'lucide-react'
import { getOccurrenceGrouping } from '../../utils/occurrenceGrouping'
import './OccurrenceStrength.css'

function OccurrenceStrength({ strength = 1, compact = false }) {
  const { grouped, label } = getOccurrenceGrouping({ strength })

  return (
    <span className={`occurrence-strength${grouped ? ' occurrence-strength--grouped' : ''}${compact ? ' occurrence-strength--compact' : ''}`}>
      <Users size={compact ? 13 : 15} aria-hidden="true" />
      {label}
    </span>
  )
}

export default OccurrenceStrength
