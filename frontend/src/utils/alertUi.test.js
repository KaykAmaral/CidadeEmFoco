import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const adminPage = readFileSync(new URL('../pages/AdminAlertsPage.jsx', import.meta.url), 'utf8')
const citizenPage = readFileSync(new URL('../pages/AlertsPage.jsx', import.meta.url), 'utf8')

test('connects the administrative list to SSE and aborts it on cleanup', () => {
  assert.match(adminPage, /streamAlertUpdates\(token/)
  assert.match(adminPage, /onUpdate:\s*refreshFromSse/)
  assert.match(adminPage, /return \(\) => \{[\s\S]*?controller\.abort\(\)/)
})

test('does not expose the delete action on the citizen alerts page', () => {
  assert.doesNotMatch(citizenPage, /deleteAdminAlert|Excluir alerta|Trash2/)
  assert.match(adminPage, /canDeleteAlert\(user\.role\)/)
})
