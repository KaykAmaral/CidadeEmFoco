import assert from 'node:assert/strict'
import test from 'node:test'
import { ApiError } from './api.js'
import { createOccurrence } from './occurrenceService.js'

const originalFetch = globalThis.fetch

function jsonResponse(body, status = 200, headers = {}) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json', ...headers },
  })
}

test.afterEach(() => {
  globalThis.fetch = originalFetch
})

test('returns the occurrence after a successful submission', async () => {
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/occurrences')
    assert.equal(options.method, 'POST')
    assert.equal(options.headers.get('Authorization'), 'Bearer jwt-token')
    return jsonResponse({ id: 25, status: 'REGISTRADA' }, 201)
  }

  const response = await createOccurrence('jwt-token', { latitude: -24, longitude: -46.4 })
  assert.equal(response.id, 25)
})

test('preserves the real invalid-coordinate response', async () => {
  globalThis.fetch = async () => jsonResponse({
    message: 'A localizacao da ocorrencia deve estar dentro do municipio de Praia Grande',
    fieldErrors: {},
  }, 422)

  await assert.rejects(
    createOccurrence('jwt-token', { latitude: -23.9, longitude: -46.3 }),
    (error) => error instanceof ApiError
      && error.status === 422
      && error.message.includes('Praia Grande'),
  )
})

test('preserves cooldown message and Retry-After from the API', async () => {
  globalThis.fetch = async () => jsonResponse({
    message: 'Aguarde 10 minuto(s) para registrar outra ocorrencia',
  }, 422, { 'retry-after': '600' })

  await assert.rejects(
    createOccurrence('jwt-token', {}),
    (error) => error.message.startsWith('Aguarde 10') && error.retryAfterSeconds === 600,
  )
})

test('allows a new attempt immediately after a network failure', async () => {
  globalThis.fetch = async () => { throw new TypeError('network failure') }

  await assert.rejects(
    createOccurrence('jwt-token', {}),
    (error) => error instanceof ApiError && error.status === 0 && error.retryAfterSeconds === 0,
  )
})
