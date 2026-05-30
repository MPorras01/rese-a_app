<template>
  <div class="map-page">
    <!-- Search bar -->
    <div class="search-bar">
      <div class="search-input-wrap">
        <span class="search-icon">🔍</span>
        <input
          v-model="search"
          type="search"
          placeholder="Buscar negocio en el mapa..."
          class="search-input"
        />
        <button v-if="search" type="button" class="clear-btn" @click="search = ''">✕</button>
      </div>
    </div>

    <!-- Filtros de categoría -->
    <div class="filters-scroll">
      <button
        type="button"
        class="filter-chip"
        :class="{ active: !activeCategory }"
        @click="activeCategory = ''"
      >
        Todos ({{ visibleMarkers.length }})
      </button>
      <button
        v-for="cat in categoriesWithCount"
        :key="cat.name"
        type="button"
        class="filter-chip"
        :class="{ active: activeCategory === cat.name }"
        @click="activeCategory = cat.name"
      >
        {{ cat.icon }} {{ cat.name }} ({{ cat.count }})
      </button>
    </div>

    <!-- Mapa principal -->
    <div class="map-container">
      <div v-if="loading" class="map-loading">
        <div class="spinner" />
        <p>Cargando negocios...</p>
      </div>

      <LeafletMap
        v-else
        :markers="visibleMarkers"
        :height="mapHeight"
        :zoom="12"
        @marker-click="onMarkerClick"
      />

      <!-- Botón de mi ubicación -->
      <button type="button" class="locate-btn" :class="{ loading: locating }" @click="goToMyLocation">
        {{ locating ? '⏳' : '📍' }}
      </button>

      <!-- Contador -->
      <div class="map-badge">
        {{ visibleMarkers.length }} negocio{{ visibleMarkers.length !== 1 ? 's' : '' }}
      </div>
    </div>

    <!-- Lista de negocios debajo del mapa (scrollable) -->
    <div class="business-list">
      <h2 class="list-title">
        {{ activeCategory || 'Todos los negocios' }}
        <span class="list-count">{{ visibleMarkers.length }}</span>
      </h2>

      <div v-if="visibleMarkers.length === 0" class="empty-state">
        <span>🗺️</span>
        <p>No hay negocios con ubicación en esta categoría.</p>
      </div>

      <article
        v-for="biz in visibleBusinesses"
        :key="biz.id"
        class="biz-row"
        :class="{ highlighted: selectedId === biz.id }"
        @click="selectBusiness(biz)"
      >
        <div class="biz-dot" :style="{ background: categoryColor(biz.category) }" />
        <div class="biz-info">
          <strong>{{ biz.name }}</strong>
          <span class="biz-meta">
            <span class="stars">{{ starsFor(biz.avgRating ?? 0) }}</span>
            {{ (biz.avgRating ?? 0).toFixed(1) }}
            <span v-if="biz.city" class="city">· {{ biz.city }}</span>
          </span>
        </div>
        <router-link :to="`/business/${biz.id}`" class="biz-link" @click.stop>›</router-link>
      </article>

      <!-- Negocios sin coordenadas -->
      <details v-if="businessesWithoutCoords.length > 0" class="no-coords">
        <summary>
          {{ businessesWithoutCoords.length }} negocio{{ businessesWithoutCoords.length !== 1 ? 's' : '' }} sin ubicación en mapa
        </summary>
        <article
          v-for="biz in businessesWithoutCoords"
          :key="biz.id"
          class="biz-row no-map"
        >
          <div class="biz-dot" :style="{ background: categoryColor(biz.category) }" />
          <div class="biz-info">
            <strong>{{ biz.name }}</strong>
            <span class="biz-meta no-coords-label">Sin coordenadas</span>
          </div>
          <router-link :to="`/business/${biz.id}`" class="biz-link">›</router-link>
        </article>
      </details>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useDebounce } from '@vueuse/core';
