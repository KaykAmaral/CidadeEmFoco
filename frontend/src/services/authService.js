import { apiRequest } from './api.js'

function loginRequest(credentials) {
  return apiRequest('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  })
}

function registerRequest(citizen) {
  return apiRequest('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(citizen),
  })
}

async function registerAndLoginRequest(citizen) {
  await registerRequest(citizen)
  return loginRequest({
    email: citizen.email,
    password: citizen.password,
  })
}

export { loginRequest, registerAndLoginRequest, registerRequest }
