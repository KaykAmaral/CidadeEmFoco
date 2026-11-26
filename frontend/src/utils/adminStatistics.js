const DAY_MS = 24 * 60 * 60 * 1000

const emptyStatisticsFilters = {
  period: '30', startDate: '', endDate: '', category: '', type: '', status: '', neighborhood: '',
}

function buildStatisticsApiFilters(filters, now = new Date()) {
  const apiFilters = {
    category: filters.category,
    type: filters.type,
    status: filters.status,
    neighborhood: filters.neighborhood.trim(),
  }

  if (filters.period === 'CUSTOM') {
    if (!filters.startDate || !filters.endDate) throw new Error('Informe as datas inicial e final do período.')
    if (filters.startDate > filters.endDate) throw new Error('A data inicial deve ser anterior à data final.')
    apiFilters.createdFrom = new Date(`${filters.startDate}T00:00:00`).toISOString()
    apiFilters.createdTo = new Date(`${filters.endDate}T23:59:59.999`).toISOString()
  } else if (filters.period !== 'ALL') {
    const createdFrom = new Date(now)
    createdFrom.setHours(0, 0, 0, 0)
    createdFrom.setDate(createdFrom.getDate() - (Number(filters.period) - 1))
    apiFilters.createdFrom = createdFrom.toISOString()
    apiFilters.createdTo = new Date(now).toISOString()
  }

  return apiFilters
}

function startOfWeek(date) {
  const value = new Date(`${date}T00:00:00`)
  value.setDate(value.getDate() - ((value.getDay() + 6) % 7))
  return value.toISOString().slice(0, 10)
}

function startOfMonth(date) {
  return `${date.slice(0, 7)}-01`
}

function evolutionGrouping(entries, createdFrom, createdTo) {
  if (!entries.length) return 'day'
  const first = createdFrom ? new Date(createdFrom) : new Date(`${entries[0].date}T00:00:00`)
  const last = createdTo ? new Date(createdTo) : new Date(`${entries.at(-1).date}T00:00:00`)
  const days = Math.max(1, Math.ceil((last - first) / DAY_MS))
  if (days <= 31) return 'day'
  if (days <= 180) return 'week'
  return 'month'
}

function aggregateEvolution(entries = [], createdFrom, createdTo) {
  const grouping = evolutionGrouping(entries, createdFrom, createdTo)
  const buckets = new Map()

  entries.forEach((entry) => {
    const key = grouping === 'day' ? entry.date : grouping === 'week' ? startOfWeek(entry.date) : startOfMonth(entry.date)
    const current = buckets.get(key) ?? { key, registeredCases: 0, resolvedCases: 0 }
    current.registeredCases += entry.registeredCases
    current.resolvedCases += entry.resolvedCases
    buckets.set(key, current)
  })

  return [...buckets.values()].map((entry) => ({
    ...entry,
    label: grouping === 'month'
      ? new Date(`${entry.key}T00:00:00`).toLocaleDateString('pt-BR', { month: 'short', year: '2-digit' }).replace('.', '')
      : new Date(`${entry.key}T00:00:00`).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' }),
    fullLabel: grouping === 'day' ? entry.key : `${grouping === 'week' ? 'Semana de' : 'Mês de'} ${entry.key}`,
  }))
}

function formatResolutionTime(minutes) {
  if (minutes === null || minutes === undefined) return null
  if (minutes < 60) return { value: Math.round(minutes), unit: 'minutos' }
  if (minutes < 1440) return { value: (minutes / 60).toLocaleString('pt-BR', { maximumFractionDigits: 1 }), unit: 'horas' }
  return { value: (minutes / 1440).toLocaleString('pt-BR', { maximumFractionDigits: 1 }), unit: 'dias' }
}

export { aggregateEvolution, buildStatisticsApiFilters, emptyStatisticsFilters, formatResolutionTime }
