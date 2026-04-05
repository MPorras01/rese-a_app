<template>
  <article class="review-card">
    <header>
      <strong>{{ review.userName || 'Usuario' }}</strong>
      <span>{{ stars }} ({{ review.rating.toFixed(1) }})</span>
    </header>
    <p>{{ review.body }}</p>
    <small>{{ formattedDate }}</small>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ReviewDto } from '@/types/business';

const props = defineProps<{
  review: ReviewDto;
}>();

const stars = computed(() => {
  const rounded = Math.round(props.review.rating);
  return `${'★'.repeat(rounded)}${'☆'.repeat(5 - rounded)}`;
});

const formattedDate = computed(() => {
  const date = new Date(props.review.createdAt);
  return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString();
});
</script>

<style scoped>
.review-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  padding: 0.9rem;
  display: grid;
  gap: 0.45rem;
}

header {
  display: flex;
  justify-content: space-between;
  color: #0f172a;
}

p,
small {
  margin: 0;
  color: #334155;
}
</style>
