import { Camera, Plus, Trash2 } from 'lucide-react'
import { useEffect, useMemo, useRef, useState } from 'react'

const MAX_IMAGES = 4
const MAX_SIZE = 5 * 1024 * 1024
const ACCEPTED = ['image/jpeg', 'image/png', 'image/webp']

function ImagePicker({ images, onChange }) {
  const [error, setError] = useState('')
  const inputRef = useRef(null)
  const previews = useMemo(() => images.map((file) => ({ file, url: URL.createObjectURL(file) })), [images])

  useEffect(() => () => previews.forEach(({ url }) => URL.revokeObjectURL(url)), [previews])

  function addFiles(event) {
    const files = [...(event.target.files ?? [])]
    event.target.value = ''
    setError('')
    if (images.length + files.length > MAX_IMAGES) return setError('Selecione no máximo 4 imagens.')
    const invalid = files.find((file) => !ACCEPTED.includes(file.type) || file.size > MAX_SIZE)
    if (invalid) return setError(!ACCEPTED.includes(invalid.type) ? 'Use apenas JPEG, PNG ou WebP.' : 'Cada imagem deve ter no máximo 5 MB.')
    onChange([...images, ...files])
  }

  return (
    <div className="image-picker">
      <div className="image-picker__toolbar"><strong>{images.length} de 4 imagens</strong><span>JPEG, PNG ou WebP · 5 MB por imagem</span></div>
      {images.length > 0 && <div className="image-picker__grid">{previews.map(({ file, url }, index) => (
        <figure key={`${file.name}-${file.lastModified}`}><img alt={`Prévia da imagem ${index + 1}`} src={url} /><button aria-label={`Remover imagem ${index + 1}`} onClick={() => onChange(images.filter((_, itemIndex) => itemIndex !== index))} type="button"><Trash2 size={16} /></button></figure>
      ))}</div>}
      {images.length < MAX_IMAGES && <label className="image-upload" htmlFor="occurrence-images"><Plus size={23} aria-hidden="true" /><strong>Adicionar imagens</strong><span><Camera size={15} aria-hidden="true" /> Você também pode usar a câmera do celular</span></label>}
      <input ref={inputRef} accept="image/jpeg,image/png,image/webp" capture="environment" className="visually-hidden" id="occurrence-images" multiple onChange={addFiles} type="file" />
      {error && <p className="location-error" role="alert">{error}</p>}
    </div>
  )
}

export default ImagePicker
