import assert from 'node:assert/strict'
import test from 'node:test'
import { ApiError } from './api.js'
import {
  getWhatsappPreferences,
  removeWhatsappPreferences,
  updateWhatsappPreferences,
} from './whatsappService.js'

const originalFetch = globalThis.fetch

function jsonResponse(body, status = 200) {
  return new Response(body ? JSON.stringify(body) : null, {
    status,
    headers: body ? { 'content-type': 'application/json' } : {},
  })
}

test.afterEach(() => {
  globalThis.fetch = originalFetch
})

test('consults WhatsApp preferences with the authenticated user token', async () => {
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/users/me/whatsapp')
    assert.equal(options.headers.get('Authorization'), 'Bearer jwt-token')
    return jsonResponse({ phoneNumber: null, notificationsEnabled: false })
  }

  const response = await getWhatsappPreferences('jwt-token')
  assert.equal(response.notificationsEnabled, false)
})

test('updates phone and explicit consent and returns synchronized data', async () => {
  globalThis.fetch = async (url, options) => {
    assert.equal(options.method, 'PUT')
    assert.deepEqual(JSON.parse(options.body), {
      phoneNumber: '+5513999999999',
      consentGiven: true,
    })
    return jsonResponse({
      phoneNumber: '+5513999999999',
      notificationsEnabled: true,
      consentAt: '2026-11-11T18:00:00Z',
      deliveryEnabled: true,
    })
  }

  const response = await updateWhatsappPreferences(
    'jwt-token',
    '+5513999999999',
    true,
  )
  assert.equal(response.phoneNumber, '+5513999999999')
  assert.equal(response.notificationsEnabled, true)
})

test('preserves API validation errors from preference editing', async () => {
  globalThis.fetch = async () => jsonResponse({
    message: 'Dados invalidos',
    fieldErrors: { consentGiven: 'E necessario autorizar o recebimento de alertas pelo WhatsApp' },
  }, 400)

  await assert.rejects(
    updateWhatsappPreferences('jwt-token', '+5513999999999', false),
    (error) => error instanceof ApiError && Boolean(error.fieldErrors.consentGiven),
  )
})

test('removes preferences through the existing endpoint', async () => {
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/users/me/whatsapp')
    assert.equal(options.method, 'DELETE')
    return jsonResponse(null, 204)
  }

  assert.equal(await removeWhatsappPreferences('jwt-token'), null)
})
