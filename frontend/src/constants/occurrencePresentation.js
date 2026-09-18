import {
  CircleAlert, CloudRain, Construction, Droplets, Flame, LightbulbOff,
  Mountain, TrafficCone, TreePine, Trees, UtilityPole, Waves, Wind,
} from 'lucide-react'

const occurrenceTypeLabels = {
  ALAGAMENTO: 'Alagamento', ENCHENTE: 'Enchente', QUEDA_ARVORE: 'Queda de árvore',
  DESLIZAMENTO: 'Deslizamento', VENTOS_FORTES: 'Ventos fortes',
  RESSACA_MARITIMA: 'Ressaca marítima', INCENDIO: 'Incêndio',
  CHUVA_INTENSA: 'Chuva intensa', BURACO_RUA: 'Buraco na rua',
  BUEIRO_ENTUPIDO: 'Bueiro entupido', POSTE_DANIFICADO: 'Poste danificado',
  SEMAFORO_COM_PROBLEMA: 'Semáforo com problema', RUA_BLOQUEADA: 'Rua bloqueada',
  FALTA_ILUMINACAO: 'Falta de iluminação',
  ARVORE_OBSTRUINDO_VIA: 'Árvore obstruindo a via', OUTRO: 'Outro',
}

const occurrenceCategoryLabels = {
  EVENTO_NATURAL: 'Evento natural',
  INFRAESTRUTURA_URBANA: 'Infraestrutura urbana',
}

const occurrenceCategoryPresentation = {
  EVENTO_NATURAL: {
    label: occurrenceCategoryLabels.EVENTO_NATURAL,
    icon: Trees,
    color: '#1677c8',
    softColor: '#e5f3ff',
  },
  INFRAESTRUTURA_URBANA: {
    label: occurrenceCategoryLabels.INFRAESTRUTURA_URBANA,
    icon: Construction,
    color: '#e87918',
    softColor: '#fff1e3',
  },
}

const occurrenceTypesByCategory = {
  EVENTO_NATURAL: [
    'ALAGAMENTO', 'ENCHENTE', 'QUEDA_ARVORE', 'DESLIZAMENTO', 'VENTOS_FORTES',
    'RESSACA_MARITIMA', 'INCENDIO', 'CHUVA_INTENSA', 'OUTRO',
  ],
  INFRAESTRUTURA_URBANA: [
    'BURACO_RUA', 'BUEIRO_ENTUPIDO', 'POSTE_DANIFICADO',
    'SEMAFORO_COM_PROBLEMA', 'RUA_BLOQUEADA', 'FALTA_ILUMINACAO',
    'ARVORE_OBSTRUINDO_VIA', 'OUTRO',
  ],
}

const perceivedRiskLabels = { BAIXO: 'Baixo', MEDIO: 'Médio', ALTO: 'Alto' }

const defaultOccurrenceTypePresentation = {
  icon: CircleAlert, color: '#64748b', softColor: '#f1f5f9',
}

const occurrenceTypePresentation = {
  ALAGAMENTO: { icon: Waves, color: '#1677c8', softColor: '#e5f3ff' },
  ENCHENTE: { icon: Waves, color: '#1677c8', softColor: '#e5f3ff' },
  QUEDA_ARVORE: { icon: TreePine, color: '#27935c', softColor: '#e7f7ee' },
  VENTOS_FORTES: { icon: Wind, color: '#38a9dc', softColor: '#e8f7fd' },
  DESLIZAMENTO: { icon: Mountain, color: '#8b5e3c', softColor: '#f5ece5' },
  INCENDIO: { icon: Flame, color: '#d83b35', softColor: '#fdeceb' },
  CHUVA_INTENSA: { icon: CloudRain, color: '#174f86', softColor: '#e7eff8' },
  RESSACA_MARITIMA: { icon: Waves, color: '#146b9b', softColor: '#e5f3f8' },
  BURACO_RUA: { icon: Construction, color: '#e87918', softColor: '#fff1e3' },
  BUEIRO_ENTUPIDO: { icon: Droplets, color: '#c69212', softColor: '#fff8da' },
  POSTE_DANIFICADO: { icon: UtilityPole, color: '#7b4ab3', softColor: '#f2eafa' },
  FALTA_ILUMINACAO: { icon: LightbulbOff, color: '#657080', softColor: '#edf0f3' },
  ARVORE_OBSTRUINDO_VIA: { icon: TreePine, color: '#176b43', softColor: '#e4f2e9' },
  SEMAFORO_COM_PROBLEMA: { icon: TrafficCone, color: '#cf3535', softColor: '#fce9e9' },
  RUA_BLOQUEADA: { icon: Construction, color: '#c46620', softColor: '#faeee5' },
}

function getOccurrenceTypePresentation(type) {
  return occurrenceTypePresentation[type] ?? defaultOccurrenceTypePresentation
}

function getOccurrenceTypeLabel(type) { return occurrenceTypeLabels[type] ?? type }
function getOccurrenceCategoryLabel(category) { return occurrenceCategoryLabels[category] ?? category }

export {
  getOccurrenceCategoryLabel, getOccurrenceTypeLabel, getOccurrenceTypePresentation,
  occurrenceCategoryLabels, occurrenceCategoryPresentation,
  occurrenceTypeLabels, occurrenceTypePresentation,
  occurrenceTypesByCategory, perceivedRiskLabels,
}
