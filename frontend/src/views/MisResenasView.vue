<template>
  <div class="mis-resenas-page">
    <div class="page-header">
      <button type="button" class="back-btn" @click="router.back()">‹</button>
      <h1>Mis reseñas</h1>
    </div>

    <div v-if="loading" class="list">
      <div v-for="n in 4" :key="n" class="skeleton-card" />
    </div>

    <div v-else-if="reviews.length === 0" class="empty-state">
      <span>⭐</span>
      <p>Aún no has escrito ninguna reseña.</p>
      <router-link to="/explore" class="explore-btn">Explorar negocios →</router-link>
    </div>

    <div v-else class="list">
      <article v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-top">
          <div class="review-meta">
            <strong class="biz-name">{{ review.businessName }}</strong>
            <span class="stars">{{ starsFor(review.rating) }}</span>
          </div>
          <span class="date">{{ formatDate(review.createdAt) }}</span>
        </div>
        <p class="body">{{ review.body }}</p>
        <button
          type="button"
          class="delete-btn"
          :disabled="deletingId === review.id"
          @click="deleteReview(review.id)"
        >
          {{ deletingId === review.id ? 'Eliminando...' : '🗑 Eliminar' }}
        </button>
      </article>
    </div>

    <div v-if="!loading && totalPages > 1" class="pagination">
      <button type="button" :disabled="page === 0" @click="load(page - 1)">← Anterior</button>
      <span>{{ page + 1 }} / {{ totalPages }}</span>
      <button type="button" :disabled="page + 1 >= totalPages" @click="load(page + 1)">Siguiente →</button>
    </div>

    <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
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

const router = useRouter();
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
    const res = await apiClient.get<PageResponse<MyReviewDto>>('/api/reviews/mine', {
      params: { page: p, size: 10 }
    });
    reviews.value = res.data.content;
    page.value = res.data.number;
    totalPages.value = res.data.totalPages;
  } catch {
    errorMsg.value = 'No se pudieron cargar tus reseñas.';
  } finally {
    loading.value = false;
  }
}

async function deleteReview(id: string): Promise<void> {
  if (!confirm('¿Eliminar esta reseña?')) return;
  deletingId.value = id;
  try {
    await apiClient.delete(`/api/reviews/${id}`);
    await load(page.value);
  } catch {
    errorMsg.value = 'No se pudo eliminar la reseña.';
  } finally {
    deletingId.value = null;
  }
}

function starsFor(r: number): string {
  const n = Math.round(r);
  return '★'.repeat(n) + '☆'.repeat(5 - n);
}

function formatDate(d: string): string {
  const date = new Date(d);
  return Number.isNaN(date.getTime()) ? d : date.toLocaleDateString('es', { day: 'numeric', month: 'short', year: 'numeric' });
}
</script>

<style scoped>
.mis-resenas-page { display: grid; gap: 0; min-height: 100%; }

.page-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
  position: sticky;
  top: 56px;
  z-index: 40;
}

.back-btn {
  background: none;
  border: none;
  font-size: 1.6rem;
  cursor: pointer;
  color: #0f172a;
  line-height: 1;
  padding: 0;
}

.page-header h1 { margin: 0; font-size: 1rem; font-weight: 700; }

.list { display: grid; gap: 0; }

.skeleton-card {
  height: 100px;
  background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
  border-bottom: 1px solid #f1f5f9;
}

@keyframes shimmer { to { background-position: -200% 0; } }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 3rem 1rem;
  color: #64748b;
}

.empty-state span { font-size: 2.5rem; }
.empty-state p { margin: 0; font-size: 0.9rem; }

.explore-btn {
  background: #0f172a;
  color: #fff;
  border-radius: 999px;
  padding: 0.6rem 1.25rem;
  text-decoration: none;
  font-size: 0.88rem;
  font-weight: 600;
}

.review-item {
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
  padding: 1rem 1.25rem;
  display: grid;
  gap: 0.5rem;
}

.review-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
}

.review-meta { display: grid; gap: 0.15rem; }
.biz-name { font-size: 0.92rem; }
.stars { color: #f59e0b; font-size: 0.85rem; }
.date { font-size: 0.75rem; color: #94a3b8; white-space: nowrap; }

.body {
  margin: 0;
  font-size: 0.88rem;
  color: #475569;
  line-height: 1.5;
}

.delete-btn {
  border: 1px solid #fca5a5;
  background: #fff;
  color: #dc2626;
  border-radius: 8px;
  padding: 0.35rem 0.75rem;
  font-size: 0.8rem;
  cursor: pointer;
  width: fit-content;
}

.delete-btn:disabled { opacity: 0.5; cursor: not-allowed; }

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

.error {
  margin: 0;
  padding: 0.75rem 1.25rem;
  background: #fee2e2;
  color: #991b1b;
  font-size: 0.88rem;
}
</style>
