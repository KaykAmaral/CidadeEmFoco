async function lookupPostalCode(postalCode, signal) {
  const digits = postalCode.replace(/\D/g, '')

  if (digits.length !== 8) {
    throw new Error('Informe um CEP com oito números.')
  }

  const response = await fetch(`https://viacep.com.br/ws/${digits}/json/`, { signal })
  if (!response.ok) throw new Error('Não foi possível consultar o CEP.')

  const data = await response.json()
  if (data.erro) throw new Error('CEP não encontrado.')

  return {
    cityIbgeCode: data.ibge,
    cityName: data.localidade,
    neighborhood: data.bairro,
    stateCode: data.uf,
    street: data.logradouro,
  }
}

export { lookupPostalCode }
