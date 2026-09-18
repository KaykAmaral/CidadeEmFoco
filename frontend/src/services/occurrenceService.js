import { apiRequest } from './api'

function buildQuery(filters = {}) {
  const params = new URLSearchParams()

  Object.entries(filters).forEach(([key, value]) => {
    if (value) {
      params.set(key, value)
    }
  })

  const query = params.toString()
  return query ? `?${query}` : ''
}

function getOccurrences(token, filters = {}) {
  return apiRequest(`/api/occurrences${buildQuery(filters)}`, { token })
}

function getMyOccurrences(token) {
  return apiRequest('/api/occurrences/mine', { token })
}

function getOccurrenceById(token, id) {
  return apiRequest(`/api/occurrences/${id}`, { token })
}

function createOccurrence(token, occurrence) {
  return apiRequest('/api/occurrences', {
    method: 'POST',
    body: JSON.stringify(occurrence),
    token,
  })
}

function uploadOccurrenceImage(token, id, file) {
  const formData = new FormData()
  formData.append('file', file)

  return apiRequest(`/api/occurrences/${id}/image`, {
    method: 'POST',
    body: formData,
    token,
  })
}

function uploadOccurrenceImages(token, id, files) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return apiRequest(`/api/occurrences/${id}/images`, { method: 'POST', body: formData, token })
}

function getOccurrenceImage(token, id, imageUrl) {
  return apiRequest(imageUrl || `/api/occurrences/${id}/image`, {
    responseType: 'blob',
    token,
  })
}

export {
  createOccurrence,
  getMyOccurrences,
  getOccurrenceById,
  getOccurrenceImage,
  getOccurrences,
  uploadOccurrenceImage,
  uploadOccurrenceImages,
}
