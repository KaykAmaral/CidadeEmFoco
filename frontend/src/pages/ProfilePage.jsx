import {
  CalendarDays,
  CheckCircle2,
  ClipboardList,
  Clock3,
  LogOut,
  Mail,
  MessageCircle,
  LoaderCircle,
  Plus,
  RotateCw,
  ShieldCheck,
  UserRound,
  Trash2,
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
  const [whatsappPreferences, setWhatsappPreferences] = useState(null)
  const [whatsappPhone, setWhatsappPhone] = useState('')
  const [whatsappConsent, setWhatsappConsent] = useState(false)
  const [whatsappLoading, setWhatsappLoading] = useState(true)
  const [whatsappSaving, setWhatsappSaving] = useState(false)
  const [whatsappError, setWhatsappError] = useState('')
  const [whatsappMessage, setWhatsappMessage] = useState('')

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
        setWhatsappPreferences(data)
        setWhatsappPhone(data.phoneNumber ?? '')
      })
      .catch((requestError) => {
        if (!isCurrent) return
        if (requestError.status === 401) return logout()
        setWhatsappError(requestError.message)
      })
      .finally(() => {
        if (isCurrent) setWhatsappLoading(false)
      })
    return () => { isCurrent = false }
  }, [logout, token])

  async function saveWhatsappPreferences(event) {
    event.preventDefault()
    setWhatsappError('')
    setWhatsappMessage('')
    setWhatsappSaving(true)
    try {
      const data = await updateWhatsappPreferences(token, whatsappPhone, whatsappConsent)
      setWhatsappPreferences(data)
      setWhatsappPhone(data.phoneNumber ?? '')
      setWhatsappConsent(false)
      setWhatsappMessage('Preferências de WhatsApp atualizadas com sucesso.')
    } catch (requestError) {
      if (requestError.status === 401) return logout()
      setWhatsappError(requestError.fieldErrors?.phoneNumber ?? requestError.fieldErrors?.consentGiven ?? requestError.message)
    } finally {
      setWhatsappSaving(false)
    }
  }

  async function removeWhatsapp() {
    setWhatsappError('')
    setWhatsappMessage('')
    setWhatsappSaving(true)
    try {
      await removeWhatsappPreferences(token)
      setWhatsappPreferences((current) => ({
        phoneNumber: null,
        notificationsEnabled: false,
        consentAt: null,
        deliveryEnabled: current?.deliveryEnabled ?? false,
      }))
      setWhatsappPhone('')
      setWhatsappConsent(false)
      setWhatsappMessage('Autorização cancelada e número removido.')
    } catch (requestError) {
      if (requestError.status === 401) return logout()
      setWhatsappError(requestError.message)
    } finally {
      setWhatsappSaving(false)
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

          <div className="profile-card__notice">
            <UserRound size={19} aria-hidden="true" />
            <p>Os dados cadastrais são somente para consulta nesta versão do MVP.</p>
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

      <article className="profile-whatsapp" aria-labelledby="whatsapp-title">
        <div className="profile-whatsapp__heading">
          <div className="profile-whatsapp__icon"><MessageCircle size={24} aria-hidden="true" /></div>
          <div>
            <span>Notificações climáticas</span>
            <h2 id="whatsapp-title">WhatsApp</h2>
            <p>Cadastre seu número para receber alertas climáticos ativados pela prefeitura.</p>
          </div>
          {whatsappPreferences?.notificationsEnabled && <strong className="profile-whatsapp__status">Autorizado</strong>}
        </div>

        {whatsappLoading ? (
          <FeedbackState type="loading" message="Consultando suas preferências..." />
        ) : (
          <form className="profile-whatsapp__form" onSubmit={saveWhatsappPreferences}>
            {!whatsappPreferences?.deliveryEnabled && (
              <div className="profile-whatsapp__availability" role="status">
                <strong>Envio real ainda não habilitado</strong>
                <p>Você pode salvar sua preferência, mas nenhuma mensagem será enviada até a prefeitura configurar e habilitar a integração oficial com a Meta.</p>
              </div>
            )}

            <div className="form-field">
              <label htmlFor="whatsapp-phone">Número com DDD e código do país</label>
              <input
                autoComplete="tel"
                className="form-control"
                id="whatsapp-phone"
                inputMode="tel"
                maxLength={30}
                onChange={(event) => { setWhatsappPhone(event.target.value); setWhatsappError(''); setWhatsappMessage('') }}
                placeholder="+55 13 99999-9999"
                required
                type="tel"
                value={whatsappPhone}
              />
              <small>O número será armazenado no formato internacional, por exemplo +5513999999999.</small>
            </div>

            <label className="profile-whatsapp__consent">
              <input checked={whatsappConsent} onChange={(event) => { setWhatsappConsent(event.target.checked); setWhatsappError('') }} type="checkbox" />
              <span>Autorizo expressamente o envio de alertas climáticos para este número pelo WhatsApp.</span>
            </label>

            {whatsappPreferences?.consentAt && (
              <p className="profile-whatsapp__consent-date">Consentimento atual registrado em {formatDateTime(whatsappPreferences.consentAt)}.</p>
            )}
            {whatsappError && <div className="form-message form-message--error" role="alert">{whatsappError}</div>}
            {whatsappMessage && <div className="form-message form-message--success" role="status">{whatsappMessage}</div>}

            <div className="profile-whatsapp__actions">
              <Button disabled={whatsappSaving || !whatsappConsent} type="submit">
                {whatsappSaving && <LoaderCircle className="button__spinner" size={17} aria-hidden="true" />}
                {whatsappPreferences?.notificationsEnabled ? 'Atualizar preferência' : 'Autorizar notificações'}
              </Button>
              {whatsappPreferences?.notificationsEnabled && (
                <Button disabled={whatsappSaving} onClick={removeWhatsapp} type="button" variant="outline">
                  <Trash2 size={17} aria-hidden="true" />Cancelar e remover número
                </Button>
              )}
            </div>
          </form>
        )}
      </article>
    </section>
  )
}

export default ProfilePage
