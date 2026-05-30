<template>
  <div ref="mapEl" class="leaflet-map" :style="{ height }" />
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix para los iconos de Leaflet con Vite (los assets se resuelven diferente)
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete (L.Icon.Default.prototype as unknown as Record<string, unknown>)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
});

export interface MapMarker {
  id: string;
  lat: number;
  lng: number;
  title: string;
  category?: string;
  rating?: number;
  popup?: string;
}

const props = withDefaults(defineProps<{
  markers?: MapMarker[];
  center?: [number, number];
  zoom?: number;
  height?: string;
  interactive?: boolean;
}>(), {
  markers: () => [],
  center: () => [4.711, -74.0721], // Bogotá por defecto
  zoom: 13,
  height: '300px',
  interactive: true,
});

const emit = defineEmits<{
  markerClick: [id: string];
}>();

const mapEl = ref<HTMLElement | null>(null);
let map: L.Map | null = null;
let markerLayer: L.LayerGroup | null = null;

// Colores por categoría
const categoryColors: Record<string, string> = {
  Restaurante: '#fb923c',
  Tienda: '#60a5fa',
  Servicio: '#34d399',
  Salud: '#f87171',
  Belleza: '#f472b6',
  Educacion: '#a78bfa',
  Otro: '#94a3b8',
};

function makeIcon(category?: string): L.DivIcon {
  const color = categoryColors[category ?? ''] ?? '#0f172a';
  return L.divIcon({
    className: '',
    html: `<div style="
      width:32px;height:32px;border-radius:50% 50% 50% 0;
      background:${color};border:3px solid #fff;
      box-shadow:0 2px 8px rgba(0,0,0,0.3);
      transform:rotate(-45deg);
    "></div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -34],
  });
}

function renderMarkers(): void {
  if (!map || !markerLayer) return;
  markerLayer.clearLayers();

  for (const m of props.markers) {
    const marker = L.marker([m.lat, m.lng], { icon: makeIcon(m.category) });

    const stars = '★'.repeat(Math.round(m.rating ?? 0)) + '☆'.repeat(5 - Math.round(m.rating ?? 0));
    const popupHtml = m.popup ?? `
      <div style="min-width:160px;font-family:system-ui,sans-serif">
        <strong style="font-size:0.9rem">${m.title}</strong><br/>
        <span style="color:#f59e0b;font-size:0.85rem">${stars}</span>
        ${m.rating ? `<span style="font-size:0.8rem;color:#64748b"> ${m.rating.toFixed(1)}</span>` : ''}
        ${m.category ? `<br/><span style="font-size:0.75rem;color:#94a3b8">${m.category}</span>` : ''}
      </div>
    `;

    marker.bindPopup(popupHtml, { maxWidth: 220 });
    marker.on('click', () => emit('markerClick', m.id));
    markerLayer.addLayer(marker);
  }

  // Ajustar vista si hay marcadores
  if (props.markers.length > 0) {
    const group = L.featureGroup(markerLayer.getLayers() as L.Layer[]);
    if (props.markers.length === 1) {
      map.setView([props.markers[0].lat, props.markers[0].lng], props.zoom);
    } else {
      map.fitBounds(group.getBounds().pad(0.15));
    }
  }
}

onMounted(() => {
  if (!mapEl.value) return;

  map = L.map(mapEl.value, {
    center: props.center,
    zoom: props.zoom,
    zoomControl: props.interactive,
    dragging: props.interactive,
    scrollWheelZoom: props.interactive,
    doubleClickZoom: props.interactive,
    touchZoom: props.interactive,
  });

  // OpenStreetMap tiles — 100% gratuito, sin API key
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19,
  }).addTo(map);

  markerLayer = L.layerGroup().addTo(map);
  renderMarkers();
});

watch(() => props.markers, renderMarkers, { deep: true });

onBeforeUnmount(() => {
  map?.remove();
  map = null;
});
</script>

<style scoped>
.leaflet-map {
  width: 100%;
  border-radius: 14px;
  overflow: hidden;
  z-index: 0;
}
</style>
