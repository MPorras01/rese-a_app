<template>
  <main class="page">
    <section class="filters">
      <input v-model="search" type="text" placeholder="Buscar negocio" />

      <select v-model="category">
        <option value="">Todas las categorias</option>
        <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
      </select>

      <input v-model="city" type="text" placeholder="Ciudad" />
    </section>

    <section v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="n in 6" :key="n" class="skeleton" />
    </section>

    <section v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <BusinessCard v-for="business in businesses" :key="business.id" :business="business" />
    </section>

    <footer class="pagination">
      <button type="button" :disabled="loading || page <= 0" @click="prevPage">Anterior</button>
      <span>Pagina {{ page + 1 }} de {{ totalPages }}</span>
      <button type="button" :disabled="loading || page + 1 >= totalPages" @click="nextPage">Siguiente</button>
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
  }
}

function nextPage(): void {
  if (page.value + 1 < totalPages.value) {
    page.value += 1;
  }
}
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.2rem;
  display: grid;
  gap: 1rem;
}

.filters {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.6rem;
}

@media (min-width: 768px) {
  .filters {
    grid-template-columns: 1.8fr 1fr 1fr;
  }
}

input,
select {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 0.7rem 0.9rem;
  font-size: 0.95rem;
}

.grid {
  display: grid;
}

.grid-cols-1 {
  grid-template-columns: repeat(1, minmax(0, 1fr));
}

.gap-4 {
  gap: 1rem;
}

@media (min-width: 640px) {
  .sm\:grid-cols-2 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (min-width: 1024px) {
  .lg\:grid-cols-3 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

.skeleton {
  height: 180px;
  border-radius: 12px;
  background: linear-gradient(90deg, #e2e8f0, #f1f5f9, #e2e8f0);
  background-size: 200% 100%;
  animation: shine 1.1s linear infinite;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
}

button {
  border: 0;
  border-radius: 10px;
  padding: 0.6rem 1rem;
  background: #0f172a;
  color: #fff;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@keyframes shine {
  to {
    background-position: -200% 0;
  }
}
</style>
