import {
  AlertCircle,
  CalendarClock,
  CheckCircle2,
  LoaderCircle,
  Plus,
  Power,
  PowerOff,
  RotateCw,
  X,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import {
  alertSeverityLabels,
  alertTypeLabels,
  getAlertSeverityLabel,
  getAlertTypeLabel,
} from '../constants/alertPresentation'
import useAuth from '../hooks/useAuth'
import {
  activateAdminAlert,
  createAdminAlert,
  deactivateAdminAlert,
  getAdminAlerts,
} from '../services/adminService'
import { formatDateTime } from '../utils/date'
import './AdminAlertsPage.css'

const initialForm = {
  title: '',
  type: '',
  severity: '',
  description: '',
  startAt: '',
  endAt: '',
}

function getPeriodState(alert) {
  const now = Date.now()
  if (new Date(alert.endAt).getTime() < now) return 'Encerrado'
  if (new Date(alert.startAt).getTime() > now) return 'Agendado'
  return 'Vigente'
}

function AdminAlertsPage() {
  const { logout, token } = useAuth()
  const [alerts, setAlerts] = useState([])
  const [form, setForm] = useState(initialForm)
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [changingId, setChangingId] = useState(null)
  const [error, setError] = useState('')
  const [formError, setFormError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [successMessage, setSuccessMessage] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let isCurrent = true

    getAdminAlerts(token)
      .then((data) => {
        if (isCurrent) setAlerts(data)
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
  }, [logout, reloadKey, token])

  function handleChange(event) {
    const { name, value } = event.target
    setForm((currentForm) => ({ ...currentForm, [name]: value }))
    setFormError('')
    setFieldErrors((currentErrors) => ({ ...currentErrors, [name]: undefined }))
  }

  function closeForm() {
    if (isSubmitting) return
    setIsFormOpen(false)
    setForm(initialForm)
    setFormError('')
    setFieldErrors({})
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError('')
    setFieldErrors({})
    setSuccessMessage('')

    if (new Date(form.endAt) <= new Date(form.startAt)) {
      setFormError('O fim do alerta deve ser posterior ao início.')
      return
    }

    setIsSubmitting(true)

    try {
      const createdAlert = await createAdminAlert(token, {
        ...form,
        startAt: new Date(form.startAt).toISOString(),
        endAt: new Date(form.endAt).toISOString(),
      })
      setAlerts((currentAlerts) => [createdAlert, ...currentAlerts])
      setIsFormOpen(false)
      setForm(initialForm)
      setFormError('')
      setFieldErrors({})
      setSuccessMessage(
        'Alerta criado como inativo. Ative-o quando estiver pronto para publicação.',
      )
    } catch (requestError) {
      if (requestError.status === 401) {
        logout()
        return
      }
      setFormError(requestError.message)
      setFieldErrors(requestError.fieldErrors ?? {})
    } finally {
      setIsSubmitting(false)
    }
  }

  async function toggleAlert(alert) {
    setChangingId(alert.id)
    setError('')
    setSuccessMessage('')

    try {
      const updatedAlert = alert.active
        ? await deactivateAdminAlert(token, alert.id)
        : await activateAdminAlert(token, alert.id)

      setAlerts((currentAlerts) =>
        currentAlerts.map((item) =>
          item.id === updatedAlert.id ? updatedAlert : item,
        ),
      )
      setSuccessMessage(
        updatedAlert.active
          ? `Alerta “${updatedAlert.title}” ativado.`
          : `Alerta “${updatedAlert.title}” desativado.`,
      )
    } catch (requestError) {
      if (requestError.status === 401) {
        logout()
        return
      }
      setError(requestError.message)
    } finally {
      setChangingId(null)
    }
  }

  function retry() {
    setError('')
    setIsLoading(true)
    setReloadKey((currentKey) => currentKey + 1)
  }

  const activeCount = alerts.filter((alert) => alert.active).length

  return (
    <section className="admin-alerts-page">
      <header className="admin-page-heading">
        <div>
          <span>Administração</span>
          <h1>Alertas climáticos</h1>
          <p>Cadastre e controle os alertas demonstrativos exibidos aos cidadãos.</p>
        </div>
        <Button onClick={() => setIsFormOpen(true)}>
          <Plus size={18} aria-hidden="true" />
          Novo alerta
        </Button>
      </header>

      <div className="admin-alerts-summary">
        <div><strong>{alerts.length}</strong><span>cadastrados</span></div>
        <div><strong>{activeCount}</strong><span>marcados como ativos</span></div>
        <p><AlertCircle size={18} aria-hidden="true" />Ativar um alerta não altera seu período de validade.</p>
      </div>

      {successMessage && (
        <div className="form-message form-message--success" role="status">
          <CheckCircle2 size={18} aria-hidden="true" />
          {successMessage}
        </div>
      )}

      {isFormOpen && (
        <form className="admin-alert-form" onSubmit={handleSubmit}>
          <div className="admin-alert-form__heading">
            <div><strong>Novo alerta</strong><p>O cadastro será salvo inicialmente como inativo.</p></div>
            <button aria-label="Fechar formulário" onClick={closeForm} type="button"><X size={20} /></button>
          </div>

          {formError && <div className="form-message form-message--error" role="alert">{formError}</div>}

          <div className="form-grid">
            <div className="form-field form-field--wide">
              <label htmlFor="alert-title">Título</label>
              <input aria-invalid={Boolean(fieldErrors.title)} className="form-control" id="alert-title" maxLength={150} name="title" onChange={handleChange} placeholder="Ex.: Alerta de chuva intensa" required value={form.title} />
              {fieldErrors.title && <small className="field-error">{fieldErrors.title}</small>}
            </div>
            <div className="form-field">
              <label htmlFor="alert-type">Tipo</label>
              <select aria-invalid={Boolean(fieldErrors.type)} className="form-control" id="alert-type" name="type" onChange={handleChange} required value={form.type}>
                <option disabled value="">Selecione</option>
                {Object.entries(alertTypeLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
              {fieldErrors.type && <small className="field-error">{fieldErrors.type}</small>}
            </div>
            <div className="form-field">
              <label htmlFor="alert-severity">Severidade</label>
              <select aria-invalid={Boolean(fieldErrors.severity)} className="form-control" id="alert-severity" name="severity" onChange={handleChange} required value={form.severity}>
                <option disabled value="">Selecione</option>
                {Object.entries(alertSeverityLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
              {fieldErrors.severity && <small className="field-error">{fieldErrors.severity}</small>}
            </div>
            <div className="form-field">
              <label htmlFor="alert-start">Início da validade</label>
              <input aria-invalid={Boolean(fieldErrors.startAt)} className="form-control" id="alert-start" name="startAt" onChange={handleChange} required type="datetime-local" value={form.startAt} />
              {fieldErrors.startAt && <small className="field-error">{fieldErrors.startAt}</small>}
            </div>
            <div className="form-field">
              <label htmlFor="alert-end">Fim da validade</label>
              <input aria-invalid={Boolean(fieldErrors.endAt)} className="form-control" id="alert-end" name="endAt" onChange={handleChange} required type="datetime-local" value={form.endAt} />
              {fieldErrors.endAt && <small className="field-error">{fieldErrors.endAt}</small>}
            </div>
            <div className="form-field form-field--wide">
              <label htmlFor="alert-description">Descrição</label>
              <textarea aria-invalid={Boolean(fieldErrors.description)} className="form-control" id="alert-description" maxLength={2000} name="description" onChange={handleChange} placeholder="Descreva o alerta e as orientações relevantes..." required value={form.description} />
              <small>{form.description.length}/2000 caracteres</small>
              {fieldErrors.description && <small className="field-error">{fieldErrors.description}</small>}
            </div>
          </div>

          <div className="admin-alert-form__actions">
            <Button disabled={isSubmitting} onClick={closeForm} variant="outline">Cancelar</Button>
            <Button disabled={isSubmitting} type="submit">
              {isSubmitting && <LoaderCircle className="button__spinner" size={17} aria-hidden="true" />}
              {isSubmitting ? 'Salvando...' : 'Criar alerta'}
            </Button>
          </div>
        </form>
      )}

      {isLoading ? (
        <FeedbackState type="loading" message="Buscando alertas cadastrados..." />
      ) : error ? (
        <div className="admin-page-error">
          <FeedbackState type="error" message={error} />
          <Button onClick={retry} variant="outline"><RotateCw size={17} />Tentar novamente</Button>
        </div>
      ) : alerts.length === 0 ? (
        <FeedbackState type="empty" title="Nenhum alerta cadastrado" message="Crie o primeiro alerta demonstrativo do sistema." />
      ) : (
        <div className="admin-alerts-list">
          {alerts.map((alert) => (
            <article className="admin-alert-card" key={alert.id}>
              <div className="admin-alert-card__main">
                <div className="admin-alert-card__badges">
                  <span className={`admin-alert-card__state admin-alert-card__state--${alert.active ? 'active' : 'inactive'}`}>{alert.active ? 'Ativo' : 'Inativo'}</span>
                  <span>{getAlertTypeLabel(alert.type)}</span>
                  <span>Severidade {getAlertSeverityLabel(alert.severity)}</span>
                  <span>{getPeriodState(alert)}</span>
                </div>
                <h2>{alert.title}</h2>
                <p>{alert.description}</p>
                <div className="admin-alert-card__period">
                  <CalendarClock size={17} aria-hidden="true" />
                  <span>{formatDateTime(alert.startAt)} até {formatDateTime(alert.endAt)}</span>
                </div>
              </div>
              <Button disabled={changingId === alert.id} onClick={() => toggleAlert(alert)} variant={alert.active ? 'outline' : 'primary'}>
                {changingId === alert.id ? <LoaderCircle className="button__spinner" size={17} /> : alert.active ? <PowerOff size={17} /> : <Power size={17} />}
                {changingId === alert.id ? 'Atualizando...' : alert.active ? 'Desativar' : 'Ativar'}
              </Button>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}

export default AdminAlertsPage
