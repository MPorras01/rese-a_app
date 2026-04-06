<template>
  <main class="business-list">
    <!-- Page Header -->
    <div class="page-header">
      <h1>Descubre negocios</h1>
      <p>Explora miles de opiniones auténticas sobre los mejores lugares.</p>
    </div>

    <!-- Filtros Section -->
    <section class="filters-section">
      <div class="filters-container">
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <input 
            v-model="search" 
            type="text" 
            placeholder="Buscar por nombre..."
            class="search-input"
          />
        </div>

        <select v-model="category" class="filter-select">
          <option value="">Todas las categorías</option>
          <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
        </select>

        <input 
          v-model="city" 
          type="text" 
          placeholder="Ciudad..." 
          class="filter-input"
        />
      </div>
    </section>

    <!-- Loading State -->
    <section v-if="loading" class="businesses-grid">
      <div v-for="n in 6" :key="n" class="skeleton-card" />
    </section>

    <!-- Businesses Grid -->
    <section v-else class="businesses-grid">
      <BusinessCard 
        v-for="business in businesses" 
        :key="business.id" 
        :business="business" 
      />
    </section>

    <!-- Empty State -->
    <div v-if="!loading && businesses.length === 0" class="empty-state">
      <div class="empty-icon">🏢</div>
      <h3>No hay negocios disponibles</h3>
      <p>Intenta con otros filtros o categorías</p>
    </div>

    <!-- Pagination -->
    <footer v-if="businesses.length > 0" class="pagination-section">
      <button 
        type="button" 
        class="btn-pagination btn-pagination--prev"
        :disabled="loading || page <= 0"
        @click="prevPage"
      >
        ← Anterior
      </button>
      
      <div class="pagination-info">
        <span class="current-page">{{ page + 1 }}</span>
        <span class="pagination-divider">/</span>
        <span class="total-pages">{{ totalPages }}</span>
      </div>

      <button 
        type="button" 
        class="btn-pagination btn-pagination--next"
        :disabled="loading || page + 1 >= totalPages"
        @click="nextPage"
      >
        Siguiente →
      </button>
    </footer>
  </main>
</template>

<script setup lang="ts">
import { ref, watchEffect } from 'vue';
import { useDebounce } from '@vueuse/core';
import apiClient from '@/api/axios';
import BusinessCard from '@/components/ui/BusinessCard.vue';
import type { BusinessDto, PageResponse } from '@/types/business';

const categories = ['Restaurante', 'Tienda', 'Servicio', 'Salud', 'Belleza', 'Educacion', 'Otro'];

const search = ref('');
const city = ref('');
const category = ref('');
const debouncedSearch = useDebounce(search, 400);

const businesses = ref<BusinessDto[]>([]);
const loading = ref(false);
const page = ref(0);
const size = ref(9);
const totalPages = ref(1);

let requestId = 0;

watchEffect(async () => {
  const currentRequest = ++requestId;
  loading.value = true;

  try {
    const response = await apiClient.get<PageResponse<BusinessDto>>('/api/businesses', {
      params: {
        page: page.value,
        size: size.value,
        search: debouncedSearch.value || undefined,
        city: city.value.trim() || undefined,
        category: category.value || undefined
      }
    });

    if (currentRequest !== requestId) {
      return;
    }

    businesses.value = response.data.content;
    totalPages.value = Math.max(response.data.totalPages || 1, 1);
  } catch {
    if (currentRequest !== requestId) {
      return;
    }
    businesses.value = [];
    totalPages.value = 1;
  } finally {
    if (currentRequest === requestId) {
      loading.value = false;
    }
  }
});

function prevPage(): void {
  if (page.value > 0) {
    page.value -= 1;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}

function nextPage(): void {
  if (page.value + 1 < totalPages.value) {
    page.value += 1;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
</script>

<style scoped>
:root {
  --color-primary: #C56956;
  --color-gold: #D4AF37;
  --color-coral: #E8644A;
  --color-cream: #F5F1E8;
  --color-dark: #1A1410;
  --color-text-light: #7A7470;
}

.business-list {
  max-width: 1280px;
  margin: 0 auto;
  padding: 3rem 2rem;
  min-height: 70vh;
}

/* Page Header */
.page-header {
  text-align: center;
  margin-bottom: 3rem;
  animation: slide-down 0.6s ease-out;
}

.page-header h1 {
  font-family: 'Georgia', serif;
  font-size: 2.5rem;
  color: var(--color-dark);
  margin-bottom: 0.5rem;
  letter-spacing: 0.5px;
}

.page-header p {
  font-size: 1.1rem;
  color: var(--color-text-light);
  margin: 0;
  letter-spacing: 0.3px;
}

/* Filters Section */
.filters-section {
  margin-bottom: 3rem;
  animation: slide-up 0.6s ease-out;
}

.filters-container {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 1rem;
  align-items: end;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 1rem;
  font-size: 1.2rem;
  pointer-events: none;
  z-index: 1;
}

.search-input,
.filter-select,
.filter-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid var(--color-gold);
  border-radius: 0;
  font-size: 0.95rem;
  background: linear-gradient(135deg, #FFFFFF 0%, var(--color-cream) 100%);
  color: var(--color-dark);
  transition: all 0.2s ease;
  font-family: inherit;
  letter-spacing: 0.2px;
}

.search-input {
  padding-left: 2.75rem;
}

.search-input:focus,
.filter-select:focus,
.filter-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(197, 105, 86, 0.1);
  background: #fff;
}

.filter-input::placeholder {
  color: var(--color-text-light);
}

.filter-select {
  cursor: pointer;
}

/* Grid */
.businesses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
  animation: fade-in 0.6s ease-out;
}

.skeleton-card {
  height: 280px;
  border-radius: 0;
  background: linear-gradient(90deg, #E8E8E8, #F0F0F0, #E8E8E8);
  background-size: 200% 100%;
  animation: loading-pulse 1.5s ease-in-out infinite;
}

@keyframes loading-pulse {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  margin: 2rem 0;
  grid-column: 1 / -1;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.7;
}

.empty-state h3 {
  font-family: 'Georgia', serif;
  font-size: 1.5rem;
  color: var(--color-dark);
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: var(--color-text-light);
  margin: 0;
}

/* Pagination */
.pagination-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2rem;
  padding: 2rem;
  border-top: 2px solid var(--color-gold);
  border-bottom: 2px solid var(--color-gold);
  animation: slide-up 0.6s ease-out;
}

.btn-pagination {
  background: var(--color-primary);
  color: #fff;
  border: 2px solid var(--color-primary);
  border-radius: 0;
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-pagination:hover:not(:disabled) {
  background: var(--color-coral);
  border-color: var(--color-coral);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(197, 105, 86, 0.2);
}

.btn-pagination:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Georgia', serif;
  font-size: 1.1rem;
  color: var(--color-dark);
  font-weight: 600;
  letter-spacing: 0.3px;
}

.pagination-divider {
  color: var(--color-text-light);
}

.current-page {
  color: var(--color-primary);
}

.total-pages {
  color: var(--color-text-light);
}

/* Animations */
@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slide-down {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .business-list {
    padding: 2rem 1rem;
  }

  .page-header h1 {
    font-size: 1.8rem;
  }

  .page-header p {
    font-size: 0.95rem;
  }

  .filters-container {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }

  .businesses-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
    margin-bottom: 2rem;
  }

  .pagination-section {
    flex-direction: column;
    gap: 1rem;
  }

  .btn-pagination {
    width: 100%;
  }
}
</style>
