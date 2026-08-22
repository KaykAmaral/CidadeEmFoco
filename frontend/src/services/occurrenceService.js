import { apiRequest } from './api'

function getOccurrences(token) {
  return apiRequest('/api/occurrences', { token })
}

export { getOccurrences }
