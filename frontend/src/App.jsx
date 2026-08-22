import { Navigate, Route, Routes } from 'react-router'
import AdminLayout from './layouts/AdminLayout'
import CitizenLayout from './layouts/CitizenLayout'
import PublicLayout from './layouts/PublicLayout'
import CitizenHomePage from './pages/CitizenHomePage'
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
            element={
              <PlaceholderPage
                eyebrow="Clima"
                title="Alertas climáticos"
                description="Consulta dos alertas ativos cadastrados pelo administrador."
              />
            }
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
            element={
              <PlaceholderPage
                eyebrow="Administração"
                title="Visão geral"
                description="Resumo administrativo das ocorrências e alertas do sistema."
              />
            }
          />
          <Route
            path="ocorrencias"
            element={
              <PlaceholderPage
                eyebrow="Administração"
                title="Gerenciar ocorrências"
                description="Consulta, análise e atualização do status das ocorrências."
              />
            }
          />
          <Route
            path="alertas"
            element={
              <PlaceholderPage
                eyebrow="Administração"
                title="Gerenciar alertas"
                description="Criação, consulta, ativação e desativação dos alertas climáticos."
              />
            }
          />
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
