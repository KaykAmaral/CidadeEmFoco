import assert from 'node:assert/strict'
import test from 'node:test'
import { occurrenceStepForErrors, validateOccurrenceStepOne, validateOccurrenceStepTwo } from './occurrenceForm.js'

const validForm = { category: 'EVENTO_NATURAL', type: 'ALAGAMENTO', perceivedRisk: 'MEDIO', description: 'Rua alagada', latitude: '-24.005', longitude: '-46.405', neighborhood: 'Centro', address: 'Rua Exemplo, 10' }

test('a primeira etapa valida somente classificação e descrição', () => {
  assert.deepEqual(validateOccurrenceStepOne({ ...validForm, category: '', description: ' ' }), { category: 'Selecione uma categoria.', description: 'Descreva o que aconteceu.' })
})

test('a segunda etapa exige coordenadas válidas e respeita a cidade identificada', () => {
  assert.deepEqual(validateOccurrenceStepTwo({ ...validForm, latitude: '' }, 'Praia Grande', 'Praia Grande'), { latitude: 'Selecione um ponto válido no mapa.' })
  assert.deepEqual(validateOccurrenceStepTwo(validForm, 'Santos', 'Praia Grande'), { location: 'O ponto precisa estar dentro de Praia Grande.' })
})

test('erros da API direcionam para a etapa correspondente', () => {
  assert.equal(occurrenceStepForErrors({ description: 'Inválida' }), 1)
  assert.equal(occurrenceStepForErrors({ longitude: 'Inválida' }), 2)
  assert.equal(occurrenceStepForErrors({}), 3)
})
