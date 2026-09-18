import { useEffect, useMemo, useState } from 'react'
import { getOccurrenceImage } from '../../services/occurrenceService'
import FeedbackState from '../ui/FeedbackState'

function OccurrenceImageGallery({ occurrence, token, onUnauthorized }) {
  const [urls, setUrls] = useState([])
  const [error, setError] = useState('')
  const imagePaths = useMemo(() => occurrence.imageUrls?.length ? occurrence.imageUrls : occurrence.imageUrl ? [occurrence.imageUrl] : [], [occurrence.imageUrl, occurrence.imageUrls])

  useEffect(() => {
    let active = true
    let objectUrls = []
    Promise.all(imagePaths.map((path) => getOccurrenceImage(token, occurrence.id, path)))
      .then((blobs) => { objectUrls = blobs.map(URL.createObjectURL); if (active) setUrls(objectUrls) })
      .catch((requestError) => { if (requestError.status === 401) onUnauthorized(); else if (active) setError('Não foi possível carregar as imagens.') })
    return () => { active = false; objectUrls.forEach(URL.revokeObjectURL) }
  }, [imagePaths, occurrence.id, onUnauthorized, token])

  if (error) return <p className="field-error">{error}</p>
  if (!urls.length) return <FeedbackState type="loading" message="Carregando imagens..." />
  return <div className="occurrence-image-gallery">{urls.map((url, index) => <img alt={`Foto ${index + 1} da ocorrência`} key={url} src={url} />)}</div>
}

export default OccurrenceImageGallery
