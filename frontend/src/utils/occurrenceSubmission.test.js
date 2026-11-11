import assert from 'node:assert/strict'
import test from 'node:test'
import {
  cooldownSecondsFromError,
  formatCooldown,
  isOutsideCitizenCity,
} from './occurrenceSubmission.js'

test('recognizes only the backend city-boundary error as invalid location', () => {
  assert.equal(isOutsideCitizenCity({
    status: 422,
    message: 'A localizacao da ocorrencia deve estar dentro do municipio de Praia Grande',
  }), true)
  assert.equal(isOutsideCitizenCity({ status: 0, message: 'Não foi possível conectar' }), false)
  assert.equal(isOutsideCitizenCity({ status: 422, message: 'Voce ja contribuiu' }), false)
})

test('uses Retry-After when the API provides it', () => {
  assert.equal(cooldownSecondsFromError({ retryAfterSeconds: 42 }), 42)
})

test('uses the duration contained in the backend cooldown message', () => {
  assert.equal(cooldownSecondsFromError({
    status: 422,
    message: 'Aguarde 10 minuto(s) para registrar outra ocorrencia',
  }), 600)
  assert.equal(formatCooldown(600), '10 min')
})

test('does not invent a cooldown for unrelated failures', () => {
  assert.equal(cooldownSecondsFromError({ status: 0, message: 'Falha de rede' }), 0)
})
