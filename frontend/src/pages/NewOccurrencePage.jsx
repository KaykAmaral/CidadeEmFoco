import { ArrowLeft, Check, LocateFixed, LoaderCircle, MapPin } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import AddressSearch from '../components/map/AddressSearch'
import LocationPickerMap from '../components/map/LocationPickerMap'
import ImagePicker from '../components/occurrences/ImagePicker'
import OccurrenceClassificationSelector from '../components/occurrences/OccurrenceClassificationSelector'
import RiskSelector from '../components/occurrences/RiskSelector'
import SimilarOccurrenceNotice from '../components/occurrences/SimilarOccurrenceNotice'
import Button from '../components/ui/Button'
import { getOccurrenceCategoryLabel, getOccurrenceTypeLabel, perceivedRiskLabels } from '../constants/occurrencePresentation'
import useAuth from '../hooks/useAuth'
import { reverseGeocode } from '../services/geocodingService'
import { createOccurrence, getOccurrences, uploadOccurrenceImages } from '../services/occurrenceService'
import { occurrenceStepForErrors, validateOccurrenceStepOne, validateOccurrenceStepTwo } from '../utils/occurrenceForm'
import { cooldownSecondsFromError, formatCooldown, isOutsideCitizenCity } from '../utils/occurrenceSubmission'
import './OccurrenceForm.css'

const initialForm = { category: '', type: '', description: '', perceivedRisk: '', latitude: '', longitude: '', neighborhood: '', address: '' }

function distanceInMeters(first, second) {
  const radians = (value) => value * Math.PI / 180
  const latitudeDistance = radians(Number(second.latitude) - Number(first.latitude))
  const longitudeDistance = radians(Number(second.longitude) - Number(first.longitude))
  const value = Math.sin(latitudeDistance / 2) ** 2 + Math.cos(radians(Number(first.latitude))) * Math.cos(radians(Number(second.latitude))) * Math.sin(longitudeDistance / 2) ** 2
  return 6371000 * 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value))
}

