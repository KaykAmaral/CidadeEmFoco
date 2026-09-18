import { Navigate, Outlet } from 'react-router'
import useAuth from '../hooks/useAuth'

function PublicOnlyRoute() {
  const { isAuthenticated, user } = useAuth()

  if (isAuthenticated) {
    const home = user.role === 'ADMIN' ? '/admin' : '/app'
    return <Navigate to={home} replace />
  }

  return <Outlet />
}

export default PublicOnlyRoute
