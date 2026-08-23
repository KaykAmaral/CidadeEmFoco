import { LoaderCircle, MapPin, Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { searchPraiaGrandeAddress } from '../../services/geocodingService'

function AddressSearch({ value, onChange, onSelect }) {
  const [suggestions, setSuggestions] = useState([])
  const [loading, setLoading] = useState(false)
  const [focused, setFocused] = useState(false)

  useEffect(() => {
    const controller = new AbortController()
    const timer = window.setTimeout(async () => {
      if (!focused || value.trim().length < 3) return setSuggestions([])
      setLoading(true)
      try { setSuggestions(await searchPraiaGrandeAddress(value, controller.signal)) }
      catch (error) { if (error.name !== 'AbortError') setSuggestions([]) }
      finally { setLoading(false) }
    }, 450)
    return () => { window.clearTimeout(timer); controller.abort() }
  }, [focused, value])

  return (
    <div className="address-search">
      <label htmlFor="occurrence-address-search">Pesquisar endereço em Praia Grande</label>
      <div className="address-search__control"><Search size={18} aria-hidden="true" /><input autoComplete="off" id="occurrence-address-search" maxLength={255} onBlur={() => window.setTimeout(() => setFocused(false), 150)} onChange={onChange} onFocus={() => setFocused(true)} placeholder="Digite rua, número ou local" value={value} />{loading && <LoaderCircle className="button__spinner" size={17} />}</div>
      {focused && suggestions.length > 0 && <ul className="address-search__suggestions">{suggestions.map((suggestion) => <li key={`${suggestion.latitude}-${suggestion.longitude}`}><button onClick={() => { onSelect(suggestion); setSuggestions([]); setFocused(false) }} type="button"><MapPin size={16} aria-hidden="true" /><span>{suggestion.displayName}</span></button></li>)}</ul>}
    </div>
  )
}

export default AddressSearch
