import assert from 'node:assert/strict'
import test from 'node:test'
import { getOccurrenceGrouping } from './occurrenceGrouping.js'

test('identifies an individual occurrence from API strength', () => {
  assert.deepEqual(getOccurrenceGrouping({ id: 1, caseId: 1, strength: 1 }), {
    grouped: false, label: 'Relato individual', strength: 1,
  })
})

test('identifies a grouped occurrence and its report quantity', () => {
  assert.deepEqual(getOccurrenceGrouping({ id: 1, caseId: 1, strength: 4 }), {
    grouped: true, label: '4 relatos agrupados', strength: 4,
  })
})
