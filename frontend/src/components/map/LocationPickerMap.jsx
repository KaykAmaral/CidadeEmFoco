import { divIcon } from 'leaflet'
import { useEffect } from 'react'
import {
  MapContainer,
  Marker,
  TileLayer,
  useMap,
  useMapEvents,
} from 'react-leaflet'
import './Map.css'

const PRAIA_GRANDE_CENTER = [-24.005833, -46.405833]

const selectedLocationIcon = divIcon({
  className: 'occurrence-marker-container',
  html: '<span class="occurrence-marker occurrence-marker--selected" style="--marker-color:#1677c8"><span class="selected-location-dot"></span></span>',
  iconAnchor: [20, 20],
  iconSize: [40, 40],
})

function MapClickHandler({ onChange }) {
  useMapEvents({
    click(event) {
      onChange({
        latitude: event.latlng.lat.toFixed(6),
        longitude: event.latlng.lng.toFixed(6),
      })
    },
  })

  return null
}

function MapFocus({ position }) {
  const map = useMap()

  useEffect(() => {
    if (position) {
      map.setView(position, Math.max(map.getZoom(), 15))
    }
  }, [map, position])

  return null
}

function LocationPickerMap({ latitude, longitude, onChange }) {
  const parsedLatitude = Number(latitude)
  const parsedLongitude = Number(longitude)
  const hasPosition =
    Number.isFinite(parsedLatitude) &&
    Number.isFinite(parsedLongitude) &&
    latitude !== '' &&
    longitude !== ''
  const position = hasPosition ? [parsedLatitude, parsedLongitude] : null

  return (
    <div className="location-picker-map">
      <MapContainer center={PRAIA_GRANDE_CENTER} scrollWheelZoom zoom={13}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapClickHandler onChange={onChange} />
        <MapFocus position={position} />
        {position && <Marker draggable eventHandlers={{ dragend(event) { const point = event.target.getLatLng(); onChange({ latitude: point.lat.toFixed(6), longitude: point.lng.toFixed(6) }) } }} icon={selectedLocationIcon} position={position} />}
      </MapContainer>
    </div>
  )
}

export default LocationPickerMap