import apiClient from '@/api/axios';
import LeafletMap from '@/components/ui/LeafletMap.vue';
import type { MapMarker } from '@/components/ui/LeafletMap.vue';
import type { BusinessDto, PageResponse } from '@/types/business';

const router = useRouter();

const businesses = ref<BusinessDto[]>([]);
const loading = ref(true);
const locating = ref(false);
const search = ref('');
const activeCategory = ref('');
const selectedId = ref<string | null>(null);
const debouncedSearch = useDebounce(search, 300);

const mapHeight = computed(() => {
  // En móvil: 45% de la pantalla
  return 'min(45dvh, 320px)';
});

const categories = [
  { name: 'Restaurante', icon: '🍽️' },
  { name: 'Tienda', icon: '🛍️' },
  { name: 'Salud', icon: '🏥' },
  { name: 'Belleza', icon: '💅' },
  { name: 'Servicio', icon: '🔧' },
  { name: 'Educacion', icon: '📚' },
  { name: 'Otro', icon: '📦' },
];

const categoryColors: Record<string, string> = {
  Restaurante: '#fb923c', Tienda: '#60a5fa', Servicio: '#34d399',
  Salud: '#f87171', Belleza: '#f472b6', Educacion: '#a78bfa', Otro: '#94a3b8',
};

// Negocios filtrados por búsqueda y categoría
const filteredBusinesses = computed(() => {
  let list = businesses.value;
  if (activeCategory.value) {
    list = list.filter((b) => b.category === activeCategory.value);
  }
  if (debouncedSearch.value.trim()) {
    const q = debouncedSearch.value.toLowerCase();
    list = list.filter((b) =>
      b.name.toLowerCase().includes(q) ||
      (b.city ?? '').toLowerCase().includes(q) ||
      (b.address ?? '').toLowerCase().includes(q)
    );
  }
  return list;
});

const businessesWithCoords = computed(() =>
  filteredBusinesses.value.filter((b) => b.lat != null && b.lng != null)
);

const businessesWithoutCoords = computed(() =>
  filteredBusinesses.value.filter((b) => b.lat == null || b.lng == null)
);

const visibleBusinesses = computed(() => filteredBusinesses.value);

const visibleMarkers = computed<MapMarker[]>(() =>
  businessesWithCoords.value.map((b) => ({
    id: b.id,
    lat: b.lat!,
    lng: b.lng!,
    title: b.name,
    category: b.category,
    rating: b.avgRating ?? 0,
  }))
);

const categoriesWithCount = computed(() =>
  categories
    .map((cat) => ({
      ...cat,
      count: businesses.value.filter((b) => b.category === cat.name && b.lat != null).length,
    }))
    .filter((cat) => cat.count > 0)
);

onMounted(async () => {
  await loadAllBusinesses();
});

async function loadAllBusinesses(): Promise<void> {
  loading.value = true;
  try {
    // Cargar todas las páginas (para el mapa necesitamos todos)
    let page = 0;
    let all: BusinessDto[] = [];
    let totalPages = 1;

    do {
      const res = await apiClient.get<PageResponse<BusinessDto>>('/api/businesses', {
        params: { page, size: 50 }
      });
      all = [...all, ...res.data.content];
      totalPages = res.data.totalPages;
      page++;
    } while (page < totalPages && page < 10); // máx 500 negocios

    businesses.value = all;
  } catch {
    businesses.value = [];
  } finally {
    loading.value = false;
  }
}

function onMarkerClick(id: string): void {
  selectedId.value = id;
  // Scroll al negocio en la lista
  const el = document.getElementById(`biz-${id}`);
  el?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function selectBusiness(biz: BusinessDto): void {
  selectedId.value = biz.id;
  if (biz.lat != null && biz.lng != null) {
    // El mapa se actualiza reactivamente via visibleMarkers
  }
}

function goToMyLocation(): void {
  if (!navigator.geolocation) return;
  locating.value = true;
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      locating.value = false;
      // Emitir evento al mapa para centrar — por ahora recargamos con nueva posición
      // En una versión futura se puede pasar el centro como prop reactivo
    },
    () => { locating.value = false; },
    { timeout: 8000 }
  );
}

