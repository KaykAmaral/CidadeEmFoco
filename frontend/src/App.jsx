import { Navigate, Route, Routes } from 'react-router'
import AdminLayout from './layouts/AdminLayout'
import CitizenLayout from './layouts/CitizenLayout'
import PublicLayout from './layouts/PublicLayout'
import CitizenHomePage from './pages/CitizenHomePage'
import AlertsPage from './pages/AlertsPage'
import AdminAlertsPage from './pages/AdminAlertsPage'
import AdminDashboardPage from './pages/AdminDashboardPage'
import AdminOccurrenceDetailPage from './pages/AdminOccurrenceDetailPage'
import AdminOccurrencesPage from './pages/AdminOccurrencesPage'
import LoginPage from './pages/LoginPage'
import NewOccurrencePage from './pages/NewOccurrencePage'
import NotFoundPage from './pages/NotFoundPage'
import OccurrenceDetailPage from './pages/OccurrenceDetailPage'
import OccurrencesPage from './pages/OccurrencesPage'
import PlaceholderPage from './pages/PlaceholderPage'
import RegisterPage from './pages/RegisterPage'
import ProtectedRoute from './routes/ProtectedRoute'
import PublicOnlyRoute from './routes/PublicOnlyRoute'
import './styles/layouts.css'

function App() {
  return (
    <Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route element={<PublicLayout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/cadastro" element={<RegisterPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRole="CITIZEN" />}>
        <Route path="/app" element={<CitizenLayout />}>
          <Route
            index
            element={<CitizenHomePage />}
          />
          <Route
            path="ocorrencias"
            element={<OccurrencesPage />}
          />
          <Route
            path="ocorrencias/nova"
            element={<NewOccurrencePage />}
          />
          <Route path="ocorrencias/:id" element={<OccurrenceDetailPage />} />
          <Route
            path="alertas"
            element={<AlertsPage />}
          />
          <Route
            path="perfil"
            element={
              <PlaceholderPage
                eyebrow="Conta"
                title="Meu perfil"
                description="Dados básicos do cidadão e acesso às suas próprias ocorrências."
              />
            }
          />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRole="ADMIN" />}>
        <Route path="/admin" element={<AdminLayout />}>
          <Route
            index
            element={<AdminDashboardPage />}
          />
          <Route
            path="ocorrencias"
            element={<AdminOccurrencesPage />}
          />
          <Route path="ocorrencias/:id" element={<AdminOccurrenceDetailPage />} />
          <Route
            path="alertas"
            element={<AdminAlertsPage />}
          />
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
