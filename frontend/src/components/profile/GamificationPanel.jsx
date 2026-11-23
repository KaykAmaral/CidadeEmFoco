import { useEffect, useState } from 'react'
import { Trophy } from 'lucide-react'
import { Link } from 'react-router'
import { getMyGamification } from '../../services/gamificationService'

function GamificationPanel({ logout, token }) {
  const [gamification, setGamification] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getMyGamification(token).then(setGamification).catch((requestError) => requestError.status === 401 ? logout() : setError(requestError.message))
  }, [logout, token])

  const progress = gamification?.xpForNextLevel > 0
    ? Math.min(100, (gamification.xpInCurrentLevel / gamification.xpForNextLevel) * 100) : 100

  return <section className="profile-gamification" aria-labelledby="gamification-title">
    <div className="profile-gamification__heading"><div><span>Participação</span><h2 id="gamification-title">Meu nível</h2></div></div>
    {error && <div className="form-message form-message--error" role="alert">{error}</div>}
    {gamification ? <div className="profile-level-card"><div className="profile-level-badge">{gamification.level}</div><div><strong>Nível {gamification.level} · {gamification.totalXp} XP</strong><p>Continue contribuindo para avançar.</p><div className="profile-level-progress" aria-label={`${Math.round(progress)}% para o próximo nível`} role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow={Math.round(progress)}><span style={{ width: `${progress}%` }} /></div><small className="profile-level-caption">{gamification.xpInCurrentLevel} de {gamification.xpForNextLevel} XP</small></div></div> : <p role="status">Carregando XP...</p>}
    <p className="profile-gamification__note">Você recebe 10 XP por relato aceito e 20 XP quando um caso do qual participou é resolvido.</p>
    <Link className="button button--outline profile-ranking-link" to="/app/rankings"><Trophy size={17} aria-hidden="true" />Ver rankings</Link>
  </section>
}

export default GamificationPanel
