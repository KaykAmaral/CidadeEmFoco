import { getOccurrenceTypePresentation } from '../../constants/occurrencePresentation'
import './OccurrenceTypeIcon.css'

function OccurrenceTypeIcon({ type, size = 20, variant = 'soft', className = '' }) {
  const { icon: Icon, color, softColor } = getOccurrenceTypePresentation(type)
  const style = variant === 'solid'
    ? { '--occurrence-icon-color': 'white', '--occurrence-icon-bg': color }
    : { '--occurrence-icon-color': color, '--occurrence-icon-bg': softColor }
  return (
    <span className={`occurrence-type-icon occurrence-type-icon--${variant} ${className}`.trim()} style={style} aria-hidden="true">
      <Icon size={size} strokeWidth={2.2} />
    </span>
  )
}

export default OccurrenceTypeIcon
