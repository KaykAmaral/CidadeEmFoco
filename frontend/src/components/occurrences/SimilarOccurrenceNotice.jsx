import { ExternalLink, Info } from 'lucide-react'
import { Link } from 'react-router'
import { getOccurrenceTypeLabel } from '../../constants/occurrencePresentation'

function SimilarOccurrenceNotice({ occurrence }) {
  if (!occurrence) return null
  return <aside className="similar-occurrence" role="status"><Info size={20} aria-hidden="true" /><div><strong>Encontramos uma ocorrência semelhante próxima</strong><span>{getOccurrenceTypeLabel(occurrence.type)} · {occurrence.address || occurrence.neighborhood || 'Localização no mapa'}</span><Link target="_blank" to={`/app/ocorrencias/${occurrence.id}`}>Visualizar ocorrência <ExternalLink size={14} aria-hidden="true" /></Link></div></aside>
}

export default SimilarOccurrenceNotice
