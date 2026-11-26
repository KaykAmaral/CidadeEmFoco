import assert from 'node:assert/strict'
import test from 'node:test'
import { createSession } from './authSession.js'

test('creates an authenticated session from the login response', () => {
  const session = createSession({
    token: 'jwt-token',
    tokenType: 'Bearer',
    expiresInSeconds: 3600,
    user: { id: 1, role: 'CITIZEN' },
  }, 1_000)

  assert.deepEqual(session, {
    token: 'jwt-token',
    tokenType: 'Bearer',
    expiresAt: 3_601_000,
    user: { id: 1, role: 'CITIZEN' },
  })
})
