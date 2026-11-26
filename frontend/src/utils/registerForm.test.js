import assert from 'node:assert/strict'
import test from 'node:test'
import {
  ACCOUNT_FIELDS,
  normalizeRegistrationErrors,
  registrationStepForErrors,
  validateRegistration,
} from './registerForm.js'

const completeForm = {
  name: 'Ana Souza', email: 'ana@example.com', cpf: '123.456.789-00', password: 'senha123',
  whatsappPhone: '+5513999999999', postalCode: '11700-000', street: 'Rua A',
  addressNumber: '10', addressComplement: '', neighborhood: 'Centro', cityIbgeCode: '3541000',
  whatsappConsentGiven: true,
}

test('validates only account fields before advancing', () => {
  const errors = validateRegistration({ ...completeForm, name: '', whatsappPhone: '' }, ACCOUNT_FIELDS)
  assert.deepEqual(errors, { name: 'Informe seu nome completo.' })
})

test('validates the complete form only on final submission', () => {
  const errors = validateRegistration({ ...completeForm, street: '', whatsappConsentGiven: false })
  assert.equal(errors.street, 'Informe o logradouro.')
  assert.ok(errors.whatsappConsentGiven)
})

test('routes API field errors to their corresponding step', () => {
  assert.equal(registrationStepForErrors({ email: 'E-mail já cadastrado' }), 1)
  assert.equal(registrationStepForErrors({ postalCode: 'CEP inválido' }), 2)
  assert.deepEqual(normalizeRegistrationErrors({ phoneNumber: 'Inválido', consentGiven: 'Obrigatório' }), {
    whatsappPhone: 'Inválido', whatsappConsentGiven: 'Obrigatório',
  })
})
