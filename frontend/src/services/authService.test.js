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

test('sends only the registration contract and returns the created citizen', async () => {
  const citizen = { name: 'Ana', email: 'ana@example.com', password: 'senha123' }
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/auth/register')
    assert.equal(options.method, 'POST')
    assert.deepEqual(JSON.parse(options.body), citizen)
    return jsonResponse({ id: 1, name: 'Ana', email: citizen.email, role: 'CITIZEN' }, 201)
  }

  const response = await registerRequest(citizen)
  assert.equal(response.role, 'CITIZEN')
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

test('registers before logging in automatically', async () => {
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, body: JSON.parse(options.body) })
    if (url.endsWith('/register')) {
      return jsonResponse({ id: 1, role: 'CITIZEN' }, 201)
    }
    return jsonResponse({
      token: 'jwt-token',
      tokenType: 'Bearer',
      expiresInSeconds: 3600,
      user: { id: 1, role: 'CITIZEN' },
    })
  }

  const citizen = { name: 'Ana', email: 'ana@example.com', password: 'senha123' }
  const response = await registerAndLoginRequest(citizen)

  assert.deepEqual(calls, [
    { url: '/api/auth/register', body: citizen },
    {
      url: '/api/auth/login',
      body: { email: citizen.email, password: citizen.password },
    },
  ])
  assert.equal(response.token, 'jwt-token')
})
