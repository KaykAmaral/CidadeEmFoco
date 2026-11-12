import { CalendarDays, MapPin, RotateCw } from 'lucide-react'
import { useEffect, useState } from 'react'
import { getAdminOccurrenceReports } from '../../services/adminService'
import { formatDateTime } from '../../utils/date'
import Button from '../ui/Button'
import FeedbackState from '../ui/FeedbackState'
import OccurrenceImageGallery from './OccurrenceImageGallery'

function AssociatedOccurrenceReports({ occurrence, onUnauthorized, token }) {
  const [reports, setReports] = useState([])
  const [isLoading, setIsLoading] = useState(occurrence.strength > 1)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    if (occurrence.strength <= 1) return undefined
    let isCurrent = true
    setIsLoading(true)
    setError('')

    getAdminOccurrenceReports(token, occurrence.id)
      .then((data) => {
        if (isCurrent) setReports(Array.isArray(data) ? data : [])
      })
      .catch((requestError) => {
        if (!isCurrent) return
        if (requestError.status === 401) onUnauthorized()
        else setError(requestError.message)
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false)
      })

    return () => { isCurrent = false }
  }, [occurrence.id, occurrence.strength, onUnauthorized, reloadKey, token])

  if (isLoading) return <FeedbackState type="loading" message="Buscando relatos associados..." />
  if (error) return (
    <div className="associated-reports__error">
      <FeedbackState type="error" message={error} />
      <Button onClick={() => setReloadKey((key) => key + 1)} variant="outline"><RotateCw size={16} />Tentar novamente</Button>
    </div>
  )
  if (reports.length <= 1) return (
    <FeedbackState type="empty" title="Ocorrência individual" message="Não há outros relatos associados a esta ocorrência." />
  )

  return (
    <div className="associated-reports__list">
      {reports.map((report, index) => (
        <article className="associated-report" key={report.id}>
          <header><strong>Relato {index + 1}</strong><span>#{report.id}</span></header>
          <p>{report.description}</p>
          <dl>
            <div><dt><CalendarDays size={14} aria-hidden="true" />Registrado em</dt><dd>{formatDateTime(report.createdAt)}</dd></div>
            <div><dt><MapPin size={14} aria-hidden="true" />Local</dt><dd>{report.address || report.neighborhood || 'Localização indicada no mapa'}</dd></div>
          </dl>
          {(report.imageUrls?.length > 0 || report.imageUrl) && (
            <OccurrenceImageGallery occurrence={report} onUnauthorized={onUnauthorized} token={token} />
          )}
        </article>
      ))}
    </div>
  )
}

export default AssociatedOccurrenceReports
