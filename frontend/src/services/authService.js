import { apiRequest } from './api'

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

export { loginRequest, registerRequest }
