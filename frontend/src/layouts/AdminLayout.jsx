import { Bell, LayoutDashboard, ListChecks, LogOut, Menu, X } from 'lucide-react'
import { useState } from 'react'
import { NavLink, Outlet } from 'react-router'
import useAuth from '../hooks/useAuth'

const navigationItems = [
  { to: '/admin', label: 'Visão geral', icon: LayoutDashboard, end: true },
  { to: '/admin/ocorrencias', label: 'Ocorrências', icon: ListChecks },
  { to: '/admin/alertas', label: 'Alertas climáticos', icon: Bell },
]

function AdminLayout() {
  const { logout, user } = useAuth()
  const [isNavigationOpen, setIsNavigationOpen] = useState(false)

  return (
    <div className="admin-layout">
      <header className="admin-mobile-header">
        <img src="/brand/cidade-em-foco-app-icon.png" alt="Cidade em Foco" />
        <button
          aria-controls="admin-sidebar"
          aria-expanded={isNavigationOpen}
          aria-label={isNavigationOpen ? 'Fechar menu administrativo' : 'Abrir menu administrativo'}
          onClick={() => setIsNavigationOpen((open) => !open)}
          type="button"
        >
          {isNavigationOpen ? <X size={22} /> : <Menu size={22} />}
        </button>
      </header>

      <aside
        className={`admin-sidebar${isNavigationOpen ? ' admin-sidebar--open' : ''}`}
        id="admin-sidebar"
      >
        <div className="admin-sidebar__brand">
          <img src="/brand/cidade-em-foco-app-icon.png" alt="Cidade em Foco — Nossa cidade, nosso olhar" />
        </div>

        <nav className="admin-navigation" aria-label="Navegação administrativa">
          {navigationItems.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              className={({ isActive }) =>
                `admin-navigation__item${
                  isActive ? ' admin-navigation__item--active' : ''
                }`
              }
              end={end}
              key={to}
              onClick={() => setIsNavigationOpen(false)}
              to={to}
            >
              <Icon size={20} aria-hidden="true" />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="admin-sidebar__account">
          <div className="admin-sidebar__avatar" aria-hidden="true">
            {user.name?.charAt(0).toUpperCase()}
          </div>
          <div className="admin-sidebar__account-details">
            <strong>{user.name}</strong>
            <span>{user.email}</span>
          </div>
          <button onClick={logout} type="button" aria-label="Sair">
            <LogOut size={19} aria-hidden="true" />
          </button>
        </div>
      </aside>

      {isNavigationOpen && (
        <button
          className="admin-sidebar-backdrop"
          aria-label="Fechar menu administrativo"
          onClick={() => setIsNavigationOpen(false)}
          type="button"
        />
      )}

      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  )
}

export default AdminLayout
