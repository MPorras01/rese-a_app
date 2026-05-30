<template>
  <article class="review-card">
    <div class="review-top">
      <div class="reviewer-avatar">{{ initials }}</div>
      <div class="reviewer-info">
        <strong>{{ review.userName || 'Usuario' }}</strong>
        <span class="date">{{ formattedDate }}</span>
      </div>
      <div class="stars">{{ stars }}</div>
    </div>
    <p class="body">{{ review.body }}</p>
    <div v-if="review.photos && review.photos.length > 0" class="photo-grid">
      <img
        v-for="(photo, index) in review.photos"
        :key="`${review.id}-photo-${index}`"
        :src="photo"
        :alt="`Foto de la reseña ${index + 1}`"
        loading="lazy"
      >
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ReviewDto } from '@/types/business';

const props = defineProps<{ review: ReviewDto }>();

const initials = computed(() =>
  (props.review.userName ?? 'U').split(' ').slice(0, 2).map((w) => w[0]?.toUpperCase() ?? '').join('')
);

const stars = computed(() => {
  const r = Math.round(props.review.rating);
  return '★'.repeat(r) + '☆'.repeat(5 - r);
});

const formattedDate = computed(() => {
  const d = new Date(props.review.createdAt);
  return Number.isNaN(d.getTime()) ? '' : d.toLocaleDateString('es', { day: 'numeric', month: 'short', year: 'numeric' });
});
</script>

<style scoped>
.review-card {
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 14px;
  padding: 0.9rem;
  display: grid;
  gap: 0.6rem;
}

.review-top {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.reviewer-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #0f172a;
  color: #fff;
  font-weight: 700;
  font-size: 0.78rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.reviewer-info {
  flex: 1;
  display: grid;
  gap: 0.05rem;
}

.reviewer-info strong { font-size: 0.88rem; }
.date { font-size: 0.75rem; color: #94a3b8; }

.stars { color: #f59e0b; font-size: 0.9rem; }

.body {
  margin: 0;
  font-size: 0.88rem;
  color: #334155;
  line-height: 1.55;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.45rem;
}

.photo-grid img {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

@media (max-width: 420px) {
  .photo-grid {
    grid-template-columns: 1fr;
  }
}
</style>
