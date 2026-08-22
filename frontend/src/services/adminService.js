import { apiRequest } from './api'

function getAdminOccurrences(token) {
  return apiRequest('/api/admin/occurrences', { token })
}

function getAdminAlerts(token) {
  return apiRequest('/api/admin/alerts', { token })
}

export { getAdminAlerts, getAdminOccurrences }
