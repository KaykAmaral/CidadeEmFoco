import { ArrowLeft, MapPinOff } from 'lucide-react'
import { Link } from 'react-router'
import Brand from '../components/ui/Brand'

function NotFoundPage() {
  return (
    <main className="not-found-page">
      <Brand />
      <MapPinOff size={48} aria-hidden="true" />
      <div>
        <span>Erro 404</span>
        <h1>Página não encontrada</h1>
        <p>O endereço informado não existe no Cidade em Foco.</p>
      </div>
      <Link className="button button--primary" to="/login">
        <ArrowLeft size={18} aria-hidden="true" />
        Voltar ao início
      </Link>
    </main>
  )
}

export default NotFoundPage
