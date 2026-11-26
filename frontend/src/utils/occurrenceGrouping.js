function getOccurrenceGrouping(occurrence) {
  const strength = Math.max(1, Number(occurrence?.strength) || 1)
  return {
    grouped: strength > 1,
    label: strength > 1 ? `${strength} relatos agrupados` : 'Relato individual',
    strength,
  }
}

export { getOccurrenceGrouping }
