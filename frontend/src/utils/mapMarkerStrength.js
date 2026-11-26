export function getMapMarkerStrength(value) {
  const strength = Math.max(1, Number(value) || 1)

  if (strength > 10) return { strength, level: 'high' }
  if (strength > 5) return { strength, level: 'medium' }

  return { strength, level: 'low' }
}
