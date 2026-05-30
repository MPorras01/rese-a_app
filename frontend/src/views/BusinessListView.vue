<template>
  <div class="explore-page">
    <!-- Search bar sticky -->
    <div class="search-bar">
      <div class="search-input-wrap">
        <span class="search-icon">🔍</span>
        <input
          v-model="search"
          type="search"
          placeholder="Buscar negocio..."
          class="search-input"
        />
        <button v-if="search" type="button" class="clear-btn" @click="search = ''">✕</button>
      </div>
    </div>

    <!-- Filtros horizontales scrollables -->
    <div class="filters-scroll">
      <button
        type="button"
        class="filter-chip"
        :class="{ active: !category }"
        @click="category = ''"
      >
        Todos
      </button>
      <button
        v-for="cat in categories"
        :key="cat.name"
        type="button"
        class="filter-chip"
        :class="{ active: category === cat.name }"
        @click="category = cat.name"
      >
        {{ cat.icon }} {{ cat.name }}
      </button>
    </div>

    <!-- Ciudad -->
    <div class="city-filter">
      <input v-model="city" type="text" placeholder="🏙️ Filtrar por ciudad..." class="city-input" />
    </div>

    <!-- Resultados -->
    <div class="results-info" v-if="!loading">
      {{ totalElements }} negocio{{ totalElements !== 1 ? 's' : '' }} encontrado{{ totalElements !== 1 ? 's' : '' }}
    </div>

    <!-- Skeleton -->
    <div v-if="loading" class="list">
      <div v-for="n in 5" :key="n" class="skeleton-card" />
    </div>

    <!-- Lista -->
    <div v-else-if="businesses.length > 0" class="list">
      <BusinessCard v-for="biz in businesses" :key="biz.id" :business="biz" />
    </div>

    <!-- Empty -->
    <div v-else class="empty-state">
      <span class="empty-icon">🔍</span>
      <p>No encontramos negocios con esos filtros.</p>
      <button type="button" @click="clearFilters">Limpiar filtros</button>
    </div>

    <!-- Paginación -->
    <div v-if="!loading && totalPages > 1" class="pagination">
      <button type="button" :disabled="page <= 0" @click="prevPage">← Anterior</button>
      <span>{{ page + 1 }} / {{ totalPages }}</span>
      <button type="button" :disabled="page + 1 >= totalPages" @click="nextPage">Siguiente →</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDebounce } from '@vueuse/core';
import apiClient from '@/api/axios';
import BusinessCard from '@/components/ui/BusinessCard.vue';
import type { BusinessDto, PageResponse } from '@/types/business';

const route = useRoute();
const router = useRouter();

const categories = [
  { name: 'Restaurante', icon: '🍽️' },
  { name: 'Tienda', icon: '🛍️' },
  { name: 'Salud', icon: '🏥' },
  { name: 'Belleza', icon: '💅' },
  { name: 'Servicio', icon: '🔧' },
  { name: 'Educacion', icon: '📚' },
  { name: 'Otro', icon: '📦' },
];

const search = ref((route.query.search as string) ?? '');
const city = ref((route.query.city as string) ?? '');
const category = ref((route.query.category as string) ?? '');
const debouncedSearch = useDebounce(search, 400);
const debouncedCity = useDebounce(city, 400);

const businesses = ref<BusinessDto[]>([]);
const loading = ref(false);
const page = ref(0);
const totalPages = ref(1);
const totalElements = ref(0);

let reqId = 0;

watchEffect(async () => {
  const id = ++reqId;
  loading.value = true;
  try {
    const res = await apiClient.get<PageResponse<BusinessDto>>('/api/businesses', {
      params: {
        page: page.value,
        size: 10,
        search: debouncedSearch.value || undefined,
        city: debouncedCity.value.trim() || undefined,
        category: category.value || undefined
      }
    });
    if (id !== reqId) return;
    businesses.value = res.data.content;
    totalPages.value = Math.max(res.data.totalPages || 1, 1);
    totalElements.value = res.data.totalElements || 0;
  } catch {
    if (id !== reqId) return;
    businesses.value = [];
  } finally {
    if (id === reqId) loading.value = false;
  }
});

function prevPage(): void { if (page.value > 0) page.value--; }
function nextPage(): void { if (page.value + 1 < totalPages.value) page.value++; }
function clearFilters(): void { search.value = ''; city.value = ''; category.value = ''; }
</script>

<style scoped>
.explore-page {
  display: grid;
  gap: 0;
}

/* Search */
.search-bar {
  position: sticky;
  top: 56px;
  z-index: 50;
  background: #fff;
  padding: 0.75rem 1rem;
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
  padding: 0.65rem 0;
  font-size: 0.95rem;
  outline: none;
}

.search-input::-webkit-search-cancel-button { display: none; }

.clear-btn {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  font-size: 0.85rem;
  padding: 0.2rem;
}

/* Filters scroll */
.filters-scroll {
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  padding: 0.6rem 1rem;
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
  padding: 0.4rem 0.85rem;
  font-size: 0.82rem;
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

/* City */
.city-filter {
  padding: 0.5rem 1rem;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
}

.city-input {
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 0.55rem 0.85rem;
  font-size: 0.88rem;
  background: #f8fafc;
}

/* Results info */
.results-info {
  padding: 0.6rem 1rem;
  font-size: 0.8rem;
  color: #64748b;
  background: #f8fafc;
}

/* List */
.list {
  display: grid;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
}

.skeleton-card {
  height: 110px;
  border-radius: 14px;
  background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}

@keyframes shimmer { to { background-position: -200% 0; } }

/* Empty */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 3rem 1rem;
  color: #64748b;
}

.empty-icon { font-size: 2.5rem; }
.empty-state p { margin: 0; font-size: 0.9rem; }

.empty-state button {
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 999px;
  padding: 0.55rem 1.1rem;
  font-size: 0.88rem;
  cursor: pointer;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 1rem;
}

.pagination button {
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 999px;
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
  cursor: pointer;
}

.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
.pagination span { font-size: 0.85rem; color: #64748b; }
</style>
