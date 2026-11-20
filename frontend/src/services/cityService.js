import { apiRequest } from './api.js'

function getActiveCities() {
  return apiRequest('/api/cities')
}

export { getActiveCities }
