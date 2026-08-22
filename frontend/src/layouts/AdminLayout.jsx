import { Bell, LayoutDashboard, ListChecks, LogOut } from 'lucide-react'
import { Link, NavLink, Outlet } from 'react-router'
import Brand from '../components/ui/Brand'

const navigationItems = [
  { to: '/admin', label: 'Visão geral', icon: LayoutDashboard, end: true },
  { to: '/admin/ocorrencias', label: 'Ocorrências', icon: ListChecks },
  { to: '/admin/alertas', label: 'Alertas climáticos', icon: Bell },
]

function AdminLayout() {
  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <Brand inverse />

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
              to={to}
            >
              <Icon size={20} aria-hidden="true" />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="admin-sidebar__account">
          <div>
            <strong>Administrador</strong>
            <span>Painel administrativo</span>
          </div>
          <Link to="/login" aria-label="Sair">
            <LogOut size={19} aria-hidden="true" />
          </Link>
        </div>
      </aside>

      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  )
}

export default AdminLayout
