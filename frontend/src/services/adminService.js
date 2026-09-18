import { apiRequest } from './api'

function buildQuery(filters = {}) {
  const params = new URLSearchParams()

  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })

  const query = params.toString()
  return query ? `?${query}` : ''
}

function getAdminOccurrences(token, filters = {}) {
  return apiRequest(`/api/admin/occurrences${buildQuery(filters)}`, { token })
}

function getAdminOccurrenceById(token, id) {
  return apiRequest(`/api/admin/occurrences/${id}`, { token })
}

function updateAdminOccurrenceStatus(token, id, status) {
  return apiRequest(`/api/admin/occurrences/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
    token,
  })
}

function getAdminAlerts(token) {
  return apiRequest('/api/admin/alerts', { token })
}

function createAdminAlert(token, alert) {
  return apiRequest('/api/admin/alerts', {
    method: 'POST',
    body: JSON.stringify(alert),
    token,
  })
}

function activateAdminAlert(token, id) {
  return apiRequest(`/api/admin/alerts/${id}/activate`, {
    method: 'PATCH',
    token,
  })
}

function deactivateAdminAlert(token, id) {
  return apiRequest(`/api/admin/alerts/${id}/deactivate`, {
    method: 'PATCH',
    token,
  })
}

export {
  activateAdminAlert,
  createAdminAlert,
  deactivateAdminAlert,
  getAdminAlerts,
  getAdminOccurrenceById,
  getAdminOccurrences,
  updateAdminOccurrenceStatus,
}
