import assert from 'node:assert/strict'
import test from 'node:test'
import { lookupPostalCode } from './postalCodeService.js'

test('maps the ViaCEP response to the registration address', async (context) => {
  context.mock.method(globalThis, 'fetch', async (url) => {
    assert.equal(url, 'https://viacep.com.br/ws/11704900/json/')
    return new Response(JSON.stringify({
      bairro: 'Centro', ibge: '3541000', localidade: 'Praia Grande',
      logradouro: 'Avenida Presidente Kennedy', uf: 'SP',
    }))
  })

  assert.deepEqual(await lookupPostalCode('11704-900'), {
    cityIbgeCode: '3541000', cityName: 'Praia Grande', neighborhood: 'Centro',
    stateCode: 'SP', street: 'Avenida Presidente Kennedy',
  })
})

test('rejects a postal code not found by ViaCEP', async (context) => {
  context.mock.method(globalThis, 'fetch', async () => new Response(JSON.stringify({ erro: true })))
  await assert.rejects(() => lookupPostalCode('00000-000'), /CEP não encontrado/)
})
