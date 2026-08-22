import { apiRequest } from './api'

function getActiveAlerts(token) {
  return apiRequest('/api/alerts/active', { token })
}

export { getActiveAlerts }
