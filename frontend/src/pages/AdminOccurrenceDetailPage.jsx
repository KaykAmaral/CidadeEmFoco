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
import OccurrenceStrength from '../components/occurrences/OccurrenceStrength'
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
  getAdminOccurrenceReports,
  updateAdminOccurrenceStatus,
} from '../services/adminService'
import { formatDateTime } from '../utils/date'
import './OccurrenceDetailPage.css'
import './AdminOccurrenceDetailPage.css'

function AdminOccurrenceDetailPage() {
  const { id } = useParams()
  const { logout, token } = useAuth()
  const [occurrence, setOccurrence] = useState(null)
  const [reports, setReports] = useState([])
  const [reportsLoading, setReportsLoading] = useState(true)
  const [reportsError, setReportsError] = useState('')
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

    getAdminOccurrenceReports(token, id)
      .then((data) => {
        if (isCurrent) setReports(data)
      })
      .catch((requestError) => {
        if (!isCurrent) return
        if (requestError.status === 401) {
          logout()
          return
        }
        setReportsError(requestError.message)
      })
      .finally(() => {
        if (isCurrent) setReportsLoading(false)
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
      try {
        setReports(await getAdminOccurrenceReports(token, occurrence.id))
        setReportsError('')
      } catch (reportsRequestError) {
        setReportsError(reportsRequestError.message)
      }
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
          <OccurrenceStrength strength={occurrence.strength} />
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

      <section className="occurrence-detail__card admin-case-reports" aria-labelledby="case-reports-title">
        <div className="admin-case-reports__heading">
          <div>
            <h2 id="case-reports-title">Relatos que formam esta ocorrência</h2>
            <p>O relato principal e todas as contribuições preservadas neste agrupamento.</p>
          </div>
          <OccurrenceStrength strength={occurrence.strength} />
        </div>

        {reportsLoading && <FeedbackState type="loading" message="Buscando relatos associados..." />}
        {!reportsLoading && reportsError && (
          <FeedbackState type="error" title="Não foi possível carregar os relatos" message={reportsError} />
        )}
        {!reportsLoading && !reportsError && reports.length === 0 && (
          <FeedbackState title="Nenhum relato associado" message="Este caso ainda não possui relatos disponíveis." />
        )}
        {!reportsLoading && !reportsError && reports.length > 0 && (
          <div className="admin-case-reports__list">
            {reports.map((report, index) => (
              <article className="admin-case-report" key={report.id}>
                <div>
                  <strong>{index === 0 ? 'Relato principal' : `Relato associado #${report.id}`}</strong>
                  <span>{report.address || report.neighborhood || 'Localização indicada no mapa'}</span>
                </div>
                <p>{report.description}</p>
                <time dateTime={report.createdAt}>{formatDateTime(report.createdAt)}</time>
                <Link to={`/admin/ocorrencias/${report.id}`}>Ver relato completo</Link>
              </article>
            ))}
          </div>
        )}
      </section>
    </article>
  )
}

export default AdminOccurrenceDetailPage
