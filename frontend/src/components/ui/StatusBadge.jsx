import occurrenceStatusConfig from '../../constants/occurrenceStatus'

function StatusBadge({ status }) {
  const config = occurrenceStatusConfig[status] ?? {
    label: status,
    tone: 'gray',
  }

  return (
    <span className={`status-badge status-badge--${config.tone}`}>
      {config.label}
    </span>
  )
}

export default StatusBadge
