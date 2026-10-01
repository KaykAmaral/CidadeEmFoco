import { Users } from 'lucide-react'
import './OccurrenceStrength.css'

function OccurrenceStrength({ strength = 1, compact = false }) {
  const normalizedStrength = Math.max(1, Number(strength) || 1)
  const grouped = normalizedStrength > 1

  return (
    <span className={`occurrence-strength${grouped ? ' occurrence-strength--grouped' : ''}${compact ? ' occurrence-strength--compact' : ''}`}>
      <Users size={compact ? 13 : 15} aria-hidden="true" />
      {grouped ? `Força ${normalizedStrength}` : 'Relato individual'}
    </span>
  )
}

export default OccurrenceStrength
