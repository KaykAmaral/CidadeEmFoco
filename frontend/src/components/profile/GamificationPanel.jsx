import { Building2, Medal, Sparkles, Trophy, UsersRound } from 'lucide-react'
import { useEffect, useState } from 'react'
import { getCitizenRanking, getCityRanking, getMyGamification } from '../../services/gamificationService'

const citizenPeriods = ['WEEK', 'MONTH', 'YEAR', 'ALL']
const cityPeriods = ['WEEK', 'MONTH', 'YEAR']
const periodLabels = { WEEK: 'Semana', MONTH: 'Mês', YEAR: 'Ano', ALL: 'Todo o período' }

function RankingEmpty({ children, icon: Icon }) {
  return <div className="ranking-empty"><span><Icon size={22} aria-hidden="true" /></span><p>{children}</p></div>
}

function GamificationPanel({ logout, token }) {
  const [gamification, setGamification] = useState(null)
  const [citizenPeriod, setCitizenPeriod] = useState('MONTH')
  const [cityPeriod, setCityPeriod] = useState('MONTH')
  const [citizens, setCitizens] = useState(null)
  const [cities, setCities] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getMyGamification(token).then(setGamification).catch((requestError) => requestError.status === 401 ? logout() : setError(requestError.message))
  }, [logout, token])
  useEffect(() => { getCitizenRanking(token, citizenPeriod).then(setCitizens).catch((requestError) => setError(requestError.message)) }, [citizenPeriod, token])
  useEffect(() => { getCityRanking(token, cityPeriod).then(setCities).catch((requestError) => setError(requestError.message)) }, [cityPeriod, token])

  const progress = gamification?.xpForNextLevel > 0
    ? Math.min(100, (gamification.xpInCurrentLevel / gamification.xpForNextLevel) * 100) : 100

  return <section className="profile-section gamification-panel" aria-labelledby="gamification-title">
    <div className="profile-section__heading"><div><span>Participação</span><h2 id="gamification-title">XP, nível e rankings</h2></div><Sparkles size={22} aria-hidden="true" /></div>
    {error && <div className="form-message form-message--error" role="alert">{error}</div>}
    {gamification ? <div className="xp-summary"><div className="xp-summary__badge"><Medal size={24} aria-hidden="true" /><span>Nível</span><strong>{gamification.level}</strong></div><div className="xp-summary__content"><div className="xp-summary__line"><strong>{gamification.totalXp} XP total</strong><span>{Math.round(progress)}% para o próximo nível</span></div><progress aria-label="Progresso para o próximo nível" max={gamification.xpForNextLevel} value={gamification.xpInCurrentLevel} /><div className="xp-summary__meta"><span>{gamification.xpInCurrentLevel}/{gamification.xpForNextLevel} XP</span><span>10 XP por relato aceito · 20 XP por caso resolvido</span></div></div></div> : <p role="status">Carregando XP...</p>}
    <div className="rankings-grid">
      <article className="ranking-card">
        <div className="ranking-card__heading"><div><UsersRound size={20} aria-hidden="true" /><div><span>Comunidade local</span><h3>Ranking de cidadãos</h3></div></div><label><span>Período</span><select className="ranking-select" aria-label="Período do ranking de cidadãos" onChange={(event) => setCitizenPeriod(event.target.value)} value={citizenPeriod}>{citizenPeriods.map((period) => <option key={period} value={period}>{periodLabels[period]}</option>)}</select></label></div>
        {citizens?.ranking?.length ? <ol className="ranking-list">{citizens.ranking.map((entry, index) => <li key={entry.userId}><span className="ranking-list__position">{index + 1}</span><div><strong>{entry.name}</strong><span>{entry.xpEarnedInPeriod} XP no período · Nível {entry.level}</span></div></li>)}</ol> : <RankingEmpty icon={Trophy}>Nenhum participante neste período.</RankingEmpty>}
      </article>
      <article className="ranking-card">
        <div className="ranking-card__heading"><div><Building2 size={20} aria-hidden="true" /><div><span>Gestão pública</span><h3>Ranking entre prefeituras</h3></div></div><label><span>Período</span><select className="ranking-select" aria-label="Período do ranking de prefeituras" onChange={(event) => setCityPeriod(event.target.value)} value={cityPeriod}>{cityPeriods.map((period) => <option key={period} value={period}>{periodLabels[period]}</option>)}</select></label></div>
        {cities?.ranking?.length ? <ol className="ranking-list">{cities.ranking.map((entry, index) => <li key={entry.city.ibgeCode}><span className="ranking-list__position">{index + 1}</span><div><strong>{entry.city.name}/{entry.city.state}</strong><span>{entry.totalCases} casos · {entry.resolvedCases} resolvidos ({entry.resolutionRatePercent}%)</span></div></li>)}</ol> : <RankingEmpty icon={Building2}>Nenhuma prefeitura classificada neste período.</RankingEmpty>}
      </article>
    </div>
    <p className="ranking-note">Ocorrências não confirmadas não entram no cálculo do índice de resolução.</p>
  </section>
}

export default GamificationPanel
