import { Bell, ClipboardList, Home, Plus, UserRound } from 'lucide-react'
import { NavLink, Outlet } from 'react-router'
import Brand from '../components/ui/Brand'

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
  return (
    <div className="citizen-layout">
      <header className="citizen-header">
        <div className="citizen-container">
          <Brand />
          <span className="profile-label">Cidadão</span>
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
