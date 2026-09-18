import { MapPin, X } from 'lucide-react'
import { getOccurrenceTypeLabel, perceivedRiskLabels } from '../../constants/occurrencePresentation'
import Button from '../ui/Button'
import OccurrenceTypeIcon from './OccurrenceTypeIcon'

function OccurrenceReviewModal({ form, imageCount, onBack, onConfirm, submitting }) {
  return (
    <div className="review-modal" role="presentation">
      <section aria-labelledby="review-title" aria-modal="true" className="review-modal__dialog" role="dialog">
        <button aria-label="Fechar revisão" className="review-modal__close" onClick={onBack} type="button"><X size={19} /></button>
        <header><OccurrenceTypeIcon size={24} type={form.type} /><div><span>Revise antes de enviar</span><h2 id="review-title">{getOccurrenceTypeLabel(form.type)}</h2></div></header>
        <dl>
          <div><dt>Risco percebido</dt><dd>{perceivedRiskLabels[form.perceivedRisk]}</dd></div>
          <div><dt>Localização</dt><dd><MapPin size={15} aria-hidden="true" />{form.address || `${form.latitude}, ${form.longitude}`}</dd></div>
          <div><dt>Bairro</dt><dd>{form.neighborhood || 'Não informado'}</dd></div>
          <div><dt>Imagens</dt><dd>{imageCount}</dd></div>
          <div className="review-modal__description"><dt>Descrição</dt><dd>{form.description}</dd></div>
        </dl>
        <footer><Button onClick={onBack} variant="outline">Voltar e editar</Button><Button disabled={submitting} onClick={onConfirm}>{submitting ? 'Enviando...' : 'Confirmar ocorrência'}</Button></footer>
      </section>
    </div>
  )
}

export default OccurrenceReviewModal
