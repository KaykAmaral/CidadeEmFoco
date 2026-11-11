import assert from 'node:assert/strict'
import test from 'node:test'
import { canDeleteAlert, isLatestAlertRequest, synchronizeAlerts } from './alertSync.js'

test('synchronizes an SSE snapshot without duplicated alerts', () => {
  const synchronized = synchronizeAlerts([
    { id: 1, title: 'Chuva' },
    { id: 1, title: 'Chuva atualizada' },
    { id: 2, title: 'Vento' },
  ])
  assert.deepEqual(synchronized, [
    { id: 1, title: 'Chuva atualizada' },
    { id: 2, title: 'Vento' },
  ])
})

test('does not restore a deleted alert from an old response', () => {
  assert.deepEqual(
    synchronizeAlerts([{ id: 4, title: 'Excluído' }], new Set([4])),
    [],
  )
  assert.equal(isLatestAlertRequest(2, 3), false)
})

test('allows exclusion only for administrators', () => {
  assert.equal(canDeleteAlert('ADMIN'), true)
  assert.equal(canDeleteAlert('CITIZEN'), false)
})
