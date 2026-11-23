function onlyDigits(value, limit) {
  return value.replace(/\D/g, '').slice(0, limit)
}

function formatCpf(value) {
  const digits = onlyDigits(value, 11)

  return digits
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/^(\d{3})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3.$4')
}

function formatWhatsapp(value) {
  const digits = onlyDigits(value, 11)

  if (digits.length <= 2) return digits ? `(${digits}` : ''
  if (digits.length <= 7) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`
  return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`
}

function formatPostalCode(value) {
  const digits = onlyDigits(value, 8)

  return digits.replace(/^(\d{5})(\d)/, '$1-$2')
}

export { formatCpf, formatPostalCode, formatWhatsapp }
