import {
  ArrowLeft,
  CalendarDays,
  LoaderCircle,
  MapPin,
  RefreshCw,
  ShieldAlert,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import OccurrenceMap from '../components/map/OccurrenceMap'
import OccurrenceTypeIcon from '../components/occurrences/OccurrenceTypeIcon'
import OccurrenceImageGallery from '../components/occurrences/OccurrenceImageGallery'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import StatusBadge from '../components/ui/StatusBadge'
import occurrenceStatusConfig from '../constants/occurrenceStatus'
import {
  getOccurrenceCategoryLabel,
  getOccurrenceTypeLabel,
  perceivedRiskLabels,
} from '../constants/occurrencePresentation'
import useAuth from '../hooks/useAuth'
import {
  getAdminOccurrenceById,
  updateAdminOccurrenceStatus,
} from '../services/adminService'
import { formatDateTime } from '../utils/date'
import './OccurrenceDetailPage.css'
import './AdminOccurrenceDetailPage.css'

function AdminOccurrenceDetailPage() {
  const { id } = useParams()
  const { logout, token } = useAuth()
  const [occurrence, setOccurrence] = useState(null)
  const [selectedStatus, setSelectedStatus] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isUpdating, setIsUpdating] = useState(false)
  const [error, setError] = useState('')
  const [updateError, setUpdateError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  useEffect(() => {
    let isCurrent = true

    getAdminOccurrenceById(token, id)
      .then((data) => {
        if (isCurrent) {
          setOccurrence(data)
          setSelectedStatus(data.status)
        }
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

  async function handleStatusUpdate(event) {
    event.preventDefault()
    setUpdateError('')
    setSuccessMessage('')
    setIsUpdating(true)

    try {
      const updatedOccurrence = await updateAdminOccurrenceStatus(
        token,
        occurrence.id,
        selectedStatus,
      )
      setOccurrence(updatedOccurrence)
      setSelectedStatus(updatedOccurrence.status)
      setSuccessMessage('Status atualizado com sucesso.')
    } catch (requestError) {
      if (requestError.status === 401) {
        logout()
        return
      }
      setUpdateError(requestError.message)
    } finally {
      setIsUpdating(false)
    }
  }

  if (isLoading) {
    return <FeedbackState type="loading" message="Buscando ocorrência..." />
  }

  if (error || !occurrence) {
    return (
      <div className="admin-page-error">
        <FeedbackState type="error" title="Ocorrência não encontrada" message={error} />
        <Link className="button button--outline" to="/admin/ocorrencias">
          <ArrowLeft size={17} aria-hidden="true" />Voltar
        </Link>
      </div>
    )
  }

  const displayedLocation =
    occurrence.address || occurrence.neighborhood || 'Localização indicada no mapa'

  return (
    <article className="occurrence-detail admin-occurrence-detail">
      <Link className="back-link" to="/admin/ocorrencias">
        <ArrowLeft size={17} aria-hidden="true" />Voltar para ocorrências
      </Link>

      <header className="occurrence-detail__heading">
        <OccurrenceTypeIcon className="occurrence-detail__type-icon" size={28} type={occurrence.type} />
        <div className="occurrence-detail__heading-content">
          <span>{getOccurrenceCategoryLabel(occurrence.category)} · #{occurrence.id}</span>
          <h1>{getOccurrenceTypeLabel(occurrence.type)}</h1>
          <p><MapPin size={16} aria-hidden="true" />{displayedLocation}</p>
        </div>
        <StatusBadge status={occurrence.status} />
      </header>

      <section className="admin-status-panel" aria-labelledby="status-title">
        <div>
          <RefreshCw size={23} aria-hidden="true" />
          <div><h2 id="status-title">Atualizar status</h2><p>Escolha a situação atual desta ocorrência.</p></div>
        </div>

        <form onSubmit={handleStatusUpdate}>
          <div className="form-field">
            <label htmlFor="admin-occurrence-status">Novo status</label>
            <select className="form-control" id="admin-occurrence-status" onChange={(event) => { setSelectedStatus(event.target.value); setSuccessMessage(''); setUpdateError('') }} value={selectedStatus}>
              {Object.entries(occurrenceStatusConfig).map(([value, config]) => <option key={value} value={value}>{config.label}</option>)}
            </select>
          </div>
          <Button disabled={isUpdating || selectedStatus === occurrence.status} type="submit">
            {isUpdating && <LoaderCircle className="button__spinner" size={17} aria-hidden="true" />}
            {isUpdating ? 'Atualizando...' : 'Salvar status'}
          </Button>
        </form>

        {successMessage && <div className="form-message form-message--success" role="status">{successMessage}</div>}
        {updateError && <div className="form-message form-message--error" role="alert">{updateError}</div>}
      </section>

      <div className="occurrence-detail__grid">
        <section className="occurrence-detail__card occurrence-detail__description">
          <h2>Descrição do cidadão</h2>
          <p>{occurrence.description}</p>
          <div className="occurrence-detail__metadata">
            <div><ShieldAlert size={19} aria-hidden="true" /><span>Risco percebido</span><strong>{perceivedRiskLabels[occurrence.perceivedRisk]}</strong></div>
            <div><CalendarDays size={19} aria-hidden="true" /><span>Registrada em</span><strong>{formatDateTime(occurrence.createdAt)}</strong></div>
            <div><RefreshCw size={19} aria-hidden="true" /><span>Atualizada em</span><strong>{formatDateTime(occurrence.updatedAt)}</strong></div>
          </div>
        </section>

        <section className="occurrence-detail__card">
          <h2>Localização</h2>
          <OccurrenceMap occurrences={[occurrence]} />
          <p className="occurrence-detail__coordinates">{occurrence.latitude}, {occurrence.longitude}</p>
        </section>

        {(occurrence.imageUrls?.length > 0 || occurrence.imageUrl) && (
          <section className="occurrence-detail__card occurrence-detail__photo">
            <h2>Imagens enviadas pelo cidadão</h2>
            <OccurrenceImageGallery occurrence={occurrence} onUnauthorized={logout} token={token} />
          </section>
        )}
      </div>
    </article>
  )
}

export default AdminOccurrenceDetailPage
