import assert from 'node:assert/strict'
import test from 'node:test'
import { formatCpf, formatPostalCode, formatWhatsapp } from './inputMasks.js'

test('formats CPF while it is typed', () => {
  assert.equal(formatCpf('12345678901'), '123.456.789.01')
  assert.equal(formatCpf('123.4'), '123.4')
  assert.equal(formatCpf('1234567890123'), '123.456.789.01')
})

test('formats WhatsApp while it is typed', () => {
  assert.equal(formatWhatsapp('13999999999'), '(13) 99999-9999')
  assert.equal(formatWhatsapp('(13) 999'), '(13) 999')
  assert.equal(formatWhatsapp('1399999999999'), '(13) 99999-9999')
})

test('formats postal code while it is typed', () => {
  assert.equal(formatPostalCode('11700000'), '11700-000')
  assert.equal(formatPostalCode('11700-0'), '11700-0')
  assert.equal(formatPostalCode('1170000012'), '11700-000')
})
