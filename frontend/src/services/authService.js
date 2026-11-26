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

function forgotPasswordRequest(email) {
  return apiRequest('/api/auth/forgot-password', {
    method: 'POST',
    body: JSON.stringify({ email }),
  })
}

function resetPasswordRequest(token, newPassword) {
  return apiRequest('/api/auth/reset-password', {
    method: 'POST',
    body: JSON.stringify({ token, newPassword }),
  })
}

async function registerAndLoginRequest(citizen) {
  return registerRequest(citizen)
}

export {
  forgotPasswordRequest,
  loginRequest,
  registerAndLoginRequest,
  registerRequest,
  resetPasswordRequest,
}
