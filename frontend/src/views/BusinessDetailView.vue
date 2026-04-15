<template>
  <div class="detail-page">
    <!-- Back + header flotante -->
    <div class="sticky-header">
      <button type="button" class="back-btn" @click="router.back()">‹</button>
      <span class="sticky-title" v-if="business">{{ business.name }}</span>
    </div>

    <div v-if="!business" class="loading-state">
      <div class="skeleton-hero" />
      <div class="skeleton-tabs" />
      <div class="skeleton-list">
        <div v-for="n in 3" :key="n" class="skeleton-card" />
      </div>
    </div>

    <template v-else>
      <!-- Hero -->
      <section class="hero">
        <div class="hero-body">
          <span class="category-badge">{{ business.category }}</span>
          <h1>{{ business.name }}</h1>
          <p v-if="business.description" class="description">{{ business.description }}</p>
          <div class="hero-meta">
            <span class="stars">{{ starsFor(business.avgRating ?? 0) }}</span>
            <strong>{{ (business.avgRating ?? 0).toFixed(1) }}</strong>
            <span class="review-count">({{ totalReviews }} reseñas)</span>
            <span v-if="business.city" class="city">· 📍 {{ business.city }}</span>
          </div>
        </div>
      </section>

      <!-- Tabs -->
      <div class="tabs">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          type="button"
          class="tab-btn"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          {{ tab.icon }} {{ tab.label }}
        </button>
      </div>

      <!-- Reseñas -->
      <div v-if="activeTab === 'reviews'" class="tab-content">
        <div v-if="canWriteReview" class="write-review-banner" @click="goToWriteReview">
          <span>✍️ Escribe tu reseña</span>
          <span class="banner-arrow">›</span>
        </div>

        <div v-if="reviews.length === 0" class="empty-tab">
          <span>💬</span>
          <p>Sé el primero en reseñar este negocio.</p>
        </div>

        <ReviewCard v-for="review in reviews" :key="review.id" :review="review" />

        <button v-if="hasMoreReviews" type="button" class="load-more" @click="loadMoreReviews">
          Cargar más reseñas
        </button>
      </div>

      <!-- Productos -->
      <div v-else-if="activeTab === 'products'" class="tab-content">
        <div v-if="products.length === 0" class="empty-tab">
          <span>📦</span>
          <p>Este negocio no tiene productos registrados.</p>
        </div>
        <div v-else class="products-list">
          <article v-for="p in products" :key="p.id" class="product-card">
            <div class="product-info">
              <strong>{{ p.name }}</strong>
              <p>{{ p.description || 'Sin descripción' }}</p>
            </div>
            <span v-if="p.priceRange" class="price-tag">{{ p.priceRange }}</span>
          </article>
        </div>
      </div>

      <!-- Info -->
      <div v-else class="tab-content info-list">
        <a v-if="business.phone" :href="`tel:${business.phone}`" class="info-row">
          <span class="info-icon">📞</span>
          <div>
            <span class="info-label">Teléfono</span>
            <span class="info-value">{{ business.phone }}</span>
          </div>
          <span class="info-arrow">›</span>
        </a>

        <a v-if="business.email" :href="`mailto:${business.email}`" class="info-row">
          <span class="info-icon">📧</span>
          <div>
            <span class="info-label">Email</span>
            <span class="info-value">{{ business.email }}</span>
          </div>
          <span class="info-arrow">›</span>
        </a>

        <a v-if="business.website" :href="business.website" target="_blank" rel="noreferrer" class="info-row">
          <span class="info-icon">🌐</span>
          <div>
            <span class="info-label">Sitio web</span>
            <span class="info-value">{{ business.website }}</span>
          </div>
          <span class="info-arrow">›</span>
        </a>

        <div v-if="business.address" class="info-row no-link">
          <span class="info-icon">📍</span>
          <div>
            <span class="info-label">Dirección</span>
            <span class="info-value">{{ business.address }}{{ business.city ? `, ${business.city}` : '' }}</span>
          </div>
        </div>

        <div v-if="!business.phone && !business.email && !business.website && !business.address" class="empty-tab">
          <span>ℹ️</span>
          <p>No hay información de contacto disponible.</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/axios';
import { useAuthStore } from '@/stores/auth';
import type { BusinessDto, ProductDto, ReviewDto, PageResponse } from '@/types/business';
import ReviewCard from '@/components/ui/ReviewCard.vue';

const props = defineProps<{ id: string }>();
const router = useRouter();
const authStore = useAuthStore();

const business = ref<BusinessDto | null>(null);
const products = ref<ProductDto[]>([]);
const reviews = ref<ReviewDto[]>([]);
const reviewsPage = ref(0);
const reviewsTotalPages = ref(1);
const totalReviews = ref(0);
const activeTab = ref<'reviews' | 'products' | 'info'>('reviews');
const hasReviewed = ref(false);

const tabs = [
  { id: 'reviews' as const, icon: '💬', label: 'Reseñas' },
  { id: 'products' as const, icon: '📦', label: 'Productos' },
  { id: 'info' as const, icon: 'ℹ️', label: 'Info' },
];

const hasMoreReviews = computed(() => reviewsPage.value + 1 < reviewsTotalPages.value);
const canWriteReview = computed(() => authStore.isActive && !hasReviewed.value);

onMounted(async () => {
  await Promise.all([loadBusiness(), loadProducts(), loadReviews(0)]);
});

