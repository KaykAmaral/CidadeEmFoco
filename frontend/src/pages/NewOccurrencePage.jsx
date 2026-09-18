import { ArrowLeft, LocateFixed, LoaderCircle } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import AddressSearch from '../components/map/AddressSearch'
import LocationPickerMap from '../components/map/LocationPickerMap'
import ImagePicker from '../components/occurrences/ImagePicker'
import OccurrenceClassificationSelector from '../components/occurrences/OccurrenceClassificationSelector'
import OccurrenceReviewModal from '../components/occurrences/OccurrenceReviewModal'
import RiskSelector from '../components/occurrences/RiskSelector'
import SimilarOccurrenceNotice from '../components/occurrences/SimilarOccurrenceNotice'
import Button from '../components/ui/Button'
import useAuth from '../hooks/useAuth'
import { reverseGeocode } from '../services/geocodingService'
import { createOccurrence, getOccurrences, uploadOccurrenceImages } from '../services/occurrenceService'
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
  const { logout, token } = useAuth()
  const [form, setForm] = useState(initialForm)
  const [images, setImages] = useState([])
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [locationError, setLocationError] = useState('')
  const [isLocating, setIsLocating] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [showReview, setShowReview] = useState(false)
  const [similarOccurrence, setSimilarOccurrence] = useState(null)
  const occurrenceType = form.type
  const occurrenceLatitude = form.latitude
  const occurrenceLongitude = form.longitude

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

  function handleChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value, ...(name === 'category' ? { type: '' } : {}) }))
    setError(''); setFieldErrors({})
    if (name === 'type' || name === 'category') setSimilarOccurrence(null)
  }

  function applyLocation(location) {
    setForm((current) => ({ ...current, latitude: location.latitude, longitude: location.longitude, address: location.address ?? current.address, neighborhood: location.neighborhood ?? current.neighborhood }))
    setLocationError(''); setFieldErrors({})
  }

  async function updatePoint(coordinates) {
    applyLocation(coordinates)
    try { applyLocation(await reverseGeocode(coordinates.latitude, coordinates.longitude)) }
    catch { setLocationError('O ponto foi atualizado, mas não foi possível identificar o endereço.') }
  }

  function useCurrentLocation() {
    if (!navigator.geolocation) return setLocationError('Seu navegador não oferece acesso à localização.')
    setIsLocating(true); setLocationError('')
    navigator.geolocation.getCurrentPosition(async (position) => {
      await updatePoint({ latitude: position.coords.latitude.toFixed(6), longitude: position.coords.longitude.toFixed(6) })
      setIsLocating(false)
    }, () => { setLocationError('Não foi possível obter sua localização. Marque o ponto no mapa.'); setIsLocating(false) }, { enableHighAccuracy: true, timeout: 10000 })
  }

  function requestReview(event) { event.preventDefault(); setShowReview(true) }

  async function confirmOccurrence() {
    setError(''); setFieldErrors({}); setIsSubmitting(true)
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
      setError(requestError.message); setFieldErrors(requestError.fieldErrors ?? {}); setShowReview(false)
    } finally { setIsSubmitting(false) }
  }

  return (
    <section className="occurrence-form-page">
      <Link className="back-link" to="/app/ocorrencias"><ArrowLeft size={17} aria-hidden="true" />Voltar para ocorrências</Link>
      <header className="occurrence-form-page__heading"><span>Participação cidadã</span><h1>Registrar ocorrência</h1><p>Informe o que aconteceu e marque o local com a maior precisão possível.</p></header>
      {error && <div className="form-message form-message--error" role="alert">{error}</div>}
      <form className="occurrence-form" onSubmit={requestReview}>
        <div className="occurrence-form__section">
          <div className="occurrence-form__section-heading"><span>1</span><div><strong>Classificação</strong><p>Escolha a categoria, o tipo e o risco percebido.</p></div></div>
          <div className="form-grid">
            <div className="form-field form-field--wide"><OccurrenceClassificationSelector category={form.category} fieldErrors={fieldErrors} onChange={handleChange} type={form.type} /></div>
            <div className="form-field form-field--wide"><RiskSelector error={fieldErrors.perceivedRisk} onChange={handleChange} value={form.perceivedRisk} /></div>
            <div className="form-field form-field--wide"><label htmlFor="occurrence-description">Descrição</label><textarea className="form-control" id="occurrence-description" maxLength={2000} name="description" onChange={handleChange} placeholder="Descreva o que está acontecendo..." required value={form.description} /><small>{form.description.length}/2000 caracteres</small>{fieldErrors.description && <small className="field-error">{fieldErrors.description}</small>}</div>
          </div>
        </div>

        <div className="occurrence-form__section">
          <div className="occurrence-form__section-heading"><span>2</span><div><strong>Localização</strong><p>Pesquise, clique no mapa, arraste o marcador ou use sua localização.</p></div></div>
          <AddressSearch onChange={(event) => setForm((current) => ({ ...current, address: event.target.value }))} onSelect={applyLocation} value={form.address} />
          <Button onClick={useCurrentLocation} variant="outline">{isLocating ? <LoaderCircle className="button__spinner" size={17} /> : <LocateFixed size={17} />}{isLocating ? 'Localizando...' : 'Usar minha localização'}</Button>
          {locationError && <p className="location-error" role="alert">{locationError}</p>}
          <LocationPickerMap latitude={form.latitude} longitude={form.longitude} onChange={updatePoint} />
          <SimilarOccurrenceNotice occurrence={similarOccurrence} />
          <div className="form-grid">
            <div className="form-field"><label htmlFor="occurrence-latitude">Latitude</label><input className="form-control" id="occurrence-latitude" max="90" min="-90" name="latitude" onChange={handleChange} required step="any" type="number" value={form.latitude} />{fieldErrors.latitude && <small className="field-error">{fieldErrors.latitude}</small>}</div>
            <div className="form-field"><label htmlFor="occurrence-longitude">Longitude</label><input className="form-control" id="occurrence-longitude" max="180" min="-180" name="longitude" onChange={handleChange} required step="any" type="number" value={form.longitude} />{fieldErrors.longitude && <small className="field-error">{fieldErrors.longitude}</small>}</div>
            <div className="form-field form-field--wide"><label htmlFor="occurrence-neighborhood">Bairro <small>(opcional)</small></label><input className="form-control" id="occurrence-neighborhood" maxLength={100} name="neighborhood" onChange={handleChange} value={form.neighborhood} /></div>
          </div>
        </div>

        <div className="occurrence-form__section"><div className="occurrence-form__section-heading"><span>3</span><div><strong>Imagens opcionais</strong><p>Adicione até quatro imagens que ajudem a compreender a ocorrência.</p></div></div><ImagePicker images={images} onChange={setImages} /></div>
        <Button className="occurrence-form__submit" type="submit">Revisar ocorrência</Button>
      </form>
      {showReview && <OccurrenceReviewModal form={form} imageCount={images.length} onBack={() => setShowReview(false)} onConfirm={confirmOccurrence} submitting={isSubmitting} />}
    </section>
  )
}

export default NewOccurrencePage
