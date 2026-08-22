import { Navigate, Outlet, useLocation } from 'react-router'
import useAuth from '../hooks/useAuth'

function ProtectedRoute({ allowedRole }) {
  const { isAuthenticated, user } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (allowedRole && user.role !== allowedRole) {
    const correctHome = user.role === 'ADMIN' ? '/admin' : '/app'
    return <Navigate to={correctHome} replace />
  }

  return <Outlet />
}

export default ProtectedRoute
