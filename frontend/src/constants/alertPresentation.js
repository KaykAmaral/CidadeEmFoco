const alertTypeLabels = {
  CHUVA_INTENSA: 'Chuva intensa',
  ALAGAMENTO: 'Alagamento',
  VENTOS_FORTES: 'Ventos fortes',
  RESSACA_MARITIMA: 'Ressaca marítima',
  DESLIZAMENTO: 'Deslizamento',
  OUTRO: 'Outro alerta',
}

const alertSeverityLabels = {
  BAIXA: 'Baixa',
  MODERADA: 'Moderada',
  ALTA: 'Alta',
}

function getAlertTypeLabel(type) {
  return alertTypeLabels[type] ?? type
}

function getAlertSeverityLabel(severity) {
  return alertSeverityLabels[severity] ?? severity
}

export { getAlertSeverityLabel, getAlertTypeLabel }
