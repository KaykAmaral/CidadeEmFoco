import {
  occurrenceCategoryLabels,
  occurrenceCategoryPresentation,
  occurrenceTypeLabels,
  occurrenceTypesByCategory,
} from '../../constants/occurrencePresentation'
import OccurrenceTypeIcon from './OccurrenceTypeIcon'

function OccurrenceClassificationSelector({ category, type, onChange, fieldErrors = {} }) {
  const availableTypes = category ? occurrenceTypesByCategory[category] : []
  return (
    <div className="classification-selector">
      <fieldset>
        <legend>Categoria</legend>
        <div className="classification-options classification-options--categories">
          {Object.entries(occurrenceCategoryLabels).map(([value, label]) => {
            const categoryConfig = occurrenceCategoryPresentation[value]
            const Icon = categoryConfig.icon
            return (
              <label className={`classification-option ${category === value ? 'is-selected' : ''}`} key={value}>
                <input checked={category === value} name="category" onChange={onChange} required type="radio" value={value} />
                <span className="classification-option__category-icon" style={{ color: categoryConfig.color, background: categoryConfig.softColor }}><Icon size={21} aria-hidden="true" /></span>
                <span>{label}</span>
              </label>
            )
          })}
        </div>
        {fieldErrors.category && <small className="field-error">{fieldErrors.category}</small>}
      </fieldset>

      <fieldset disabled={!category}>
        <legend>Tipo da ocorrência</legend>
        {!category && <p className="classification-selector__hint">Escolha uma categoria para visualizar os tipos.</p>}
        <div className="classification-options classification-options--types">
          {availableTypes.map((value) => (
            <label className={`classification-option ${type === value ? 'is-selected' : ''}`} key={value}>
              <input checked={type === value} name="type" onChange={onChange} required type="radio" value={value} />
              <OccurrenceTypeIcon size={19} type={value} />
              <span>{occurrenceTypeLabels[value]}</span>
            </label>
          ))}
        </div>
        {fieldErrors.type && <small className="field-error">{fieldErrors.type}</small>}
      </fieldset>
    </div>
  )
}

export default OccurrenceClassificationSelector
