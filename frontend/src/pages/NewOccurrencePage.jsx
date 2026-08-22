import { ArrowLeft, LocateFixed, LoaderCircle, Upload } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import LocationPickerMap from '../components/map/LocationPickerMap'
import Button from '../components/ui/Button'
import {
  occurrenceCategoryLabels,
  occurrenceTypeLabels,
  occurrenceTypesByCategory,
  perceivedRiskLabels,
} from '../constants/occurrencePresentation'
import useAuth from '../hooks/useAuth'
import {
  createOccurrence,
  uploadOccurrenceImage,
} from '../services/occurrenceService'
import './OccurrenceForm.css'

const MAX_IMAGE_SIZE = 5 * 1024 * 1024
const ACCEPTED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp']

const initialForm = {
  category: '',
  type: '',
  description: '',
  perceivedRisk: '',
  latitude: '',
  longitude: '',
  neighborhood: '',
  address: '',
}

function NewOccurrencePage() {
  const navigate = useNavigate()
  const { logout, token } = useAuth()
  const [form, setForm] = useState(initialForm)
  const [image, setImage] = useState(null)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [imageError, setImageError] = useState('')
  const [locationError, setLocationError] = useState('')
  const [isLocating, setIsLocating] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const availableTypes = form.category
    ? occurrenceTypesByCategory[form.category]
    : []

  function handleChange(event) {
    const { name, value } = event.target
    setForm((currentForm) => ({
      ...currentForm,
      [name]: value,
      ...(name === 'category' ? { type: '' } : {}),
    }))
    setError('')
    setFieldErrors({})
  }

  function handleCoordinates(coordinates) {
    setForm((currentForm) => ({ ...currentForm, ...coordinates }))
    setLocationError('')
    setFieldErrors({})
  }

  function useCurrentLocation() {
    if (!navigator.geolocation) {
      setLocationError('Seu navegador não oferece acesso à localização.')
      return
    }

    setIsLocating(true)
    setLocationError('')
    navigator.geolocation.getCurrentPosition(
      (position) => {
        handleCoordinates({
          latitude: position.coords.latitude.toFixed(6),
          longitude: position.coords.longitude.toFixed(6),
        })
        setIsLocating(false)
      },
      () => {
        setLocationError(
          'Não foi possível obter sua localização. Marque o ponto no mapa.',
        )
        setIsLocating(false)
      },
      { enableHighAccuracy: true, timeout: 10000 },
    )
  }

  function handleImageChange(event) {
    const selectedImage = event.target.files?.[0] ?? null
    setImageError('')

    if (!selectedImage) {
      setImage(null)
      return
    }

    if (!ACCEPTED_IMAGE_TYPES.includes(selectedImage.type)) {
      setImageError('Use uma imagem JPEG, PNG ou WebP.')
      event.target.value = ''
      setImage(null)
      return
    }

    if (selectedImage.size > MAX_IMAGE_SIZE) {
      setImageError('A imagem deve ter no máximo 5 MB.')
      event.target.value = ''
      setImage(null)
      return
    }

    setImage(selectedImage)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setFieldErrors({})
    setIsSubmitting(true)

    try {
      const occurrence = await createOccurrence(token, {
        ...form,
        latitude: Number(form.latitude),
        longitude: Number(form.longitude),
      })

      let photoWarning = ''

      if (image) {
        try {
          await uploadOccurrenceImage(token, occurrence.id, image)
        } catch (requestError) {
          if (requestError.status === 401) {
            logout()
            return
          }
          photoWarning = `A ocorrência foi criada, mas a foto não foi enviada: ${requestError.message}`
        }
      }

      navigate(`/app/ocorrencias/${occurrence.id}`, {
        replace: true,
        state: { created: true, photoWarning },
      })
    } catch (requestError) {
      if (requestError.status === 401) {
        logout()
        return
      }

      setError(requestError.message)
      setFieldErrors(requestError.fieldErrors ?? {})
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="occurrence-form-page">
      <Link className="back-link" to="/app/ocorrencias">
        <ArrowLeft size={17} aria-hidden="true" />
        Voltar para ocorrências
      </Link>

      <header className="occurrence-form-page__heading">
        <span>Participação cidadã</span>
        <h1>Registrar ocorrência</h1>
        <p>Informe o que aconteceu e marque o local com a maior precisão possível.</p>
      </header>

      {error && <div className="form-message form-message--error" role="alert">{error}</div>}

      <form className="occurrence-form" onSubmit={handleSubmit}>
        <div className="occurrence-form__section">
          <div className="occurrence-form__section-heading">
            <span>1</span>
            <div><strong>Classificação</strong><p>Escolha a categoria, o tipo e o risco percebido.</p></div>
          </div>

          <div className="form-grid">
            <div className="form-field">
              <label htmlFor="occurrence-category">Categoria</label>
              <select className="form-control" id="occurrence-category" name="category" onChange={handleChange} required value={form.category}>
                <option value="" disabled>Selecione</option>
                {Object.entries(occurrenceCategoryLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
              {fieldErrors.category && <small className="field-error">{fieldErrors.category}</small>}
            </div>

            <div className="form-field">
              <label htmlFor="occurrence-type">Tipo</label>
              <select className="form-control" disabled={!form.category} id="occurrence-type" name="type" onChange={handleChange} required value={form.type}>
                <option value="" disabled>Selecione</option>
                {availableTypes.map((type) => <option key={type} value={type}>{occurrenceTypeLabels[type]}</option>)}
              </select>
              {fieldErrors.type && <small className="field-error">{fieldErrors.type}</small>}
            </div>

            <div className="form-field">
              <label htmlFor="occurrence-risk">Risco percebido</label>
              <select className="form-control" id="occurrence-risk" name="perceivedRisk" onChange={handleChange} required value={form.perceivedRisk}>
                <option value="" disabled>Selecione</option>
                {Object.entries(perceivedRiskLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
              <small>Esta é a sua percepção, não uma classificação técnica.</small>
              {fieldErrors.perceivedRisk && <small className="field-error">{fieldErrors.perceivedRisk}</small>}
            </div>

            <div className="form-field form-field--wide">
              <label htmlFor="occurrence-description">Descrição</label>
              <textarea className="form-control" id="occurrence-description" maxLength={2000} name="description" onChange={handleChange} placeholder="Descreva o que está acontecendo..." required value={form.description} />
              <small>{form.description.length}/2000 caracteres</small>
              {fieldErrors.description && <small className="field-error">{fieldErrors.description}</small>}
            </div>
          </div>
        </div>

        <div className="occurrence-form__section">
          <div className="occurrence-form__section-heading">
            <span>2</span>
            <div><strong>Localização</strong><p>Clique no mapa, use sua localização ou preencha as coordenadas.</p></div>
          </div>

          <Button onClick={useCurrentLocation} variant="outline">
            {isLocating ? <LoaderCircle className="button__spinner" size={17} aria-hidden="true" /> : <LocateFixed size={17} aria-hidden="true" />}
            {isLocating ? 'Localizando...' : 'Usar minha localização'}
          </Button>
          {locationError && <p className="location-error" role="alert">{locationError}</p>}

          <LocationPickerMap latitude={form.latitude} longitude={form.longitude} onChange={handleCoordinates} />

          <div className="form-grid">
            <div className="form-field">
              <label htmlFor="occurrence-latitude">Latitude</label>
              <input className="form-control" id="occurrence-latitude" max="90" min="-90" name="latitude" onChange={handleChange} required step="any" type="number" value={form.latitude} />
              {fieldErrors.latitude && <small className="field-error">{fieldErrors.latitude}</small>}
            </div>
            <div className="form-field">
              <label htmlFor="occurrence-longitude">Longitude</label>
              <input className="form-control" id="occurrence-longitude" max="180" min="-180" name="longitude" onChange={handleChange} required step="any" type="number" value={form.longitude} />
              {fieldErrors.longitude && <small className="field-error">{fieldErrors.longitude}</small>}
            </div>
            <div className="form-field">
              <label htmlFor="occurrence-neighborhood">Bairro <small>(opcional)</small></label>
              <input className="form-control" id="occurrence-neighborhood" maxLength={100} name="neighborhood" onChange={handleChange} placeholder="Ex.: Boqueirão" value={form.neighborhood} />
              {fieldErrors.neighborhood && <small className="field-error">{fieldErrors.neighborhood}</small>}
            </div>
            <div className="form-field">
              <label htmlFor="occurrence-address">Endereço <small>(opcional)</small></label>
              <input className="form-control" id="occurrence-address" maxLength={255} name="address" onChange={handleChange} placeholder="Rua e número aproximado" value={form.address} />
              {fieldErrors.address && <small className="field-error">{fieldErrors.address}</small>}
            </div>
          </div>
        </div>

        <div className="occurrence-form__section">
          <div className="occurrence-form__section-heading">
            <span>3</span>
            <div><strong>Foto opcional</strong><p>Envie uma imagem que ajude a compreender a ocorrência.</p></div>
          </div>

          <label className="image-upload" htmlFor="occurrence-image">
            <Upload size={24} aria-hidden="true" />
            <strong>{image ? image.name : 'Selecionar foto'}</strong>
            <span>JPEG, PNG ou WebP · máximo de 5 MB</span>
          </label>
          <input accept="image/jpeg,image/png,image/webp" className="visually-hidden" id="occurrence-image" onChange={handleImageChange} type="file" />
          {imageError && <p className="location-error" role="alert">{imageError}</p>}
        </div>

        <Button className="occurrence-form__submit" disabled={isSubmitting} type="submit">
          {isSubmitting && <LoaderCircle className="button__spinner" size={18} aria-hidden="true" />}
          {isSubmitting ? 'Registrando...' : 'Registrar ocorrência'}
        </Button>
      </form>
    </section>
  )
}

export default NewOccurrencePage
