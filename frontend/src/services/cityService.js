import { apiRequest } from './api.js'

function getActiveCities() {
  return apiRequest('/api/cities')
}

function getCityBoundary(token, ibgeCode) {
  return apiRequest(`/api/cities/${encodeURIComponent(ibgeCode)}/boundary`, { token })
}

export { getActiveCities, getCityBoundary }
