import { ShieldCheck, ShieldAlert, ShieldX } from 'lucide-react'

const risks = [
  { value: 'BAIXO', label: 'Baixo', description: 'Não apresenta perigo imediato', icon: ShieldCheck, tone: 'low' },
  { value: 'MEDIO', label: 'Médio', description: 'Pode causar acidentes ou prejuízos', icon: ShieldAlert, tone: 'medium' },
  { value: 'ALTO', label: 'Alto', description: 'Apresenta risco imediato para pessoas ou imóveis', icon: ShieldX, tone: 'high' },
]

function RiskSelector({ value, onChange, error }) {
  return (
    <fieldset className="risk-selector">
      <legend>Risco percebido</legend>
      <div className="risk-selector__options">
        {risks.map(({ value: risk, label, description, icon: Icon, tone }) => (
          <label className={`risk-option risk-option--${tone} ${value === risk ? 'is-selected' : ''}`} key={risk}>
            <input checked={value === risk} name="perceivedRisk" onChange={onChange} required type="radio" value={risk} />
            <Icon size={22} aria-hidden="true" /><strong>{label}</strong><span>{description}</span>
          </label>
        ))}
      </div>
      <small>Esta é a sua percepção, não uma classificação técnica.</small>
      {error && <small className="field-error">{error}</small>}
    </fieldset>
  )
}

export default RiskSelector
