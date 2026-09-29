const emptyDashboardFilters = {
  startDate: '',
  endDate: '',
  neighborhood: '',
  category: '',
  type: '',
  status: '',
}

function buildDashboardFilters(filters) {
  const { startDate, endDate, ...parameters } = filters
  if (startDate && endDate && startDate > endDate) {
    throw new Error('A data final deve ser igual ou posterior à data inicial.')
  }

  parameters.neighborhood = parameters.neighborhood.trim()
  // Interpret calendar dates in the browser's timezone, then send UTC instants.
  if (startDate) parameters.createdFrom = new Date(`${startDate}T00:00:00`).toISOString()
  if (endDate) {
    const nextDay = new Date(`${endDate}T00:00:00`)
    nextDay.setDate(nextDay.getDate() + 1)
    parameters.createdBefore = nextDay.toISOString()
  }
  return parameters
}

export { buildDashboardFilters, emptyDashboardFilters }
