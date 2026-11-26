import assert from 'node:assert/strict'
import test from 'node:test'
import { aggregateEvolution, buildStatisticsApiFilters, emptyStatisticsFilters, formatResolutionTime } from './adminStatistics.js'

process.env.TZ = 'America/Sao_Paulo'

test('builds statistics filters with the same rolling-period behavior as overview', () => {
  const result = buildStatisticsApiFilters({ ...emptyStatisticsFilters, period: '7', neighborhood: ' Centro ' }, new Date('2026-11-26T12:00:00-03:00'))
  assert.equal(result.neighborhood, 'Centro')
  assert.equal(result.createdFrom, '2026-11-20T03:00:00.000Z')
  assert.equal(result.createdTo, '2026-11-26T15:00:00.000Z')
})

test('aggregates daily evolution into weeks for medium periods', () => {
  const entries = [
    { date: '2026-09-01', registeredCases: 2, resolvedCases: 1 },
    { date: '2026-09-02', registeredCases: 3, resolvedCases: 2 },
  ]
  const result = aggregateEvolution(entries, '2026-09-01T00:00:00Z', '2026-11-30T00:00:00Z')
  assert.deepEqual(result.map(({ registeredCases, resolvedCases }) => ({ registeredCases, resolvedCases })), [{ registeredCases: 5, resolvedCases: 3 }])
})

test('formats valid resolution averages without inventing missing values', () => {
  assert.deepEqual(formatResolutionTime(null), null)
  assert.deepEqual(formatResolutionTime(90), { value: '1,5', unit: 'horas' })
  assert.deepEqual(formatResolutionTime(2880), { value: '2', unit: 'dias' })
})
