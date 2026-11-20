import { useEffect, useState } from 'react'
import { getCitizenRanking, getCityRanking, getMyGamification } from '../../services/gamificationService'

const citizenPeriods = ['WEEK', 'MONTH', 'YEAR', 'ALL']
const cityPeriods = ['WEEK', 'MONTH', 'YEAR']

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

  return <section className="profile-preferences" aria-labelledby="gamification-title">
    <div className="profile-preferences__heading"><div><span>Participação</span><h3 id="gamification-title">XP, nível e rankings</h3></div></div>
    {error && <div className="form-message form-message--error" role="alert">{error}</div>}
    {gamification ? <div><p><strong>Nível {gamification.level}</strong> · {gamification.totalXp} XP total</p><progress aria-label="Progresso para o próximo nível" max={gamification.xpForNextLevel} value={gamification.xpInCurrentLevel} style={{ width: '100%' }} /> <small>{Math.round(progress)}% — {gamification.xpInCurrentLevel}/{gamification.xpForNextLevel} XP</small><p><small>Você recebe 10 XP por relato aceito e 20 XP quando um caso do qual participou é resolvido.</small></p></div> : <p role="status">Carregando XP...</p>}
    <h4>Ranking de cidadãos da sua cidade</h4>
    <select aria-label="Período do ranking de cidadãos" onChange={(event) => setCitizenPeriod(event.target.value)} value={citizenPeriod}>{citizenPeriods.map((period) => <option key={period}>{period}</option>)}</select>
    {citizens?.ranking?.length ? <ol>{citizens.ranking.map((entry) => <li key={entry.userId}><strong>{entry.name}</strong> — {entry.xpEarnedInPeriod} XP no período, nível {entry.level}</li>)}</ol> : <p>Nenhum participante neste período.</p>}
    <h4>Ranking entre prefeituras</h4>
    <select aria-label="Período do ranking de prefeituras" onChange={(event) => setCityPeriod(event.target.value)} value={cityPeriod}>{cityPeriods.map((period) => <option key={period}>{period}</option>)}</select>
    {cities?.ranking?.length ? <ol>{cities.ranking.map((entry) => <li key={entry.city.ibgeCode}><strong>{entry.city.name}/{entry.city.state}</strong> — {entry.totalCases} casos, {entry.resolvedCases} resolvidos ({entry.resolutionRatePercent}%)</li>)}</ol> : <p>Nenhuma prefeitura classificada neste período.</p>}
    <p><small>Ocorrências não confirmadas não entram no cálculo do índice de resolução.</small></p>
  </section>
}

export default GamificationPanel
