<template>
  <article class="card" @click="goToDetail">
    <header class="header">
      <h3>{{ business.name }}</h3>
      <span class="badge" :style="{ backgroundColor: categoryColor }">{{ business.category }}</span>
    </header>

    <p class="city">{{ business.city || 'Ciudad no disponible' }}</p>

    <div class="rating">
      <span class="stars">{{ stars }}</span>
      <span class="score">{{ avgRatingLabel }}</span>
      <span class="reviews">({{ business.totalReviews ?? 0 }} reseñas)</span>
    </div>

    <p class="description">{{ shortDescription }}</p>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import type { BusinessDto } from '@/types/business';

const props = defineProps<{
  business: BusinessDto;
}>();

const router = useRouter();

const shortDescription = computed(() => {
  const text = props.business.description || 'Sin descripción.';
  if (text.length <= 80) {
    return text;
  }
  return `${text.slice(0, 80)}...`;
});

const avg = computed(() => props.business.avgRating ?? 0);
const avgRatingLabel = computed(() => (props.business.avgRating ?? 0).toFixed(1));
const stars = computed(() => {
  const rounded = Math.round(avg.value);
  return `${'★'.repeat(rounded)}${'☆'.repeat(5 - rounded)}`;
});

const categoryColor = computed(() => {
  const palette: Record<string, string> = {
    Restaurante: '#fb923c',
    Tienda: '#60a5fa',
    Servicio: '#34d399',
    Salud: '#f87171',
    Belleza: '#f472b6',
    Educacion: '#a78bfa',
    Otro: '#94a3b8'
  };

  return palette[props.business.category] ?? '#94a3b8';
});

function goToDetail(): void {
  void router.push(`/businesses/${props.business.id}`);
}
</script>

<style scoped>
.card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  padding: 1rem;
  display: grid;
  gap: 0.7rem;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.08);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.6rem;
}

h3 {
  margin: 0;
  font-size: 1.06rem;
}

.badge {
  color: #fff;
  font-size: 0.75rem;
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
  white-space: nowrap;
}

.city,
.description,
.rating {
  margin: 0;
  color: #475569;
}

.stars {
  color: #f59e0b;
}

.score,
.reviews {
  margin-left: 0.35rem;
}
</style>
