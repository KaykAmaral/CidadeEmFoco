import { Building2, Medal, RotateCw, Trophy, Users } from 'lucide-react'
import { useEffect, useState } from 'react'
import Button from '../components/ui/Button'
import FeedbackState from '../components/ui/FeedbackState'
import useAuth from '../hooks/useAuth'
import { getCitizenRanking, getCityRanking } from '../services/gamificationService'
import './RankingsPage.css'

const citizenPeriods = ['WEEK', 'MONTH', 'YEAR', 'ALL']
const cityPeriods = ['WEEK', 'MONTH', 'YEAR']
const periodLabels = { WEEK: 'Esta semana', MONTH: 'Este mês', YEAR: 'Este ano', ALL: 'Todo o período' }

function RankingsPage() {
  const { logout, token, user } = useAuth()
  const [citizenPeriod, setCitizenPeriod] = useState('MONTH')
  const [cityPeriod, setCityPeriod] = useState('MONTH')
  const [citizens, setCitizens] = useState(null)
  const [cities, setCities] = useState(null)
  const [citizenError, setCitizenError] = useState('')
  const [cityError, setCityError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true
    getCitizenRanking(token, citizenPeriod)
      .then((data) => { if (active) setCitizens(data) })
      .catch((error) => { if (!active) return; if (error.status === 401) logout(); else setCitizenError(error.message) })
    return () => { active = false }
  }, [citizenPeriod, logout, reloadKey, token])

  useEffect(() => {
    let active = true
    getCityRanking(token, cityPeriod)
      .then((data) => { if (active) setCities(data) })
      .catch((error) => { if (!active) return; if (error.status === 401) logout(); else setCityError(error.message) })
    return () => { active = false }
  }, [cityPeriod, logout, reloadKey, token])

  return (
    <section className="rankings-page">
      <header className="rankings-hero">
        <div className="rankings-hero__icon"><Trophy size={30} aria-hidden="true" /></div>
        <div><span>Participação que transforma</span><h1>Rankings do Cidade em Foco</h1><p>Acompanhe quem mais contribui e as cidades com melhor índice de resolução.</p></div>
      </header>

      <div className="rankings-grid">
        <section className="ranking-board" aria-labelledby="citizen-ranking-title">
          <div className="ranking-board__heading">
            <div className="ranking-board__title"><Users size={21} aria-hidden="true" /><div><span>{user.city?.name || 'Sua cidade'}</span><h2 id="citizen-ranking-title">Cidadãos em destaque</h2></div></div>
            <select aria-label="Período do ranking de cidadãos" onChange={(event) => { setCitizenError(''); setCitizens(null); setCitizenPeriod(event.target.value) }} value={citizenPeriod}>{citizenPeriods.map((period) => <option key={period} value={period}>{periodLabels[period]}</option>)}</select>
          </div>
          {citizenError ? <div className="ranking-error"><FeedbackState type="error" message={citizenError} /><Button onClick={() => { setCitizenError(''); setCitizens(null); setReloadKey((value) => value + 1) }} variant="outline"><RotateCw size={16} aria-hidden="true" />Tentar novamente</Button></div>
            : !citizens ? <FeedbackState type="loading" message="Carregando cidadãos..." />
              : citizens.ranking?.length ? <ol className="ranking-list">{citizens.ranking.map((entry) => <li className={entry.userId === user.id ? 'ranking-list__item--current' : ''} key={entry.userId}><span className="ranking-list__position">{entry.position <= 3 ? <Medal size={18} aria-hidden="true" /> : entry.position}</span><div className="ranking-list__identity"><strong>{entry.name}{entry.userId === user.id ? ' (você)' : ''}</strong><small>Nível {entry.level} · {entry.totalXp} XP total</small></div><span className="ranking-list__result">{entry.xpEarnedInPeriod} XP</span></li>)}</ol>
                : <FeedbackState type="empty" title="Ranking ainda vazio" message="As contribuições dos cidadãos aparecerão aqui." />}
        </section>

        <section className="ranking-board" aria-labelledby="city-ranking-title">
          <div className="ranking-board__heading">
            <div className="ranking-board__title"><Building2 size={21} aria-hidden="true" /><div><span>Gestão municipal</span><h2 id="city-ranking-title">Prefeituras em destaque</h2></div></div>
            <select aria-label="Período do ranking de prefeituras" onChange={(event) => { setCityError(''); setCities(null); setCityPeriod(event.target.value) }} value={cityPeriod}>{cityPeriods.map((period) => <option key={period} value={period}>{periodLabels[period]}</option>)}</select>
          </div>
          {cityError ? <div className="ranking-error"><FeedbackState type="error" message={cityError} /><Button onClick={() => { setCityError(''); setCities(null); setReloadKey((value) => value + 1) }} variant="outline"><RotateCw size={16} aria-hidden="true" />Tentar novamente</Button></div>
            : !cities ? <FeedbackState type="loading" message="Carregando prefeituras..." />
              : cities.ranking?.length ? <ol className="ranking-list">{cities.ranking.map((entry) => <li key={entry.city.ibgeCode}><span className="ranking-list__position">{entry.position <= 3 ? <Medal size={18} aria-hidden="true" /> : entry.position}</span><div className="ranking-list__identity"><strong>{entry.city.name}/{entry.city.stateCode}</strong><small>{entry.resolvedCases} resolvidos · {entry.confirmedCases} confirmados</small></div><span className="ranking-list__result">{entry.resolutionRatePercent}%</span></li>)}</ol>
                : <FeedbackState type="empty" title="Ranking ainda vazio" message="Os resultados das prefeituras aparecerão aqui." />}
          <p className="ranking-board__note">Ocorrências não confirmadas não entram no cálculo do índice de resolução.</p>
        </section>
      </div>
    </section>
  )
}

export default RankingsPage
