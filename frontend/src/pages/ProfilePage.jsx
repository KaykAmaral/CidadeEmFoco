import {
  CalendarDays,
  CheckCircle2,
  ClipboardList,
  Clock3,
  LogOut,
  Mail,
  MessageCircle,
  Pencil,
  Plus,
  RotateCw,
  Save,
  ShieldCheck,
  UserRound,
  X,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import OccurrenceCard from '../components/occurrences/OccurrenceCard'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import useAuth from '../hooks/useAuth'
import { getMyOccurrences } from '../services/occurrenceService'
import {
  getWhatsappPreferences,
  removeWhatsappPreferences,
  updateWhatsappPreferences,
} from '../services/whatsappService'
import { formatDateTime } from '../utils/date'
import { preferencesToForm, whatsappPreferencePayload } from '../utils/whatsappPreferences'
import './ProfilePage.css'

function getInitials(name) {
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase()
}

function ProfilePage() {
  const { logout, token, user } = useAuth()
  const [occurrences, setOccurrences] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [preferences, setPreferences] = useState(null)
  const [preferenceForm, setPreferenceForm] = useState(preferencesToForm())
  const [preferenceError, setPreferenceError] = useState('')
  const [preferenceFieldErrors, setPreferenceFieldErrors] = useState({})
  const [preferenceSuccess, setPreferenceSuccess] = useState('')
  const [isPreferencesLoading, setIsPreferencesLoading] = useState(true)
  const [isEditingPreferences, setIsEditingPreferences] = useState(false)
  const [isSavingPreferences, setIsSavingPreferences] = useState(false)

  useEffect(() => {
    let isCurrent = true

    getMyOccurrences(token)
      .then((data) => {
        if (isCurrent) setOccurrences(data)
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

  useEffect(() => {
    let isCurrent = true

    getWhatsappPreferences(token)
      .then((data) => {
        if (!isCurrent) return
        setPreferences(data)
        setPreferenceForm(preferencesToForm(data))
      })
      .catch((requestError) => {
        if (!isCurrent) return
        if (requestError.status === 401) {
          logout()
          return
        }
        setPreferenceError(requestError.message)
      })
      .finally(() => {
        if (isCurrent) setIsPreferencesLoading(false)
      })

    return () => {
      isCurrent = false
    }
  }, [logout, token])

  function startEditingPreferences() {
    setPreferenceForm(preferencesToForm(preferences))
    setPreferenceError('')
    setPreferenceFieldErrors({})
    setPreferenceSuccess('')
    setIsEditingPreferences(true)
  }

  function cancelEditingPreferences() {
    setPreferenceForm(preferencesToForm(preferences))
    setPreferenceError('')
    setPreferenceFieldErrors({})
    setIsEditingPreferences(false)
  }

  function handlePreferenceChange(event) {
    const { checked, name, type, value } = event.target
    setPreferenceForm((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value,
    }))
    setPreferenceError('')
    setPreferenceFieldErrors((current) => ({ ...current, [name]: undefined }))
    setPreferenceSuccess('')
  }

  async function handlePreferenceSubmit(event) {
    event.preventDefault()
    if (isSavingPreferences) return

    setIsSavingPreferences(true)
    setPreferenceError('')
    setPreferenceFieldErrors({})
    setPreferenceSuccess('')

    try {
      const payload = whatsappPreferencePayload(preferenceForm)
      const updatedPreferences = payload.consentGiven
        ? await updateWhatsappPreferences(token, payload.phoneNumber, payload.consentGiven)
        : await removeWhatsappPreferences(token).then(() => ({
            phoneNumber: null,
            notificationsEnabled: false,
            consentAt: null,
            deliveryEnabled: preferences?.deliveryEnabled ?? false,
          }))
      setPreferences(updatedPreferences)
      setPreferenceForm(preferencesToForm(updatedPreferences))
      setPreferenceSuccess(
        updatedPreferences.notificationsEnabled
          ? 'Preferências de WhatsApp atualizadas.'
          : 'Alertas por WhatsApp desativados.',
      )
      setIsEditingPreferences(false)
    } catch (requestError) {
      if (requestError.status === 401) {
        logout()
        return
      }
      const requestFieldErrors = requestError.fieldErrors ?? {}
      setPreferenceFieldErrors(requestFieldErrors)
      setPreferenceError(Object.keys(requestFieldErrors).length ? '' : requestError.message)
    } finally {
      setIsSavingPreferences(false)
    }
  }

  function retry() {
    setError('')
    setIsLoading(true)
    setReloadKey((currentKey) => currentKey + 1)
  }

  const resolvedCount = occurrences.filter(
    (occurrence) => occurrence.status === 'RESOLVIDA',
  ).length
  const inProgressCount = occurrences.filter((occurrence) =>
    ['EM_ANALISE', 'EM_ATENDIMENTO'].includes(occurrence.status),
  ).length
  const recentOccurrences = occurrences.slice(0, 3)

  return (
    <section className="profile-page">
      <header className="profile-page__heading">
        <span>Conta</span>
        <h1>Meu perfil</h1>
        <p>Consulte seus dados e acompanhe sua participação no Cidade em Foco.</p>
      </header>

      <div className="profile-grid">
        <article className="profile-card">
          <div className="profile-card__identity">
            <div className="profile-avatar" aria-hidden="true">
              {getInitials(user.name)}
            </div>
            <div>
              <span>Cidadão</span>
              <h2>{user.name}</h2>
              <p>Morador colaborador da plataforma</p>
            </div>
          </div>

          <dl className="profile-details">
            <div>
              <dt><Mail size={17} aria-hidden="true" />E-mail</dt>
              <dd>{user.email}</dd>
            </div>
            <div>
              <dt><ShieldCheck size={17} aria-hidden="true" />Perfil de acesso</dt>
              <dd>Cidadão</dd>
            </div>
            <div>
              <dt><CalendarDays size={17} aria-hidden="true" />Cadastro realizado em</dt>
              <dd>{formatDateTime(user.createdAt)}</dd>
            </div>
          </dl>

          <section className="profile-preferences" aria-labelledby="whatsapp-preferences-title">
            <div className="profile-preferences__heading">
              <div>
                <span>Comunicação</span>
                <h3 id="whatsapp-preferences-title">Alertas por WhatsApp</h3>
              </div>
              {!isPreferencesLoading && !isEditingPreferences && (
                <Button onClick={startEditingPreferences} variant="outline">
                  <Pencil size={16} aria-hidden="true" />
                  Editar perfil
                </Button>
              )}
            </div>

            {isPreferencesLoading ? (
              <FeedbackState type="loading" message="Consultando preferências..." />
            ) : isEditingPreferences ? (
              <form className="profile-preferences__form" onSubmit={handlePreferenceSubmit} aria-busy={isSavingPreferences}>
                {preferenceError && <div className="form-message form-message--error" role="alert">{preferenceError}</div>}
                <div className="form-field">
                  <label htmlFor="profile-whatsapp">Número do WhatsApp</label>
                  <input
                    aria-describedby={preferenceFieldErrors.phoneNumber ? 'profile-whatsapp-error' : 'profile-whatsapp-help'}
                    aria-invalid={Boolean(preferenceFieldErrors.phoneNumber)}
                    className="form-control"
                    disabled={!preferenceForm.consentGiven || isSavingPreferences}
                    id="profile-whatsapp"
                    maxLength={30}
                    name="phoneNumber"
                    onChange={handlePreferenceChange}
                    placeholder="+5513999999999"
                    required={preferenceForm.consentGiven}
                    type="tel"
                    value={preferenceForm.phoneNumber}
                  />
                  {preferenceFieldErrors.phoneNumber ? (
                    <small className="field-error" id="profile-whatsapp-error">{preferenceFieldErrors.phoneNumber}</small>
                  ) : (
                    <small id="profile-whatsapp-help">Use o formato internacional, por exemplo: +5513999999999.</small>
                  )}
                </div>
                <label className="profile-consent">
                  <input
                    aria-describedby={preferenceFieldErrors.consentGiven ? 'profile-consent-error' : undefined}
                    aria-invalid={Boolean(preferenceFieldErrors.consentGiven)}
                    checked={preferenceForm.consentGiven}
                    disabled={isSavingPreferences}
                    name="consentGiven"
                    onChange={handlePreferenceChange}
                    type="checkbox"
                  />
                  <span>Autorizo o recebimento de alertas climáticos pelo WhatsApp.</span>
                </label>
                {preferenceFieldErrors.consentGiven && (
                  <small className="field-error" id="profile-consent-error">{preferenceFieldErrors.consentGiven}</small>
                )}
                <div className="profile-preferences__actions">
                  <Button disabled={isSavingPreferences} type="submit">
                    <Save size={16} aria-hidden="true" />
                    {isSavingPreferences ? 'Salvando...' : 'Salvar'}
                  </Button>
                  <Button disabled={isSavingPreferences} onClick={cancelEditingPreferences} type="button" variant="outline">
                    <X size={16} aria-hidden="true" />
                    Cancelar
                  </Button>
                </div>
              </form>
            ) : preferenceError ? (
              <div className="form-message form-message--error" role="alert">{preferenceError}</div>
            ) : (
              <dl className="profile-preferences__summary">
                <div>
                  <dt><MessageCircle size={16} aria-hidden="true" />Recebimento</dt>
                  <dd>{preferences?.notificationsEnabled ? 'Autorizado' : 'Desativado'}</dd>
                </div>
                {preferences?.phoneNumber && <div><dt>Número</dt><dd>{preferences.phoneNumber}</dd></div>}
                {preferences?.consentAt && <div><dt>Consentimento registrado em</dt><dd>{formatDateTime(preferences.consentAt)}</dd></div>}
                <div><dt>Serviço de entrega</dt><dd>{preferences?.deliveryEnabled ? 'Disponível' : 'Indisponível no momento'}</dd></div>
              </dl>
            )}

            {preferenceSuccess && <div className="form-message form-message--success" role="status">{preferenceSuccess}</div>}
          </section>

          <div className="profile-card__notice">
            <UserRound size={19} aria-hidden="true" />
            <p>Nome, e-mail e demais dados cadastrais são somente para consulta.</p>
          </div>

          <Button onClick={logout} variant="outline">
            <LogOut size={17} aria-hidden="true" />
            Sair da conta
          </Button>
        </article>

        <div className="profile-activity">
          <div className="profile-activity__heading">
            <div>
              <span>Minha participação</span>
              <h2>Resumo de ocorrências</h2>
            </div>
            <Link className="button button--primary" to="/app/ocorrencias/nova">
              <Plus size={17} aria-hidden="true" />
              Registrar
            </Link>
          </div>

          {isLoading ? (
            <FeedbackState type="loading" message="Buscando suas ocorrências..." />
          ) : error ? (
            <div className="profile-activity__error">
              <FeedbackState type="error" message={error} />
              <Button onClick={retry} variant="outline">
                <RotateCw size={17} aria-hidden="true" />
                Tentar novamente
              </Button>
            </div>
          ) : (
            <>
              <div className="profile-stats">
                <div>
                  <ClipboardList size={21} aria-hidden="true" />
                  <strong>{occurrences.length}</strong>
                  <span>Total registrado</span>
                </div>
                <div>
                  <Clock3 size={21} aria-hidden="true" />
                  <strong>{inProgressCount}</strong>
                  <span>Em análise ou atendimento</span>
                </div>
                <div>
                  <CheckCircle2 size={21} aria-hidden="true" />
                  <strong>{resolvedCount}</strong>
                  <span>Resolvidas</span>
                </div>
              </div>

              <div className="profile-recent">
                <div className="profile-recent__heading">
                  <h3>Registros recentes</h3>
                  <Link state={{ mode: 'mine' }} to="/app/ocorrencias">
                    Ver todas
                  </Link>
                </div>

                {recentOccurrences.length === 0 ? (
                  <FeedbackState
                    type="empty"
                    title="Você ainda não registrou ocorrências"
                    message="Use o botão Registrar para enviar sua primeira contribuição."
                  />
                ) : (
                  <div className="profile-recent__list">
                    {recentOccurrences.map((occurrence) => (
                      <OccurrenceCard
                        key={occurrence.id}
                        occurrence={occurrence}
                        to={`/app/ocorrencias/${occurrence.id}`}
                      />
                    ))}
                  </div>
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </section>
  )
}

export default ProfilePage