async function loadBusiness(): Promise<void> {
  const res = await apiClient.get<BusinessDto>(`/api/businesses/${props.id}`);
  business.value = res.data;
}

async function loadProducts(): Promise<void> {
  const res = await apiClient.get<ProductDto[]>('/api/products', { params: { businessId: props.id } });
  products.value = res.data;
}

async function loadReviews(page: number): Promise<void> {
  const res = await apiClient.get<PageResponse<ReviewDto>>('/api/reviews', {
    params: { businessId: props.id, page, size: 10 }
  });
  reviewsPage.value = res.data.number;
  reviewsTotalPages.value = Math.max(res.data.totalPages || 1, 1);
  totalReviews.value = res.data.totalElements || res.data.content.length;
  reviews.value = page === 0 ? res.data.content : [...reviews.value, ...res.data.content];
  const uid = authStore.user?.id;
  hasReviewed.value = Boolean(uid && reviews.value.some((r) => r.userId === uid));
}

async function loadMoreReviews(): Promise<void> {
  if (hasMoreReviews.value) await loadReviews(reviewsPage.value + 1);
}

function goToWriteReview(): void {
  void router.push(`/business/${props.id}/review/new`);
}

function starsFor(rating: number): string {
  const r = Math.round(rating);
  return '★'.repeat(r) + '☆'.repeat(5 - r);
}
</script>

<style scoped>
.detail-page {
  display: grid;
  gap: 0;
  min-height: 100%;
}

/* Sticky header */
.sticky-header {
  position: sticky;
  top: 56px;
  z-index: 50;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 1rem;
  height: 48px;
}

.back-btn {
  background: none;
  border: none;
  font-size: 1.6rem;
  cursor: pointer;
  color: #0f172a;
  line-height: 1;
  padding: 0;
  -webkit-tap-highlight-color: transparent;
}

.sticky-title {
  font-weight: 700;
  font-size: 0.95rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Hero */
.hero {
  background: linear-gradient(145deg, #0f172a, #1e3a8a);
  color: #fff;
  padding: 1.5rem 1.25rem;
}

.hero-body { display: grid; gap: 0.5rem; }

.category-badge {
  background: rgba(255,255,255,0.15);
  border-radius: 999px;
  padding: 0.2rem 0.65rem;
  font-size: 0.78rem;
  width: fit-content;
}

.hero-body h1 {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 800;
}

.description {
  margin: 0;
  color: #94a3b8;
  font-size: 0.88rem;
  line-height: 1.5;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  flex-wrap: wrap;
  font-size: 0.88rem;
}

.stars { color: #fbbf24; }
.review-count, .city { color: #94a3b8; }

/* Tabs */
.tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: calc(56px + 48px);
  z-index: 40;
}

.tab-btn {
  flex: 1;
  border: none;
  background: none;
  padding: 0.75rem 0.5rem;
  font-size: 0.82rem;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  -webkit-tap-highlight-color: transparent;
}

.tab-btn.active {
  color: #0f172a;
  border-bottom-color: #0f172a;
}

/* Tab content */
.tab-content {
  display: grid;
  gap: 0.75rem;
  padding: 0.85rem 1rem;
}

/* Write review banner */
.write-review-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 12px;
  padding: 0.85rem 1rem;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.9rem;
  color: #166534;
  -webkit-tap-highlight-color: transparent;
}

.banner-arrow { font-size: 1.2rem; }

/* Empty */
.empty-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 2rem 1rem;
  color: #64748b;
  font-size: 0.9rem;
}

.empty-tab span { font-size: 2rem; }
.empty-tab p { margin: 0; }

/* Products */
.products-list { display: grid; gap: 0.6rem; }

.product-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 0.85rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.75rem;
}

.product-info { flex: 1; display: grid; gap: 0.2rem; }
.product-info strong { font-size: 0.92rem; }
.product-info p { margin: 0; font-size: 0.8rem; color: #64748b; }

.price-tag {
  background: #f1f5f9;
  border-radius: 999px;
  padding: 0.2rem 0.6rem;
  font-size: 0.78rem;
  font-weight: 600;
  color: #334155;
  white-space: nowrap;
}

/* Info */
.info-list { gap: 0; padding: 0; }

.info-row {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 1rem 1.25rem;
  text-decoration: none;
  color: #0f172a;
  border-bottom: 1px solid #f8fafc;
  background: #fff;
  -webkit-tap-highlight-color: transparent;
}

.info-row.no-link { cursor: default; }

.info-icon { font-size: 1.2rem; }

.info-row div {
  flex: 1;
  display: grid;
  gap: 0.1rem;
}

.info-label { font-size: 0.75rem; color: #64748b; }
.info-value { font-size: 0.9rem; font-weight: 500; }
.info-arrow { color: #94a3b8; }

/* Load more */
.load-more {
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 999px;
  padding: 0.65rem 1.25rem;
  font-size: 0.88rem;
  cursor: pointer;
  justify-self: center;
}

/* Skeletons */
.skeleton-hero {
  height: 160px;
  background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}

.skeleton-tabs {
  height: 44px;
  background: #f1f5f9;
}

.skeleton-list { display: grid; gap: 0.75rem; padding: 0.85rem 1rem; }

.skeleton-card {
  height: 90px;
  border-radius: 12px;
  background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}

@keyframes shimmer { to { background-position: -200% 0; } }
</style>
