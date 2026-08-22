import { ArrowRight, Check, Plus } from 'lucide-react'
import Brand from './components/ui/Brand'
import Button from './components/ui/Button'
import FeedbackState from './components/ui/FeedbackState'
import StatusBadge from './components/ui/StatusBadge'
import occurrenceStatusConfig from './constants/occurrenceStatus'
import './App.css'

const colors = [
  { name: 'Azul-marinho', value: '#062B52', className: 'navy' },
  { name: 'Azul oceano', value: '#0875D1', className: 'blue' },
  { name: 'Amarelo solar', value: '#FFB800', className: 'yellow' },
  { name: 'Fundo claro', value: '#F5F8FC', className: 'background' },
]

function SectionTitle({ eyebrow, children, description }) {
  return (
    <div className="section-title">
      <span>{eyebrow}</span>
      <h2>{children}</h2>
      {description && <p>{description}</p>}
    </div>
  )
}

function App() {
  return (
    <main className="design-page">
      <header className="design-header">
        <div className="design-container design-header__content">
          <Brand />
          <span className="stage-label">
            <Check size={15} aria-hidden="true" />
            Etapa 2
          </span>
        </div>
      </header>

      <section className="design-hero">
        <div className="design-container design-hero__content">
          <div>
            <span className="hero-eyebrow">Identidade visual</span>
            <h1>Uma interface simples, acolhedora e ligada à cidade.</h1>
            <p>
              Estes elementos formarão a base das telas do cidadão e do painel
              administrativo do Cidade em Foco.
            </p>
          </div>

          <div className="hero-brand-card">
            <Brand inverse />
            <p>Praia Grande — SP</p>
          </div>
        </div>
      </section>

      <div className="design-container design-content">
        <section aria-labelledby="colors-title">
          <SectionTitle
            eyebrow="Cores"
            description="A paleta foi extraída da logo e será usada de maneira consistente."
          >
            <span id="colors-title">Paleta principal</span>
          </SectionTitle>

          <div className="color-grid">
            {colors.map((color) => (
              <article className="color-card" key={color.value}>
                <div
                  className={`color-card__sample color-card__sample--${color.className}`}
                />
                <div>
                  <strong>{color.name}</strong>
                  <code>{color.value}</code>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="showcase-grid" aria-label="Elementos da interface">
          <article className="surface-card">
            <SectionTitle eyebrow="Ações">Botões</SectionTitle>
            <div className="button-showcase">
              <Button>
                <Plus size={18} aria-hidden="true" />
                Registrar ocorrência
              </Button>
              <Button variant="secondary">Ver alertas</Button>
              <Button variant="outline">
                Ver detalhes
                <ArrowRight size={17} aria-hidden="true" />
              </Button>
              <Button disabled>Indisponível</Button>
            </div>
          </article>

          <article className="surface-card">
            <SectionTitle
              eyebrow="Situação"
              description="Apenas os cinco status definidos pelo backend."
            >
              Status das ocorrências
            </SectionTitle>
            <div className="status-showcase">
              {Object.keys(occurrenceStatusConfig).map((status) => (
                <StatusBadge status={status} key={status} />
              ))}
            </div>
          </article>
        </section>

        <section className="surface-card" aria-labelledby="forms-title">
          <SectionTitle
            eyebrow="Formulários"
            description="Campos grandes e claros para funcionar bem também no celular."
          >
            <span id="forms-title">Padrão de preenchimento</span>
          </SectionTitle>

          <form className="form-grid" onSubmit={(event) => event.preventDefault()}>
            <div className="form-field">
              <label htmlFor="title">Título</label>
              <input
                className="form-control"
                id="title"
                placeholder="Ex.: Alagamento na avenida"
              />
            </div>

            <div className="form-field">
              <label htmlFor="risk">Risco percebido</label>
              <select className="form-control" id="risk" defaultValue="">
                <option value="" disabled>
                  Selecione o nível
                </option>
                <option>Baixo</option>
                <option>Médio</option>
                <option>Alto</option>
              </select>
            </div>

            <div className="form-field form-field--wide">
              <label htmlFor="description">Descrição</label>
              <textarea
                className="form-control"
                id="description"
                placeholder="Descreva o que está acontecendo..."
              />
              <small>Informe somente os detalhes necessários.</small>
            </div>
          </form>
        </section>

        <section aria-labelledby="feedback-title">
          <SectionTitle
            eyebrow="Feedback"
            description="Mensagens simples para o usuário entender o estado da tela."
          >
            <span id="feedback-title">Carregamento, vazio e erro</span>
          </SectionTitle>

          <div className="feedback-grid">
            <FeedbackState type="loading" />
            <FeedbackState type="empty" />
            <FeedbackState type="error" />
          </div>
        </section>
      </div>

      <footer className="design-footer">
        <div className="design-container">
          <Brand compact />
          <p>
            Cidade em Foco · Base visual do MVP
          </p>
        </div>
      </footer>
    </main>
  )
}

export default App
