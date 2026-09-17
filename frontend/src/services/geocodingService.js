const NOMINATIM_URL = 'https://nominatim.openstreetmap.org'

function normalizeLocation(result) {
  const details = result.address ?? {}
  const road = details.road || details.pedestrian || details.residential || details.footway
  const address = [road, details.house_number].filter(Boolean).join(', ') || result.display_name?.split(',').slice(0, 2).join(',')
  return {
    latitude: Number(result.lat).toFixed(6),
    longitude: Number(result.lon).toFixed(6),
    address,
    neighborhood: details.suburb || details.neighbourhood || details.city_district || '',
    displayName: result.display_name,
  }
}

async function searchPraiaGrandeAddress(query, signal) {
  if (query.trim().length < 3) return []
  const params = new URLSearchParams({
    q: `${query}, Praia Grande, SP, Brasil`, format: 'jsonv2', addressdetails: '1',
    limit: '5', countrycodes: 'br', bounded: '1', viewbox: '-46.53,-23.93,-46.32,-24.12',
  })
  const response = await fetch(`${NOMINATIM_URL}/search?${params}`, { signal, headers: { Accept: 'application/json' } })
  if (!response.ok) throw new Error('Não foi possível pesquisar o endereço.')
  return (await response.json()).map(normalizeLocation)
}

async function reverseGeocode(latitude, longitude, signal) {
  const params = new URLSearchParams({ lat: latitude, lon: longitude, format: 'jsonv2', addressdetails: '1', zoom: '18' })
  const response = await fetch(`${NOMINATIM_URL}/reverse?${params}`, { signal, headers: { Accept: 'application/json' } })
  if (!response.ok) throw new Error('Não foi possível identificar o endereço deste ponto.')
  return normalizeLocation(await response.json())
}

export { reverseGeocode, searchPraiaGrandeAddress }