function NewOccurrencePage() {
  const navigate = useNavigate()
  const { logout, token, user } = useAuth()
  const [step, setStep] = useState(1)
  const [form, setForm] = useState(initialForm)
  const [images, setImages] = useState([])
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [locationError, setLocationError] = useState('')
  const [selectedCity, setSelectedCity] = useState('')
  const [isLocating, setIsLocating] = useState(false)
  const [isResolvingLocation, setIsResolvingLocation] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [similarOccurrence, setSimilarOccurrence] = useState(null)
  const [cooldownSeconds, setCooldownSeconds] = useState(0)
  const submittingRef = useRef(false)
  const isCooldownActive = cooldownSeconds > 0
  const { type: occurrenceType, latitude: occurrenceLatitude, longitude: occurrenceLongitude } = form

  useEffect(() => {
    if (!occurrenceType || !occurrenceLatitude || !occurrenceLongitude) return undefined
    let current = true
    const timer = window.setTimeout(async () => {
      try {
        const occurrences = await getOccurrences(token, { type: occurrenceType })
        const similar = occurrences.find((occurrence) => occurrence.status !== 'RESOLVIDA' && distanceInMeters({ latitude: occurrenceLatitude, longitude: occurrenceLongitude }, occurrence) <= 300)
        if (current) setSimilarOccurrence(similar ?? null)
      } catch { if (current) setSimilarOccurrence(null) }
    }, 600)
    return () => { current = false; window.clearTimeout(timer) }
  }, [occurrenceLatitude, occurrenceLongitude, occurrenceType, token])

  useEffect(() => {
    if (!isCooldownActive) return undefined
    const timer = window.setInterval(() => setCooldownSeconds((remaining) => Math.max(0, remaining - 1)), 1000)
    return () => window.clearInterval(timer)
  }, [isCooldownActive])

  function handleChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value, ...(name === 'category' ? { type: '' } : {}) }))
    setError('')
    setFieldErrors((current) => ({ ...current, [name]: undefined }))
    if (name === 'type' || name === 'category') setSimilarOccurrence(null)
  }

  function applyLocation(location) {
    setForm((current) => ({ ...current, latitude: location.latitude, longitude: location.longitude, address: location.address ?? current.address, neighborhood: location.neighborhood ?? current.neighborhood }))
    if (location.city !== undefined) setSelectedCity(location.city)
    setLocationError('')
    setFieldErrors((current) => ({ ...current, latitude: undefined, longitude: undefined, location: undefined }))
  }

  async function updatePoint(coordinates) {
    applyLocation(coordinates)
    setSelectedCity('')
    setIsResolvingLocation(true)
    try { applyLocation(await reverseGeocode(coordinates.latitude, coordinates.longitude)) }
    catch { setLocationError('O ponto foi atualizado, mas não foi possível identificar o endereço.') }
    finally { setIsResolvingLocation(false) }
  }

  function useCurrentLocation() {
    if (!navigator.geolocation) return setLocationError('Seu navegador não oferece acesso à localização.')
    setIsLocating(true); setLocationError('')
    navigator.geolocation.getCurrentPosition(async (position) => {
      await updatePoint({ latitude: position.coords.latitude.toFixed(6), longitude: position.coords.longitude.toFixed(6) })
      setIsLocating(false)
    }, () => { setLocationError('Não foi possível obter sua localização. Marque o ponto no mapa.'); setIsLocating(false) }, { enableHighAccuracy: true, timeout: 10000 })
  }

  function goToStep(nextStep) {
    setError('')
    setStep(nextStep)
    const scrollContainer = document.querySelector('.citizen-main')
    if (scrollContainer) scrollContainer.scrollTo({ top: 0, behavior: 'smooth' })
    else window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function advanceFromClassification() {
    const errors = validateOccurrenceStepOne(form)
    setFieldErrors(errors)
    if (!Object.keys(errors).length) goToStep(2)
  }

  function advanceFromLocation() {
    const errors = validateOccurrenceStepTwo(form, selectedCity, user?.city?.name)
    setFieldErrors(errors)
    if (errors.location) setLocationError(errors.location)
    if (isResolvingLocation) setLocationError('Aguarde enquanto confirmamos o local selecionado.')
    if (!Object.keys(errors).length && !isResolvingLocation) goToStep(3)
  }

  async function confirmOccurrence() {
    if (submittingRef.current || isCooldownActive) return
    submittingRef.current = true
    setError(''); setLocationError(''); setFieldErrors({}); setIsSubmitting(true)
    try {
      const occurrence = await createOccurrence(token, { ...form, latitude: Number(form.latitude), longitude: Number(form.longitude) })
      let photoWarning = ''
      if (images.length) {
        try { await uploadOccurrenceImages(token, occurrence.id, images) }
        catch (requestError) {
          if (requestError.status === 401) { logout(); return }
          photoWarning = `A ocorrência foi criada, mas as imagens não foram enviadas: ${requestError.message}`
        }
      }
      navigate(`/app/ocorrencias/${occurrence.id}`, { replace: true, state: { created: true, photoWarning } })
    } catch (requestError) {
      if (requestError.status === 401) { logout(); return }
      const apiCooldownSeconds = cooldownSecondsFromError(requestError)
      if (apiCooldownSeconds > 0) setCooldownSeconds(apiCooldownSeconds)
      if (isOutsideCitizenCity(requestError)) {
        setLocationError(`${requestError.message}. Você só pode registrar ocorrências na cidade vinculada ao seu cadastro.`)
        setStep(2)
      } else {
        setError(requestError.message)
        setStep(occurrenceStepForErrors(requestError.fieldErrors ?? {}))
      }
      setFieldErrors(requestError.fieldErrors ?? {})
    } finally {
      submittingRef.current = false
      setIsSubmitting(false)
    }
  }

  function submitOccurrence(event) {
    event.preventDefault()
    if (!isSubmitting && !isCooldownActive) confirmOccurrence()
  }

  return (
    <section className="occurrence-form-page">
      <Link className="back-link" to="/app/ocorrencias"><ArrowLeft size={17} aria-hidden="true" />Voltar para ocorrências</Link>
      <header className="occurrence-form-page__heading"><span>Participação cidadã</span><h1>Registrar ocorrência</h1><p>Informe o que aconteceu e marque o local com a maior precisão possível.</p></header>
      {error && <div className="form-message form-message--error" role="alert">{error}</div>}
      <div aria-label={`Etapa ${step} de 3`} className="occurrence-form__progress">
        <div><strong>Etapa {step} de 3</strong><span>{step === 1 ? 'O que aconteceu?' : step === 2 ? 'Onde aconteceu?' : 'Imagens e revisão'}</span></div>
        <div aria-hidden="true" className="occurrence-form__progress-track"><span style={{ width: `${step / 3 * 100}%` }} /></div>
      </div>
      <form className="occurrence-form" onSubmit={submitOccurrence}>
        {step === 1 && <div className="occurrence-form__section occurrence-form__step">
          <div className="occurrence-form__section-heading"><span>1</span><div><strong>O que aconteceu?</strong><p>Escolha a categoria, o tipo e o risco percebido.</p></div></div>
          <div className="form-grid">
            <div className="form-field form-field--wide"><OccurrenceClassificationSelector category={form.category} fieldErrors={fieldErrors} onChange={handleChange} type={form.type} /></div>
            <div className="form-field form-field--wide"><RiskSelector error={fieldErrors.perceivedRisk} onChange={handleChange} value={form.perceivedRisk} /></div>
            <div className="form-field form-field--wide"><label htmlFor="occurrence-description">Descrição</label><textarea aria-invalid={Boolean(fieldErrors.description)} className="form-control" id="occurrence-description" maxLength={2000} name="description" onChange={handleChange} placeholder="Descreva o que está acontecendo..." value={form.description} /><small>{form.description.length}/2000 caracteres</small>{fieldErrors.description && <small className="field-error">{fieldErrors.description}</small>}</div>
          </div>
          <div className="occurrence-form__actions"><Button onClick={advanceFromClassification}>Próximo</Button></div>
        </div>}

        {step === 2 && <div className="occurrence-form__section occurrence-form__step">
          <div className="occurrence-form__section-heading"><span>2</span><div><strong>Onde aconteceu?</strong><p>Pesquise, clique no mapa, arraste o marcador ou use sua localização. Você só pode registrar ocorrências na cidade vinculada ao seu cadastro.</p></div></div>
          <AddressSearch error={fieldErrors.address} onChange={(event) => { setForm((current) => ({ ...current, address: event.target.value })); setFieldErrors((current) => ({ ...current, address: undefined })) }} onSelect={applyLocation} value={form.address} />
          <Button disabled={isLocating} onClick={useCurrentLocation} variant="outline">{isLocating ? <LoaderCircle className="button__spinner" size={17} /> : <LocateFixed size={17} />}{isLocating ? 'Localizando...' : 'Usar minha localização'}</Button>
          {locationError && <p className="location-error" role="alert">{locationError}</p>}
          <LocationPickerMap latitude={form.latitude} longitude={form.longitude} onChange={updatePoint} />
          <SimilarOccurrenceNotice occurrence={similarOccurrence} />
          <div className="form-grid">
            <div className={`selected-location form-field--wide ${form.latitude && form.longitude ? 'is-selected' : ''}`}><MapPin size={20} aria-hidden="true" /><div><strong>{isResolvingLocation ? 'Confirmando local...' : form.latitude && form.longitude ? 'Local selecionado' : 'Nenhum local selecionado'}</strong><span>{form.address || form.neighborhood || (form.latitude && form.longitude ? 'Ponto marcado no mapa' : 'Pesquise um endereço ou marque um ponto no mapa.')}</span></div>{form.latitude && form.longitude && !isResolvingLocation && <Check size={18} aria-hidden="true" />}</div>
            {(fieldErrors.latitude || fieldErrors.longitude) && <small className="field-error form-field--wide">{fieldErrors.latitude || fieldErrors.longitude}</small>}
            <div className="form-field form-field--wide"><label htmlFor="occurrence-neighborhood">Bairro <small>(opcional)</small></label><input aria-invalid={Boolean(fieldErrors.neighborhood)} className="form-control" id="occurrence-neighborhood" maxLength={100} name="neighborhood" onChange={handleChange} value={form.neighborhood} />{fieldErrors.neighborhood && <small className="field-error">{fieldErrors.neighborhood}</small>}</div>
          </div>
          <div className="occurrence-form__actions"><Button onClick={() => goToStep(1)} variant="outline">Voltar</Button><Button disabled={isResolvingLocation} onClick={advanceFromLocation}>Próximo</Button></div>
        </div>}

        {step === 3 && <div className="occurrence-form__section occurrence-form__step">
          <div className="occurrence-form__section-heading"><span>3</span><div><strong>Imagens e revisão</strong><p>As imagens são opcionais. Adicione até quatro e revise os dados antes de confirmar.</p></div></div>
          <ImagePicker images={images} onChange={setImages} />
          <div className="occurrence-review" aria-label="Resumo da ocorrência"><h2>Revise antes de enviar</h2><dl><div><dt>Categoria</dt><dd>{getOccurrenceCategoryLabel(form.category)}</dd></div><div><dt>Tipo</dt><dd>{getOccurrenceTypeLabel(form.type)}</dd></div><div><dt>Risco percebido</dt><dd>{perceivedRiskLabels[form.perceivedRisk]}</dd></div><div><dt>Local</dt><dd>{form.address || form.neighborhood || 'Ponto selecionado no mapa'}</dd></div><div className="occurrence-review__wide"><dt>Descrição</dt><dd>{form.description}</dd></div></dl></div>
          <div className="occurrence-form__actions"><Button disabled={isSubmitting} onClick={() => goToStep(2)} variant="outline">Voltar</Button><Button disabled={isSubmitting || isCooldownActive} type="submit">{isSubmitting ? <><LoaderCircle className="button__spinner" size={17} />Enviando...</> : isCooldownActive ? `Aguarde ${formatCooldown(cooldownSeconds)}` : 'Confirmar registro'}</Button></div>
        </div>}
      </form>
    </section>
  )
}

export default NewOccurrencePage
