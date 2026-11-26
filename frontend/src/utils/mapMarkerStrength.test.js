import assert from 'node:assert/strict'
import test from 'node:test'
import { getMapMarkerStrength } from './mapMarkerStrength.js'

test('uses blue strength for markers with one to five reports', () => {
  assert.deepEqual(getMapMarkerStrength(1), { strength: 1, level: 'low' })
  assert.deepEqual(getMapMarkerStrength(5), { strength: 5, level: 'low' })
})

test('uses yellow strength for markers with six to ten reports', () => {
  assert.deepEqual(getMapMarkerStrength(6), { strength: 6, level: 'medium' })
  assert.deepEqual(getMapMarkerStrength(10), { strength: 10, level: 'medium' })
})

test('uses red strength for markers with more than ten reports', () => {
  assert.deepEqual(getMapMarkerStrength(11), { strength: 11, level: 'high' })
})

test('falls back to a single report when strength is missing or invalid', () => {
  assert.deepEqual(getMapMarkerStrength(undefined), { strength: 1, level: 'low' })
})
