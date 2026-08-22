import { Construction } from 'lucide-react'

function PlaceholderPage({ eyebrow, title, description }) {
  return (
    <section className="page-placeholder" aria-labelledby="page-title">
      <header className="page-heading">
        <span>{eyebrow}</span>
        <h1 id="page-title">{title}</h1>
        <p>{description}</p>
      </header>

      <div className="page-placeholder__content">
        <Construction size={28} aria-hidden="true" />
        <div>
          <strong>Estrutura de navegação pronta</strong>
          <p>O conteúdo funcional desta página será implementado por etapas.</p>
        </div>
      </div>
    </section>
  )
}

export default PlaceholderPage
