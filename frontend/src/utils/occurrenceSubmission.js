function normalizeMessage(message = '') {
  return message.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
}

function isOutsideCitizenCity(error) {
  const message = normalizeMessage(error?.message)
  return error?.status === 422
    && message.includes('localizacao da ocorrencia deve estar dentro do municipio')
}

function cooldownSecondsFromError(error) {
  if (Number.isFinite(error?.retryAfterSeconds) && error.retryAfterSeconds > 0) {
    return Math.ceil(error.retryAfterSeconds)
  }

  const match = error?.message?.match(/aguarde\s+(\d+)\s+minuto(?:\(s\)|s)?/i)
  return match ? Number(match[1]) * 60 : 0
}

function formatCooldown(seconds) {
  if (seconds < 60) return `${seconds}s`
  const minutes = Math.ceil(seconds / 60)
  return `${minutes} min`
}

export { cooldownSecondsFromError, formatCooldown, isOutsideCitizenCity }
