import { ArrowRight, MapPinned, RotateCw } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import ActiveAlertBanner from '../components/alerts/ActiveAlertBanner'
import OccurrenceMap from '../components/map/OccurrenceMap'
import OccurrenceCategoryFilter from '../components/map/OccurrenceCategoryFilter'
import OccurrenceCard from '../components/occurrences/OccurrenceCard'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import useAuth from '../hooks/useAuth'
import useAlerts from '../hooks/useAlerts'
import { getMapOccurrences, getOccurrences } from '../services/occurrenceService'
import './CitizenHomePage.css'

const MAP_REFRESH_INTERVAL_MS = 60_000

function CitizenHomePage() {
  const { logout, token, user } = useAuth()
  const { alerts } = useAlerts()
  const [occurrences, setOccurrences] = useState([])
  const [mapOccurrences, setMapOccurrences] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [activeMapCategories, setActiveMapCategories] = useState([
    'EVENTO_NATURAL',
    'INFRAESTRUTURA_URBANA',
  ])

  useEffect(() => {
    let isCurrent = true

    async function loadDashboard() {
      try {
        const [occurrenceList, mapOccurrenceList] = await Promise.all([
          getOccurrences(token),
          getMapOccurrences(token),
        ])

        if (isCurrent) {
          setOccurrences(occurrenceList)
          setMapOccurrences(mapOccurrenceList)
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

  useEffect(() => {
    let isCurrent = true

    const refreshMap = async () => {
      try {
        const mapOccurrenceList = await getMapOccurrences(token)
        if (isCurrent) setMapOccurrences(mapOccurrenceList)
      } catch (requestError) {
        if (isCurrent && requestError.status === 401) logout()
      }
    }

    const intervalId = window.setInterval(refreshMap, MAP_REFRESH_INTERVAL_MS)

    return () => {
      isCurrent = false
      window.clearInterval(intervalId)
    }
  }, [logout, token])

  function handleRetry() {
    setError('')
    setIsLoading(true)
    setReloadKey((currentKey) => currentKey + 1)
  }

  function toggleMapCategory(category) {
    setActiveMapCategories((categories) =>
      categories.includes(category)
        ? categories.filter((value) => value !== category)
        : [...categories, category])
  }

  if (isLoading) {
    return (
      <FeedbackState
        type="loading"
        title="Preparando sua visÃ£o da cidade"
        message="Buscando alertas e ocorrÃªncias recentes."
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
          <span>VisÃ£o geral</span>
          <h1>OlÃ¡, {firstName}</h1>
          <p>Acompanhe o que estÃ¡ acontecendo em Praia Grande.</p>
        </div>
        <Link className="button button--primary" to="/app/ocorrencias/nova">
          Registrar ocorrÃªncia
          <ArrowRight size={17} aria-hidden="true" />
        </Link>
      </header>

      <ActiveAlertBanner alerts={alerts} />

      <div className="dashboard-grid">
        <section className="dashboard-map-card" aria-labelledby="map-title">
          <div className="dashboard-section-heading">
            <div>
              <MapPinned size={20} aria-hidden="true" />
              <h2 id="map-title">Mapa de ocorrÃªncias</h2>
            </div>
            <OccurrenceCategoryFilter activeCategories={activeMapCategories} occurrences={mapOccurrences} onToggle={toggleMapCategory} />
          </div>
          <OccurrenceMap activeCategories={activeMapCategories} occurrences={mapOccurrences} />
          {mapOccurrences.length === 0 && (
            <p className="map-empty-message">
              Ainda nÃ£o hÃ¡ ocorrÃªncias para posicionar no mapa.
            </p>
          )}
        </section>

        <section className="recent-occurrences" aria-labelledby="recent-title">
          <div className="dashboard-section-heading">
            <h2 id="recent-title">OcorrÃªncias recentes</h2>
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
              title="Nenhuma ocorrÃªncia registrada"
              message="As novas ocorrÃªncias aparecerÃ£o aqui."
            />
          )}
        </section>
      </div>
    </div>
  )
}

export default CitizenHomePage

