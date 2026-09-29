import assert from 'node:assert/strict'
import test from 'node:test'
import { buildDashboardFilters, emptyDashboardFilters } from './adminDashboardFilters.js'

process.env.TZ = 'America/Sao_Paulo'

test('empty filters preserve the existing unfiltered request', () => {
  assert.deepEqual(buildDashboardFilters(emptyDashboardFilters), {
    neighborhood: '', category: '', type: '', status: '',
  })
})

test('combines all filters and includes the whole last day in local time', () => {
  assert.deepEqual(buildDashboardFilters({
    startDate: '2026-09-01', endDate: '2026-09-28', neighborhood: '  Boqueirão  ',
    category: 'EVENTO_NATURAL', type: 'ALAGAMENTO', status: 'REGISTRADA',
  }), {
    neighborhood: 'Boqueirão', category: 'EVENTO_NATURAL', type: 'ALAGAMENTO', status: 'REGISTRADA',
    createdFrom: '2026-09-01T03:00:00.000Z', createdBefore: '2026-09-29T03:00:00.000Z',
  })
})

test('allows a single day and advances across month and year boundaries', () => {
  const filters = buildDashboardFilters({ ...emptyDashboardFilters, startDate: '2026-12-31', endDate: '2026-12-31' })
  assert.equal(filters.createdFrom, '2026-12-31T03:00:00.000Z')
  assert.equal(filters.createdBefore, '2027-01-01T03:00:00.000Z')
})

test('allows either date boundary independently', () => {
  const startOnly = buildDashboardFilters({ ...emptyDashboardFilters, startDate: '2026-09-28' })
  const endOnly = buildDashboardFilters({ ...emptyDashboardFilters, endDate: '2026-09-28' })
  assert.equal(startOnly.createdFrom, '2026-09-28T03:00:00.000Z')
  assert.equal(startOnly.createdBefore, undefined)
  assert.equal(endOnly.createdFrom, undefined)
  assert.equal(endOnly.createdBefore, '2026-09-29T03:00:00.000Z')
})

test('rejects a reversed period', () => {
  assert.throws(() => buildDashboardFilters({
    ...emptyDashboardFilters, startDate: '2026-09-29', endDate: '2026-09-28',
  }), /data final/)
})

test('does not mutate the form state', () => {
  const filters = Object.freeze({ ...emptyDashboardFilters, neighborhood: ' Centro ' })
  assert.equal(buildDashboardFilters(filters).neighborhood, 'Centro')
  assert.equal(filters.neighborhood, ' Centro ')
})
