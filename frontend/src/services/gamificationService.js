import { apiRequest } from './api'

function getMyGamification(token) {
  return apiRequest('/api/gamification/me', { token })
}

function getCitizenRanking(token, period = 'MONTH') {
  return apiRequest(`/api/gamification/ranking?period=${encodeURIComponent(period)}`, { token })
}

function getCityRanking(token, period = 'MONTH') {
  return apiRequest(`/api/rankings/cities?period=${encodeURIComponent(period)}`, { token })
}

export { getCitizenRanking, getCityRanking, getMyGamification }
