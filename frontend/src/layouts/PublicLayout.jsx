import { Outlet } from 'react-router'
import Brand from '../components/ui/Brand'

function PublicLayout() {
  return (
    <main className="public-layout">
      <section className="public-layout__identity">
        <Brand inverse />
        <div>
          <span>Praia Grande — SP</span>
          <h1>Informação e participação para cuidar da cidade.</h1>
          <p>
            Registre ocorrências urbanas e acompanhe alertas climáticos em um
            único lugar.
          </p>
        </div>
      </section>

      <section className="public-layout__content">
        <div className="public-layout__mobile-brand">
          <Brand />
        </div>
        <Outlet />
      </section>
    </main>
  )
}

export default PublicLayout
