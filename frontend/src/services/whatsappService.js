import { apiRequest } from './api'

function getWhatsappPreferences(token) {
  return apiRequest('/api/users/me/whatsapp', { token })
}

function updateWhatsappPreferences(token, phoneNumber, consentGiven) {
  return apiRequest('/api/users/me/whatsapp', {
    method: 'PUT',
    body: JSON.stringify({ phoneNumber, consentGiven }),
    token,
  })
}

function removeWhatsappPreferences(token) {
  return apiRequest('/api/users/me/whatsapp', {
    method: 'DELETE',
    token,
  })
}

export {
  getWhatsappPreferences,
  removeWhatsappPreferences,
  updateWhatsappPreferences,
}
