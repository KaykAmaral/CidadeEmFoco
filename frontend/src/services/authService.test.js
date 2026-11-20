import assert from 'node:assert/strict'
import test from 'node:test'
import { ApiError } from './api.js'
import { loginRequest, registerAndLoginRequest, registerRequest } from './authService.js'

const originalFetch = globalThis.fetch

function jsonResponse(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json' },
  })
}

test.afterEach(() => {
  globalThis.fetch = originalFetch
})

test('sends the complete registration contract and returns the authenticated session', async () => {
  const citizen = {
    name: 'Ana', email: 'ana@example.com', password: 'senha123', cpf: '52998224725',
    whatsappPhone: '+5513999999999', whatsappConsentGiven: true, street: 'Rua A',
    addressNumber: '10', addressComplement: '', neighborhood: 'Centro',
    postalCode: '11700-000', cityIbgeCode: '3541000',
  }
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/auth/register')
    assert.equal(options.method, 'POST')
    assert.deepEqual(JSON.parse(options.body), citizen)
    return jsonResponse({ token: 'jwt-token', user: { id: 1, role: 'CITIZEN' } }, 201)
  }

  const response = await registerRequest(citizen)
  assert.equal(response.token, 'jwt-token')
})

test('preserves API field validation errors', async () => {
  globalThis.fetch = async () => jsonResponse({
    message: 'Dados invalidos',
    fieldErrors: { email: 'Informe um email valido' },
  }, 400)

  await assert.rejects(
    registerRequest({ name: 'Ana', email: 'invalido', password: 'senha123' }),
    (error) => error instanceof ApiError
      && error.message === 'Dados invalidos'
      && error.fieldErrors.email === 'Informe um email valido',
  )
})

test('uses the existing login endpoint to obtain the automatic-login token', async () => {
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/auth/login')
    assert.deepEqual(JSON.parse(options.body), {
      email: 'ana@example.com',
      password: 'senha123',
    })
    return jsonResponse({
      token: 'jwt-token',
      tokenType: 'Bearer',
      expiresInSeconds: 3600,
      user: { role: 'CITIZEN' },
    })
  }

  const response = await loginRequest({ email: 'ana@example.com', password: 'senha123' })
  assert.equal(response.token, 'jwt-token')
})

test('uses the authenticated response returned directly by registration', async () => {
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, body: JSON.parse(options.body) })
    return jsonResponse({
      token: 'jwt-token',
      tokenType: 'Bearer',
      expiresInSeconds: 3600,
      user: { id: 1, role: 'CITIZEN' },
    })
  }

  const citizen = { name: 'Ana', email: 'ana@example.com', password: 'senha123' }
  const response = await registerAndLoginRequest(citizen)

  assert.deepEqual(calls, [{ url: '/api/auth/register', body: citizen }])
  assert.equal(response.token, 'jwt-token')
})
