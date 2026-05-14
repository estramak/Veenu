import { useEffect, useState } from 'react';
import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';
import { getListings } from '../services/api';
import 'leaflet/dist/leaflet.css';

const TYPE_COLORS = {
  BUSINESS: '#c0674a',
  EVENT: '#e8c547',
  GROUP: '#7a9e7e',
  NOTE: '#6e4f6e',
};

const CENTER = [46.2804, -119.2752]; // Richland, WA

export default function Map() {
  const [listings, setListings] = useState([]);

  useEffect(() => {
    getListings(CENTER[0], CENTER[1], 20)
      .then(res => setListings(res.data))
      .catch(err => console.error('Failed to load listings:', err));
  }, []);

  return (
    <MapContainer
      center={CENTER}
      zoom={13}
      style={{ height: '100vh', width: '100%' }}
    >
      <TileLayer
        url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
        attribution='&copy; <a href="https://carto.com/">CARTO</a>'
      />
      {listings.map(listing => (
        <CircleMarker
          key={listing.id}
          center={[listing.latitude, listing.longitude]}
          radius={8}
          fillColor={TYPE_COLORS[listing.type]}
          color={TYPE_COLORS[listing.type]}
          fillOpacity={0.9}
          weight={2}
        >
          <Popup>
            <strong>{listing.name}</strong>
            <br />
            {listing.type}
            <br />
            {listing.address}
          </Popup>
        </CircleMarker>
      ))}
    </MapContainer>
  );
}