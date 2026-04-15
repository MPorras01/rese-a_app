<template>
  <main class="mis-resenas-page">
    <header class="page-header">
      <router-link to="/profile" class="back-link">← Perfil</router-link>
      <h1>Mis reseñas</h1>
    </header>

    <p v-if="loading" class="state-msg">Cargando...</p>

    <p v-else-if="reviews.length === 0" class="state-msg">
      Aún no has escrito ninguna reseña.
      <router-link to="/explore">Explorar negocios →</router-link>
    </p>

    <template v-else>
      <article v-for="review in reviews" :key="review.id" class="review-card">
        <div class="review-header">
          <div>
            <strong class="business-name">{{ review.businessName }}</strong>
            <span class="stars">{{ starsFor(review.rating) }}</span>
          </div>
          <small>{{ formatDate(review.createdAt) }}</small>
        </div>
        <p class="body">{{ review.body }}</p>
        <button
          type="button"
          class="delete-btn"
          :disabled="deletingId === review.id"
          @click="deleteReview(review.id)"
        >
          {{ deletingId === review.id ? 'Eliminando...' : 'Eliminar' }}
        </button>
      </article>

      <div v-if="totalPages > 1" class="pagination">
        <button type="button" :disabled="page === 0" @click="load(page - 1)">← Anterior</button>
        <span>{{ page + 1 }} / {{ totalPages }}</span>
        <button type="button" :disabled="page + 1 >= totalPages" @click="load(page + 1)">
          Siguiente →
        </button>
      </div>
    </template>

    <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import apiClient from '@/api/axios';
import type { PageResponse } from '@/types/business';

interface MyReviewDto {
  id: string;
  businessId: string;
  businessName: string;
  rating: number;
  body: string;
  createdAt: string;
}

const reviews = ref<MyReviewDto[]>([]);
const loading = ref(false);
const page = ref(0);
const totalPages = ref(1);
const deletingId = ref<string | null>(null);
const errorMsg = ref('');

onMounted(() => load(0));

async function load(p: number): Promise<void> {
  loading.value = true;
  errorMsg.value = '';
  try {
    const response = await apiClient.get<PageResponse<MyReviewDto>>('/api/reviews/mine', {
      params: { page: p, size: 10 }
    });
    reviews.value = response.data.content;
    page.value = response.data.number;
    totalPages.value = response.data.totalPages;
  } catch {
    errorMsg.value = 'No se pudieron cargar tus reseñas.';
  } finally {
    loading.value = false;
  }
}

async function deleteReview(id: string): Promise<void> {
  if (!confirm('¿Eliminar esta reseña?')) return;
  deletingId.value = id;
  errorMsg.value = '';
  try {
    await apiClient.delete(`/api/reviews/${id}`);
    await load(page.value);
  } catch {
    errorMsg.value = 'No se pudo eliminar la reseña.';
  } finally {
    deletingId.value = null;
  }
}

function starsFor(rating: number): string {
  const r = Math.round(rating);
  return '★'.repeat(r) + '☆'.repeat(5 - r);
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return Number.isNaN(d.getTime()) ? dateStr : d.toLocaleDateString();
}
</script>

<style scoped>
.mis-resenas-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 1.5rem;
  display: grid;
  gap: 1rem;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.page-header h1 {
  margin: 0;
  font-size: 1.4rem;
}

.back-link {
  color: #334155;
  text-decoration: none;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 0.35rem 0.7rem;
  font-size: 0.9rem;
}

.state-msg {
  color: #64748b;
  margin: 0;
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.state-msg a {
  color: #1d4ed8;
  text-decoration: none;
}

.review-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  padding: 1rem;
  display: grid;
  gap: 0.5rem;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
}

.business-name {
  display: block;
  font-size: 1rem;
}

.stars {
  color: #f59e0b;
  font-size: 0.95rem;
}

.body {
  margin: 0;
  color: #334155;
  font-size: 0.9rem;
}

.delete-btn {
  border: 1px solid #fca5a5;
  background: #fff;
  color: #dc2626;
  border-radius: 8px;
  padding: 0.35rem 0.7rem;
  cursor: pointer;
  font-size: 0.85rem;
  justify-self: start;
}

.delete-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  justify-content: center;
}

.pagination button {
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 8px;
  padding: 0.4rem 0.75rem;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.error {
  background: #fee2e2;
  color: #991b1b;
  border-radius: 10px;
  padding: 0.65rem 0.9rem;
  margin: 0;
  font-size: 0.9rem;
}
</style>
