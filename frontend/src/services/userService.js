import { apiRequest } from './api'

function getMyProfile(token) {
  return apiRequest('/api/users/me', { token })
}

function updateMyProfile(token, profile) {
  return apiRequest('/api/users/me', {
    method: 'PUT',
    body: JSON.stringify(profile),
    token,
  })
}

export { getMyProfile, updateMyProfile }
