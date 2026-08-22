import { CheckCircle2, MapPinned, Route, ShieldCheck } from 'lucide-react'
import './App.css'

const setupItems = [
  { icon: Route, label: 'Navegação preparada' },
  { icon: MapPinned, label: 'Mapa preparado' },
  { icon: ShieldCheck, label: 'Integração com a API preparada' },
]

function App() {
  const apiUrl = import.meta.env.VITE_API_URL

  return (
    <main className="setup-page">
      <section className="setup-card" aria-labelledby="setup-title">
        <img
          className="setup-logo"
          src="/brand/cidade-em-foco-app-icon.png"
          alt="Cidade em Foco"
        />

        <div className="setup-heading">
          <span className="setup-badge">
            <CheckCircle2 size={16} aria-hidden="true" />
            Etapa 1 concluída
          </span>
          <h1 id="setup-title">Frontend preparado</h1>
          <p>
            A base React com JavaScript está pronta para receber as telas do
            Cidade em Foco.
          </p>
        </div>

        <ul className="setup-list">
          {setupItems.map(({ icon: Icon, label }) => (
            <li key={label}>
              <Icon size={20} aria-hidden="true" />
              <span>{label}</span>
            </li>
          ))}
        </ul>

        <div className="api-info">
          <span>API configurada</span>
          <code>{apiUrl}</code>
        </div>
      </section>
    </main>
  )
}

export default App
