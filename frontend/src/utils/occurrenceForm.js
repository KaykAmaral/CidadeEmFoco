const CLASSIFICATION_FIELDS = ['category', 'type', 'perceivedRisk', 'description']
const LOCATION_FIELDS = ['latitude', 'longitude', 'neighborhood', 'address']

function normalizeText(value = '') {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim().toLowerCase()
}

function validateOccurrenceStepOne(form) {
  const errors = {}
  if (!form.category) errors.category = 'Selecione uma categoria.'
  if (!form.type) errors.type = 'Selecione o tipo da ocorrência.'
  if (!form.perceivedRisk) errors.perceivedRisk = 'Selecione o risco percebido.'
  if (!form.description?.trim()) errors.description = 'Descreva o que aconteceu.'
  else if (form.description.length > 2000) errors.description = 'A descrição deve ter no máximo 2000 caracteres.'
  return errors
}

function validateOccurrenceStepTwo(form, selectedCity, citizenCity) {
  const errors = {}
  const latitude = Number(form.latitude)
  const longitude = Number(form.longitude)
  if (form.latitude === '' || !Number.isFinite(latitude) || latitude < -90 || latitude > 90) errors.latitude = 'Selecione um ponto válido no mapa.'
  if (form.longitude === '' || !Number.isFinite(longitude) || longitude < -180 || longitude > 180) errors.longitude = 'Selecione um ponto válido no mapa.'
  if (form.neighborhood?.length > 100) errors.neighborhood = 'O bairro deve ter no máximo 100 caracteres.'
  if (form.address?.length > 255) errors.address = 'O endereço deve ter no máximo 255 caracteres.'
  if (selectedCity && citizenCity && normalizeText(selectedCity) !== normalizeText(citizenCity)) errors.location = `O ponto precisa estar dentro de ${citizenCity}.`
  return errors
}

function occurrenceStepForErrors(fieldErrors = {}) {
  const fields = Object.keys(fieldErrors)
  if (fields.some((field) => CLASSIFICATION_FIELDS.includes(field))) return 1
  if (fields.some((field) => LOCATION_FIELDS.includes(field) || field === 'location')) return 2
  return 3
}

export { occurrenceStepForErrors, validateOccurrenceStepOne, validateOccurrenceStepTwo }
