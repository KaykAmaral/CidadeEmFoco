const occurrenceTypeLabels = {
  ALAGAMENTO: 'Alagamento',
  ENCHENTE: 'Enchente',
  QUEDA_ARVORE: 'Queda de árvore',
  DESLIZAMENTO: 'Deslizamento',
  VENTOS_FORTES: 'Ventos fortes',
  RESSACA_MARITIMA: 'Ressaca marítima',
  BURACO_RUA: 'Buraco na rua',
  BUEIRO_ENTUPIDO: 'Bueiro entupido',
  POSTE_DANIFICADO: 'Poste danificado',
  SEMAFORO_COM_PROBLEMA: 'Semáforo com problema',
  RUA_BLOQUEADA: 'Rua bloqueada',
  FALTA_ILUMINACAO: 'Falta de iluminação',
  OUTRO: 'Outro',
}

const occurrenceCategoryLabels = {
  EVENTO_NATURAL: 'Evento natural',
  INFRAESTRUTURA_URBANA: 'Infraestrutura urbana',
}

const occurrenceTypesByCategory = {
  EVENTO_NATURAL: [
    'ALAGAMENTO',
    'ENCHENTE',
    'QUEDA_ARVORE',
    'DESLIZAMENTO',
    'VENTOS_FORTES',
    'RESSACA_MARITIMA',
    'OUTRO',
  ],
  INFRAESTRUTURA_URBANA: [
    'BURACO_RUA',
    'BUEIRO_ENTUPIDO',
    'POSTE_DANIFICADO',
    'SEMAFORO_COM_PROBLEMA',
    'RUA_BLOQUEADA',
    'FALTA_ILUMINACAO',
    'OUTRO',
  ],
}

const perceivedRiskLabels = {
  BAIXO: 'Baixo',
  MEDIO: 'Médio',
  ALTO: 'Alto',
}

function getOccurrenceTypeLabel(type) {
  return occurrenceTypeLabels[type] ?? type
}

function getOccurrenceCategoryLabel(category) {
  return occurrenceCategoryLabels[category] ?? category
}

export {
  getOccurrenceCategoryLabel,
  getOccurrenceTypeLabel,
  occurrenceCategoryLabels,
  occurrenceTypeLabels,
  occurrenceTypesByCategory,
  perceivedRiskLabels,
}
