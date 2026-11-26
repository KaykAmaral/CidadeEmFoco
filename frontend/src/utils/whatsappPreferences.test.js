import assert from 'node:assert/strict'
import test from 'node:test'
import { preferencesToForm, whatsappPreferencePayload } from './whatsappPreferences.js'

test('fills the edit form from the consulted preferences', () => {
  assert.deepEqual(preferencesToForm({
    phoneNumber: '+5513999999999',
    notificationsEnabled: true,
  }), {
    phoneNumber: '+5513999999999',
    consentGiven: true,
  })
})

test('restores consulted values when editing is cancelled', () => {
  const preferences = { phoneNumber: '+5513888888888', notificationsEnabled: true }
  const editedForm = { phoneNumber: '+5513777777777', consentGiven: false }

  assert.notDeepEqual(editedForm, preferencesToForm(preferences))
  assert.deepEqual(preferencesToForm(preferences), {
    phoneNumber: '+5513888888888',
    consentGiven: true,
  })
})

test('normalizes the phone before updating preferences', () => {
  assert.deepEqual(whatsappPreferencePayload({
    phoneNumber: '  +5513999999999  ',
    consentGiven: true,
  }), {
    phoneNumber: '+5513999999999',
    consentGiven: true,
  })
})