function categoryColor(cat: string): string {
  return categoryColors[cat] ?? '#94a3b8';
}

function starsFor(r: number): string {
  const n = Math.round(r);
  return '★'.repeat(n) + '☆'.repeat(5 - n);
}
</script>

<style scoped>
.map-page {
  display: grid;
  gap: 0;
  min-height: 100%;
}

/* Search */
.search-bar {
  position: sticky;
  top: 56px;
  z-index: 50;
  background: #fff;
  padding: 0.65rem 1rem;
  border-bottom: 1px solid #f1f5f9;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: #f1f5f9;
  border-radius: 999px;
  padding: 0 0.85rem;
  gap: 0.5rem;
}

.search-icon { font-size: 0.9rem; color: #64748b; }

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 0.6rem 0;
  font-size: 0.92rem;
  outline: none;
}

.search-input::-webkit-search-cancel-button { display: none; }

.clear-btn {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  font-size: 0.85rem;
}

/* Filters */
.filters-scroll {
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  padding: 0.5rem 1rem;
  scrollbar-width: none;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
}

.filters-scroll::-webkit-scrollbar { display: none; }

.filter-chip {
  flex-shrink: 0;
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 999px;
  padding: 0.35rem 0.75rem;
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
  color: #475569;
  white-space: nowrap;
  -webkit-tap-highlight-color: transparent;
}

.filter-chip.active {
  background: #0f172a;
  color: #fff;
  border-color: #0f172a;
}

/* Map container */
.map-container {
  position: relative;
  background: #e2e8f0;
}

.map-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  height: min(45dvh, 320px);
  color: #64748b;
  font-size: 0.88rem;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #e2e8f0;
  border-top-color: #0f172a;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* Botón de ubicación */
.locate-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
  z-index: 10;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  font-size: 1.1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  -webkit-tap-highlight-color: transparent;
}

.locate-btn.loading { opacity: 0.6; }

/* Badge contador */
.map-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 10;
  background: rgba(15,23,42,0.85);
  color: #fff;
  border-radius: 999px;
  padding: 0.25rem 0.65rem;
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

/* Business list */
.business-list {
  background: #fff;
  display: grid;
  gap: 0;
}

.list-title {
  margin: 0;
  padding: 0.85rem 1.25rem 0.5rem;
  font-size: 0.92rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border-bottom: 1px solid #f1f5f9;
}

.list-count {
  background: #f1f5f9;
  border-radius: 999px;
  padding: 0.1rem 0.5rem;
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 600;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 2rem 1rem;
  color: #64748b;
  font-size: 0.88rem;
}

.empty-state span { font-size: 2rem; }
.empty-state p { margin: 0; }

/* Business row */
.biz-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.85rem 1.25rem;
  border-bottom: 1px solid #f8fafc;
  cursor: pointer;
  transition: background 0.1s;
  -webkit-tap-highlight-color: transparent;
}

.biz-row:active,
.biz-row.highlighted {
  background: #f0f9ff;
}

.biz-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.biz-info {
  flex: 1;
  display: grid;
  gap: 0.15rem;
  min-width: 0;
}

.biz-info strong {
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.biz-meta {
  font-size: 0.75rem;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.stars { color: #f59e0b; }
.city { color: #94a3b8; }

.biz-link {
  color: #94a3b8;
  font-size: 1.2rem;
  text-decoration: none;
  padding: 0.25rem;
  flex-shrink: 0;
}

/* Sin coordenadas */
.no-coords {
  border-top: 1px solid #f1f5f9;
}

.no-coords summary {
  padding: 0.75rem 1.25rem;
  font-size: 0.82rem;
  color: #94a3b8;
  cursor: pointer;
  list-style: none;
  -webkit-tap-highlight-color: transparent;
}

.biz-row.no-map { opacity: 0.6; }
.no-coords-label { color: #94a3b8; font-style: italic; }
</style>
