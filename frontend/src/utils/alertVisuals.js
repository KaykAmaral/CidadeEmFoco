function getDayPeriod(date = new Date()) {
  const hour = date.getHours()
  if (hour >= 6 && hour < 12) return 'day'
  if (hour >= 12 && hour < 18) return 'afternoon'
  return 'night'
}

function getAlertEffect(type) {
  if (['CHUVA_INTENSA', 'ALAGAMENTO', 'RESSACA_MARITIMA'].includes(type)) return 'rain'
  if (type === 'VENTOS_FORTES') return 'wind'
  if (type === 'DESLIZAMENTO') return 'terrain'
  return 'attention'
}

export { getAlertEffect, getDayPeriod }
