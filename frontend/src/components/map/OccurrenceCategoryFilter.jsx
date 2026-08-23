import { occurrenceCategoryPresentation } from '../../constants/occurrencePresentation'

function OccurrenceCategoryFilter({ occurrences, activeCategories, onToggle }) {
  return (
    <div className="map-category-filter" aria-label="Filtrar ocorrências por categoria">
      {Object.entries(occurrenceCategoryPresentation).map(([category, config]) => {
        const Icon = config.icon
        const active = activeCategories.includes(category)
        const count = occurrences.filter((occurrence) => occurrence.category === category).length
        return (
          <button
            aria-pressed={active}
            className={`map-category-filter__option ${active ? 'is-active' : 'is-inactive'}`}
            key={category}
            onClick={() => onToggle(category)}
            style={{ '--category-color': config.color, '--category-soft-color': config.softColor }}
            title={`Filtrar por ${config.label}`}
            type="button"
          >
            <span><Icon size={16} aria-hidden="true" /></span>
            {config.label} ({count})
          </button>
        )
      })}
    </div>
  )
}

export default OccurrenceCategoryFilter
