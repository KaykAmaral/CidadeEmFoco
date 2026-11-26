import { Moon, ShieldCheck, Sun } from 'lucide-react'
import { getDayPeriod } from '../../utils/alertVisuals'
import './CalmCityIllustration.css'

const periodLabels = {
  day: 'Cidade tranquila durante o dia',
  afternoon: 'Cidade tranquila ao entardecer',
  night: 'Cidade tranquila durante a noite',
}

function CalmCityIllustration({ period = getDayPeriod() }) {
  const PeriodIcon = period === 'night' ? Moon : Sun

  return (
    <section className={`calm-city calm-city--${period}`} aria-label={periodLabels[period]}>
      <div className="calm-city__sky" aria-hidden="true">
        <PeriodIcon className="calm-city__sun" size={44} strokeWidth={1.5} />
        <span className="calm-city__cloud calm-city__cloud--one" />
        <span className="calm-city__cloud calm-city__cloud--two" />
        <div className="calm-city__buildings">
          <span /><span /><span /><span /><span />
        </div>
        <div className="calm-city__ground" />
      </div>
      <div className="calm-city__content">
        <ShieldCheck size={25} aria-hidden="true" />
        <div><strong>Cidade sem alertas ativos</strong><p>Não há alertas válidos neste momento. Continue acompanhando as atualizações.</p></div>
      </div>
    </section>
  )
}

export default CalmCityIllustration
