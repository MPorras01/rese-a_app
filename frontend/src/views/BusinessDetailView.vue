<template>
  <main class="detail-page" v-if="business">
    <header class="hero">
      <h1>{{ business.name }}</h1>
      <p class="badge">{{ business.category }}</p>
      <p class="rating">{{ (business.avgRating ?? 0).toFixed(1) }} ★</p>
      <p class="reviews">{{ totalReviews }} reseñas</p>
    </header>

    <nav class="tabs">
      <button type="button" :class="{ active: activeTab === 'reviews' }" @click="activeTab = 'reviews'">Reseñas</button>
      <button type="button" :class="{ active: activeTab === 'products' }" @click="activeTab = 'products'">Productos</button>
      <button type="button" :class="{ active: activeTab === 'info' }" @click="activeTab = 'info'">Info</button>
    </nav>

    <section v-if="activeTab === 'reviews'" class="panel">
      <ReviewCard v-for="review in reviews" :key="review.id" :review="review" />
      <button v-if="hasMoreReviews" type="button" @click="loadMoreReviews">Cargar mas</button>
      <button
        v-if="canWriteReview"
        type="button"
        class="write-review"
        @click="goToWriteReview"
      >
        Escribir reseña
      </button>
    </section>

    <section v-else-if="activeTab === 'products'" class="panel products">
      <article v-for="product in products" :key="product.id" class="product-card">
        <h3>{{ product.name }}</h3>
        <p>{{ product.description || 'Sin descripción' }}</p>
        <small>{{ product.priceRange || 'Precio no informado' }}</small>
      </article>
    </section>

    <section v-else class="panel info">
      <p><strong>Direccion:</strong> {{ business.address || 'No disponible' }}</p>
      <p>
        <strong>Telefono:</strong>
        <a v-if="business.phone" :href="`tel:${business.phone}`">{{ business.phone }}</a>
        <span v-else>No disponible</span>
      </p>
      <p>
        <strong>Email:</strong>
        <a v-if="business.email" :href="`mailto:${business.email}`">{{ business.email }}</a>
        <span v-else>No disponible</span>
      </p>
      <p>
        <strong>Sitio web:</strong>
        <a v-if="business.website" :href="business.website" target="_blank" rel="noreferrer">{{ business.website }}</a>
        <span v-else>No disponible</span>
      </p>
    </section>
  </main>

  <main v-else class="detail-page">
    <p>Cargando negocio...</p>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/axios';
import { useAuthStore } from '@/stores/auth';
import type { BusinessDto, ProductDto, ReviewDto, PageResponse } from '@/types/business';
import ReviewCard from '@/components/ui/ReviewCard.vue';

const props = defineProps<{
  id: string;
}>();

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

const hasMoreReviews = computed(() => reviewsPage.value + 1 < reviewsTotalPages.value);
const canWriteReview = computed(() => authStore.isActive && !hasReviewed.value);

onMounted(async () => {
  await Promise.all([loadBusiness(), loadProducts(), loadReviews(0)]);
});

async function loadBusiness(): Promise<void> {
  const response = await apiClient.get<BusinessDto>(`/api/businesses/${props.id}`);
  business.value = response.data;
}

async function loadProducts(): Promise<void> {
  const response = await apiClient.get<ProductDto[]>('/api/products', {
    params: {
      businessId: props.id
    }
  });
  products.value = response.data;
}

async function loadReviews(page: number): Promise<void> {
  const response = await apiClient.get<PageResponse<ReviewDto>>('/api/reviews', {
    params: {
      businessId: props.id,
      page,
      size: 10
    }
  });

  reviewsPage.value = response.data.number;
  reviewsTotalPages.value = Math.max(response.data.totalPages || 1, 1);
  totalReviews.value = response.data.totalElements || response.data.content.length;

  if (page === 0) {
    reviews.value = response.data.content;
  } else {
    reviews.value = [...reviews.value, ...response.data.content];
  }

  const currentUserId = authStore.user?.id;
  hasReviewed.value = Boolean(currentUserId && reviews.value.some((item) => item.userId === currentUserId));
}

async function loadMoreReviews(): Promise<void> {
  if (!hasMoreReviews.value) {
    return;
  }

  await loadReviews(reviewsPage.value + 1);
}

function goToWriteReview(): void {
  void router.push(`/businesses/${props.id}/review/new`);
}
</script>

<style scoped>
.detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 1.1rem;
  display: grid;
  gap: 1rem;
}

.hero {
  border-radius: 14px;
  background: linear-gradient(130deg, #0f172a, #1e3a8a);
  color: #fff;
  padding: 1.25rem;
  display: grid;
  gap: 0.35rem;
}

.hero h1,
.hero p {
  margin: 0;
}

.badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  padding: 0.2rem 0.55rem;
  width: fit-content;
}

.tabs {
  display: flex;
  gap: 0.5rem;
}

.tabs button {
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 10px;
  padding: 0.55rem 0.85rem;
}

.tabs button.active {
  background: #0f172a;
  color: #fff;
}

.panel {
  display: grid;
  gap: 0.75rem;
}

.products {
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.product-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 0.9rem;
  display: grid;
  gap: 0.4rem;
}

.product-card h3,
.product-card p,
.product-card small {
  margin: 0;
}

.info p {
  margin: 0;
}

a {
  color: #1d4ed8;
}

.write-review {
  width: fit-content;
  border: 0;
  border-radius: 10px;
  background: #16a34a;
  color: #fff;
  padding: 0.6rem 0.9rem;
}
</style>
