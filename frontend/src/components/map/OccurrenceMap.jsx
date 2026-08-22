import { divIcon } from 'leaflet'
import { MapContainer, Marker, Popup, TileLayer } from 'react-leaflet'
import {
  getOccurrenceCategoryLabel,
  getOccurrenceTypeLabel,
} from '../../constants/occurrencePresentation'
import StatusBadge from '../ui/StatusBadge'

const PRAIA_GRANDE_CENTER = [-24.005833, -46.405833]

const markerIcons = {
  EVENTO_NATURAL: divIcon({
    className: 'occurrence-marker-container',
    html: '<span class="occurrence-marker occurrence-marker--natural"><span></span></span>',
    iconAnchor: [17, 34],
    iconSize: [34, 34],
    popupAnchor: [0, -30],
  }),
  INFRAESTRUTURA_URBANA: divIcon({
    className: 'occurrence-marker-container',
    html: '<span class="occurrence-marker occurrence-marker--infrastructure"><span></span></span>',
    iconAnchor: [17, 34],
    iconSize: [34, 34],
    popupAnchor: [0, -30],
  }),
}

function OccurrenceMap({ occurrences }) {
  const mappedOccurrences = occurrences.filter((occurrence) => {
    const latitude = Number(occurrence.latitude)
    const longitude = Number(occurrence.longitude)
    return Number.isFinite(latitude) && Number.isFinite(longitude)
  })

  return (
    <div className="occurrence-map" aria-label="Mapa de ocorrências de Praia Grande">
      <MapContainer
        center={PRAIA_GRANDE_CENTER}
        scrollWheelZoom
        zoom={13}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {mappedOccurrences.map((occurrence) => (
          <Marker
            icon={markerIcons[occurrence.category]}
            key={occurrence.id}
            position={[Number(occurrence.latitude), Number(occurrence.longitude)]}
          >
            <Popup>
              <div className="map-popup">
                <span>{getOccurrenceCategoryLabel(occurrence.category)}</span>
                <strong>{getOccurrenceTypeLabel(occurrence.type)}</strong>
                <p>
                  {occurrence.address ||
                    occurrence.neighborhood ||
                    'Localização informada pelo cidadão'}
                </p>
                <StatusBadge status={occurrence.status} />
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  )
}

export default OccurrenceMap
