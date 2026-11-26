import GamificationPanel from '../components/profile/GamificationPanel'
import useAuth from '../hooks/useAuth'
import './ParticipationPage.css'

function ParticipationPage() {
  const { logout, token } = useAuth()

  return (
    <section className="participation-page">
      <header className="participation-page__heading">
        <span>Participação cidadã</span>
        <h1>Participação</h1>
        <p>Acompanhe sua evolução e compare a participação de cidadãos e prefeituras.</p>
      </header>

      <GamificationPanel logout={logout} token={token} />
    </section>
  )
}

export default ParticipationPage
