import useOccurrenceMap from '../../hooks/useOccurrenceMap'
import Button from '../ui/Button'
import FeedbackState from '../ui/FeedbackState'
import OccurrenceMap from './OccurrenceMap'
import OccurrenceCategoryFilter from './OccurrenceCategoryFilter'

function LiveOccurrenceMap({ filters = {}, refreshKey, activeCategories, onToggleCategory }) {
  const { occurrences, error, isLoading, retry } = useOccurrenceMap(filters, refreshKey)

  if (isLoading) return <FeedbackState type="loading" message="Atualizando os marcadores do mapa..." />
  if (error) return (
    <div className="live-occurrence-map">
      <FeedbackState type="error" message={error} />
      <Button onClick={retry} variant="outline">Tentar novamente</Button>
    </div>
  )

  return (
    <div className="live-occurrence-map">
      {onToggleCategory && (
        <OccurrenceCategoryFilter occurrences={occurrences} activeCategories={activeCategories} onToggle={onToggleCategory} />
      )}
      <OccurrenceMap activeCategories={activeCategories} occurrences={occurrences} />
      {occurrences.length === 0 && (
        <p className="map-empty-message" role="status">
          Nenhuma ocorrência visível no mapa para esta consulta. Registros resolvidos fora do prazo continuam disponíveis no histórico.
        </p>
      )}
    </div>
  )
}

export default LiveOccurrenceMap
