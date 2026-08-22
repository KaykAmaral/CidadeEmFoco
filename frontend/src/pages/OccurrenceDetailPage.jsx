import { ArrowLeft, CalendarDays, MapPin, ShieldAlert } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router'
import OccurrenceMap from '../components/map/OccurrenceMap'
import FeedbackState from '../components/ui/FeedbackState'
import StatusBadge from '../components/ui/StatusBadge'
import {
  getOccurrenceCategoryLabel,
  getOccurrenceTypeLabel,
  perceivedRiskLabels,
} from '../constants/occurrencePresentation'
import useAuth from '../hooks/useAuth'
import {
  getOccurrenceById,
  getOccurrenceImage,
} from '../services/occurrenceService'
import { formatDateTime } from '../utils/date'
import './OccurrenceDetailPage.css'

function OccurrenceDetailPage() {
  const { id } = useParams()
  const location = useLocation()
  const { logout, token } = useAuth()
  const [occurrence, setOccurrence] = useState(null)
  const [imageUrl, setImageUrl] = useState('')
  const [imageError, setImageError] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let isCurrent = true

    getOccurrenceById(token, id)
      .then((data) => {
        if (isCurrent) setOccurrence(data)
      })
      .catch((requestError) => {
        if (!isCurrent) return
        if (requestError.status === 401) {
          logout()
          return
        }
        setError(requestError.message)
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false)
      })

    return () => {
      isCurrent = false
    }
  }, [id, logout, token])

  useEffect(() => {
    if (!occurrence?.imageUrl) return undefined

    let isCurrent = true
    let objectUrl = ''

    getOccurrenceImage(token, occurrence.id)
      .then((imageBlob) => {
        if (!isCurrent) return

        objectUrl = URL.createObjectURL(imageBlob)
        setImageUrl(objectUrl)
      })
      .catch((requestError) => {
        if (!isCurrent) return
        if (requestError.status === 401) {
          logout()
          return
        }
        setImageError('Não foi possível carregar a foto desta ocorrência.')
      })

    return () => {
      isCurrent = false
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [logout, occurrence?.id, occurrence?.imageUrl, token])

  if (isLoading) {
    return <FeedbackState type="loading" message="Buscando detalhes da ocorrência..." />
  }

  if (error || !occurrence) {
    return (
      <div className="occurrence-detail-error">
        <FeedbackState
          type="error"
          title="Ocorrência não encontrada"
          message={error || 'Não foi possível localizar o registro.'}
        />
        <Link className="button button--outline" to="/app/ocorrencias">
          <ArrowLeft size={17} aria-hidden="true" />
          Voltar
        </Link>
      </div>
    )
  }

  const displayedLocation =
    occurrence.address || occurrence.neighborhood || 'Localização indicada no mapa'

  return (
    <article className="occurrence-detail">
      <Link className="back-link" to="/app/ocorrencias">
        <ArrowLeft size={17} aria-hidden="true" />
        Voltar para ocorrências
      </Link>

      {location.state?.created && (
        <div className="form-message form-message--success" role="status">
          Ocorrência registrada com sucesso.
        </div>
      )}
      {location.state?.photoWarning && (
        <div className="form-message form-message--error" role="alert">
          {location.state.photoWarning}
        </div>
      )}

      <header className="occurrence-detail__heading">
        <div>
          <span>{getOccurrenceCategoryLabel(occurrence.category)}</span>
          <h1>{getOccurrenceTypeLabel(occurrence.type)}</h1>
          <p><MapPin size={16} aria-hidden="true" />{displayedLocation}</p>
        </div>
        <StatusBadge status={occurrence.status} />
      </header>

      <div className="occurrence-detail__grid">
        <section className="occurrence-detail__card occurrence-detail__description">
          <h2>Descrição</h2>
          <p>{occurrence.description}</p>

          <div className="occurrence-detail__metadata">
            <div>
              <ShieldAlert size={19} aria-hidden="true" />
              <span>Risco percebido</span>
              <strong>{perceivedRiskLabels[occurrence.perceivedRisk]}</strong>
            </div>
            <div>
              <CalendarDays size={19} aria-hidden="true" />
              <span>Registrada em</span>
              <strong>{formatDateTime(occurrence.createdAt)}</strong>
            </div>
          </div>
        </section>

        <section className="occurrence-detail__card">
          <h2>Localização</h2>
          <OccurrenceMap occurrences={[occurrence]} />
          <p className="occurrence-detail__coordinates">
            {occurrence.latitude}, {occurrence.longitude}
          </p>
        </section>

        {occurrence.imageUrl && (
          <section className="occurrence-detail__card occurrence-detail__photo">
            <h2>Foto enviada</h2>
            {imageUrl ? (
              <img src={imageUrl} alt={`Foto da ocorrência: ${getOccurrenceTypeLabel(occurrence.type)}`} />
            ) : imageError ? (
              <p className="field-error">{imageError}</p>
            ) : (
              <FeedbackState type="loading" message="Carregando foto..." />
            )}
          </section>
        )}
      </div>
    </article>
  )
}

export default OccurrenceDetailPage
