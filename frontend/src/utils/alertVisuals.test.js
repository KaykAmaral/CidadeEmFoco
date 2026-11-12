import assert from 'node:assert/strict'
import test from 'node:test'
import { getAlertEffect, getDayPeriod } from './alertVisuals.js'

test('selects day, afternoon and night city variants', () => {
  assert.equal(getDayPeriod(new Date(2026, 0, 1, 8)), 'day')
  assert.equal(getDayPeriod(new Date(2026, 0, 1, 15)), 'afternoon')
  assert.equal(getDayPeriod(new Date(2026, 0, 1, 22)), 'night')
})

test('maps API alert types to lightweight visual effects', () => {
  assert.equal(getAlertEffect('CHUVA_INTENSA'), 'rain')
  assert.equal(getAlertEffect('VENTOS_FORTES'), 'wind')
  assert.equal(getAlertEffect('DESLIZAMENTO'), 'terrain')
  assert.equal(getAlertEffect('OUTRO'), 'attention')
})
