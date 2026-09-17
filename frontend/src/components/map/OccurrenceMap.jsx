import { divIcon } from 'leaflet'
import { renderToStaticMarkup } from 'react-dom/server'
import { MapContainer, Marker, Popup, TileLayer } from 'react-leaflet'
import { Link, useLocation } from 'react-router'
import {
  getOccurrenceTypeLabel,
  getOccurrenceTypePresentation,
  perceivedRiskLabels,
} from '../../constants/occurrencePresentation'
import StatusBadge from '../ui/StatusBadge'
import './Map.css'

const PRAIA_GRANDE_CENTER = [-24.005833, -46.405833]

function createMarkerIcon(type) {
  const { icon: Icon, color } = getOccurrenceTypePresentation(type)
  const iconMarkup = renderToStaticMarkup(<Icon aria-hidden="true" size={19} strokeWidth={2.4} />)
  return divIcon({
    className: 'occurrence-marker-container',
    html: `<span class="occurrence-marker" style="--marker-color:${color}">${iconMarkup}</span>`,
    iconAnchor: [20, 20], iconSize: [40, 40], popupAnchor: [0, -21],
  })
}

function OccurrenceMap({ occurrences, activeCategories }) {
  const location = useLocation()
  const detailBasePath = location.pathname.startsWith('/admin')
    ? '/admin/ocorrencias' : '/app/ocorrencias'
  const selectedCategories = activeCategories ?? Object.keys(
    occurrences.reduce((categories, occurrence) => ({ ...categories, [occurrence.category]: true }), {}),
  )
  const mappedOccurrences = occurrences.filter((occurrence) =>
    selectedCategories.includes(occurrence.category) &&
    Number.isFinite(Number(occurrence.latitude)) && Number.isFinite(Number(occurrence.longitude)))

  return (
    <div className="occurrence-map" aria-label="Mapa de ocorrências de Praia Grande">
      <MapContainer center={PRAIA_GRANDE_CENTER} scrollWheelZoom zoom={13}>
        <TileLayer attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors' url="https://tile.openstreetmap.org/{z}/{x}/{y}.png" />
        {mappedOccurrences.map((occurrence) => (
          <Marker icon={createMarkerIcon(occurrence.type)} key={occurrence.id} position={[Number(occurrence.latitude), Number(occurrence.longitude)]}>
            <Popup>
              <div className="map-popup">
                <strong>{getOccurrenceTypeLabel(occurrence.type)}</strong>
                <p>{occurrence.address || occurrence.neighborhood || 'Localização informada pelo cidadão'}</p>
                <dl>
                  <div><dt>Risco</dt><dd>{perceivedRiskLabels[occurrence.perceivedRisk] ?? occurrence.perceivedRisk}</dd></div>
                  <div><dt>Status</dt><dd><StatusBadge status={occurrence.status} /></dd></div>
                </dl>
                <Link to={`${detailBasePath}/${occurrence.id}`}>Visualizar detalhes</Link>
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
      {activeCategories?.length === 0 && (
        <div className="occurrence-map__filter-empty" role="status">
          Selecione uma categoria para visualizar as ocorrências
        </div>
      )}
    </div>
  )
}

export default OccurrenceMap
