import {
  Bell,
  ClipboardList,
  Home,
  LogOut,
  Plus,
  UserRound,
} from 'lucide-react'
import { NavLink, Outlet } from 'react-router'
import Brand from '../components/ui/Brand'
import useAuth from '../hooks/useAuth'

const navigationItems = [
  { to: '/app', label: 'Início', icon: Home, end: true },
  { to: '/app/ocorrencias', label: 'Ocorrências', icon: ClipboardList },
  {
    to: '/app/ocorrencias/nova',
    label: 'Registrar',
    icon: Plus,
    primary: true,
  },
  { to: '/app/alertas', label: 'Alertas', icon: Bell },
  { to: '/app/perfil', label: 'Perfil', icon: UserRound },
]

function CitizenLayout() {
  const { logout, user } = useAuth()
  const firstName = user.name.split(' ')[0]

  return (
    <div className="citizen-layout">
      <header className="citizen-header">
        <div className="citizen-container">
          <Brand />
          <div className="citizen-header__account">
            <span className="profile-label">Olá, {firstName}</span>
            <button className="logout-button" onClick={logout} type="button">
              <LogOut size={18} aria-hidden="true" />
              <span>Sair</span>
            </button>
          </div>
        </div>
      </header>

      <main className="citizen-main citizen-container">
        <Outlet />
      </main>

      <nav className="citizen-navigation" aria-label="Navegação do cidadão">
        {navigationItems.map(({ to, label, icon: Icon, end, primary }) => (
          <NavLink
            className={({ isActive }) =>
              [
                'citizen-navigation__item',
                isActive && 'citizen-navigation__item--active',
                primary && 'citizen-navigation__item--primary',
              ]
                .filter(Boolean)
                .join(' ')
            }
            end={end}
            key={to}
            to={to}
          >
            <span className="citizen-navigation__icon">
              <Icon size={21} aria-hidden="true" />
            </span>
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  )
}

export default CitizenLayout
