import { ArrowRight, MapPinned, RotateCw } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import ActiveAlertBanner from '../components/alerts/ActiveAlertBanner'
import OccurrenceMap from '../components/map/OccurrenceMap'
import OccurrenceCard from '../components/occurrences/OccurrenceCard'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import useAuth from '../hooks/useAuth'
import { getActiveAlerts } from '../services/alertService'
import { getOccurrences } from '../services/occurrenceService'
import './CitizenHomePage.css'

function CitizenHomePage() {
  const { logout, token, user } = useAuth()
  const [alerts, setAlerts] = useState([])
  const [occurrences, setOccurrences] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let isCurrent = true

    async function loadDashboard() {
      try {
        const [activeAlerts, occurrenceList] = await Promise.all([
          getActiveAlerts(token),
          getOccurrences(token),
        ])

        if (isCurrent) {
          setAlerts(activeAlerts)
          setOccurrences(occurrenceList)
        }
      } catch (requestError) {
        if (!isCurrent) {
          return
        }

        if (requestError.status === 401) {
          logout()
          return
        }

        setError(requestError.message)
      } finally {
        if (isCurrent) {
          setIsLoading(false)
        }
      }
    }

    loadDashboard()

    return () => {
      isCurrent = false
    }
  }, [logout, reloadKey, token])

  function handleRetry() {
    setError('')
    setIsLoading(true)
    setReloadKey((currentKey) => currentKey + 1)
  }

  if (isLoading) {
    return (
      <FeedbackState
        type="loading"
        title="Preparando sua visão da cidade"
        message="Buscando alertas e ocorrências recentes."
      />
    )
  }

  if (error) {
    return (
      <div className="dashboard-error">
        <FeedbackState type="error" message={error} />
        <Button variant="outline" onClick={handleRetry}>
          <RotateCw size={17} aria-hidden="true" />
          Tentar novamente
        </Button>
      </div>
    )
  }

  const recentOccurrences = occurrences.slice(0, 5)
  const firstName = user.name.split(' ')[0]

  return (
    <div className="citizen-dashboard">
      <header className="dashboard-heading">
        <div>
          <span>Visão geral</span>
          <h1>Olá, {firstName}</h1>
          <p>Acompanhe o que está acontecendo em Praia Grande.</p>
        </div>
        <Link className="button button--primary" to="/app/ocorrencias/nova">
          Registrar ocorrência
          <ArrowRight size={17} aria-hidden="true" />
        </Link>
      </header>

      <ActiveAlertBanner alerts={alerts} />

      <div className="dashboard-grid">
        <section className="dashboard-map-card" aria-labelledby="map-title">
          <div className="dashboard-section-heading">
            <div>
              <MapPinned size={20} aria-hidden="true" />
              <h2 id="map-title">Mapa de ocorrências</h2>
            </div>
            <div className="map-legend" aria-label="Legenda do mapa">
              <span><i className="map-legend__natural" />Evento natural</span>
              <span><i className="map-legend__infrastructure" />Infraestrutura</span>
            </div>
          </div>
          <OccurrenceMap occurrences={occurrences} />
          {occurrences.length === 0 && (
            <p className="map-empty-message">
              Ainda não há ocorrências para posicionar no mapa.
            </p>
          )}
        </section>

        <section className="recent-occurrences" aria-labelledby="recent-title">
          <div className="dashboard-section-heading">
            <h2 id="recent-title">Ocorrências recentes</h2>
            <Link to="/app/ocorrencias">Ver todas</Link>
          </div>

          {recentOccurrences.length > 0 ? (
            <div className="recent-occurrences__list">
              {recentOccurrences.map((occurrence) => (
                <OccurrenceCard
                  occurrence={occurrence}
                  key={occurrence.id}
                  to={`/app/ocorrencias/${occurrence.id}`}
                />
              ))}
            </div>
          ) : (
            <FeedbackState
              type="empty"
              title="Nenhuma ocorrência registrada"
              message="As novas ocorrências aparecerão aqui."
            />
          )}
        </section>
      </div>
    </div>
  )
}

export default CitizenHomePage
