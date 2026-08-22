import {
  CalendarDays,
  CheckCircle2,
  ClipboardList,
  Clock3,
  LogOut,
  Mail,
  Plus,
  RotateCw,
  ShieldCheck,
  UserRound,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import OccurrenceCard from '../components/occurrences/OccurrenceCard'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import useAuth from '../hooks/useAuth'
import { getMyOccurrences } from '../services/occurrenceService'
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
                  <Link to="/app/ocorrencias">Ver todas</Link>
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
