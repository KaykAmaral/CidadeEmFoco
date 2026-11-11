function preferencesToForm(preferences) {
  return {
    phoneNumber: preferences?.phoneNumber ?? '',
    consentGiven: Boolean(preferences?.notificationsEnabled),
  }
}

function whatsappPreferencePayload(form) {
  return {
    phoneNumber: form.phoneNumber.trim(),
    consentGiven: form.consentGiven,
  }
}

export { preferencesToForm, whatsappPreferencePayload }
