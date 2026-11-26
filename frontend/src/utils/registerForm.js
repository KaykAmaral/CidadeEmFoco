const ACCOUNT_FIELDS = ['name', 'email', 'cpf', 'password']
const CONTACT_FIELDS = [
  'whatsappPhone',
  'postalCode',
  'street',
  'addressNumber',
  'addressComplement',
  'neighborhood',
  'cityIbgeCode',
  'whatsappConsentGiven',
]

function required(value, message) {
  return String(value ?? '').trim() ? '' : message
}

function validateRegistration(form, fields = [...ACCOUNT_FIELDS, ...CONTACT_FIELDS]) {
  const errors = {}
  const checks = {
    name: () => {
      const missing = required(form.name, 'Informe seu nome completo.')
      if (missing) return missing
      return form.name.trim().length < 2 ? 'O nome deve ter entre 2 e 100 caracteres.' : ''
    },
    email: () => {
      const missing = required(form.email, 'Informe seu e-mail.')
      if (missing) return missing
      return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim()) ? '' : 'Informe um e-mail válido.'
    },
    cpf: () => required(form.cpf, 'Informe seu CPF.'),
    password: () => {
      const missing = required(form.password, 'Crie uma senha.')
      if (missing) return missing
      return form.password.length < 8 || form.password.length > 72
        ? 'A senha deve ter entre 8 e 72 caracteres.'
        : ''
    },
    whatsappPhone: () => required(form.whatsappPhone, 'Informe seu WhatsApp.'),
    postalCode: () => required(form.postalCode, 'Informe seu CEP.'),
    street: () => required(form.street, 'Informe o logradouro.'),
    addressNumber: () => required(form.addressNumber, 'Informe o número do endereço.'),
    addressComplement: () => '',
    neighborhood: () => required(form.neighborhood, 'Informe o bairro.'),
    cityIbgeCode: () => required(form.cityIbgeCode, 'Selecione a cidade.'),
    whatsappConsentGiven: () => form.whatsappConsentGiven
      ? ''
      : 'Autorize o recebimento de alertas climáticos pelo WhatsApp.',
  }

  fields.forEach((field) => {
    const message = checks[field]?.()
    if (message) errors[field] = message
  })
  return errors
}

function registrationStepForErrors(fieldErrors = {}) {
  return Object.keys(fieldErrors).some((field) => ACCOUNT_FIELDS.includes(field)) ? 1 : 2
}

function normalizeRegistrationErrors(fieldErrors = {}) {
  const aliases = {
    consentGiven: 'whatsappConsentGiven',
    phoneNumber: 'whatsappPhone',
  }
  return Object.fromEntries(
    Object.entries(fieldErrors).map(([field, message]) => [aliases[field] ?? field, message]),
  )
}

export {
  ACCOUNT_FIELDS,
  CONTACT_FIELDS,
  normalizeRegistrationErrors,
  registrationStepForErrors,
  validateRegistration,
}
