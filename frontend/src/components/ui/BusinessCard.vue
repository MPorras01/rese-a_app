<template>
  <article class="card" @click="goToDetail">
    <div class="card-inner">
      <!-- Accent corner -->
      <div class="accent-corner"></div>
      
      <header class="header">
        <div class="title-section">
          <h3 class="business-name">{{ business.name }}</h3>
          <p class="city">{{ business.city || 'Ciudad no disponible' }}</p>
        </div>
        <span class="badge" :style="{ backgroundColor: categoryColor }">{{ business.category }}</span>
      </header>

      <div class="divider"></div>

      <div class="rating-section">
        <div class="rating-display">
          <span class="stars">{{ stars }}</span>
          <span class="score">{{ avgRatingLabel }}</span>
        </div>
        <span class="reviews-count">({{ business.totalReviews ?? 0 }} reseñas)</span>
      </div>

      <p class="description">{{ shortDescription }}</p>

      <div class="footer-accent"></div>
    </div>
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
    Restaurante: '#C56956',
    Tienda: '#E8644A',
    Servicio: '#D4AF37',
    Salud: '#E8644A',
    Belleza: '#C56956',
    Educacion: '#D4AF37',
    Otro: '#7A7470'
  };

  return palette[props.business.category] ?? '#7A7470';
});

function goToDetail(): void {
  void router.push(`/businesses/${props.business.id}`);
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

.card {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  height: 100%;
  perspective: 1200px;
}

.card:hover {
  transform: translateY(-8px) rotateX(2deg);
}

.card-inner {
  background: linear-gradient(135deg, #FFFFFF 0%, var(--color-cream) 100%);
  border: 2px solid var(--color-gold);
  border-radius: 0;
  padding: 1.5rem;
  display: grid;
  gap: 1rem;
  position: relative;
  overflow: hidden;
  height: 100%;
  box-shadow: 0 8px 24px rgba(197, 105, 86, 0.1);
  transition: box-shadow 0.4s ease;
}

.card-inner::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.12) 0%, transparent 70%);
  pointer-events: none;
}

.card:hover .card-inner {
  box-shadow: 0 16px 40px rgba(197, 105, 86, 0.2);
}

.accent-corner {
  position: absolute;
  top: -1px;
  left: -1px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 40px 40px 0 0;
  border-color: var(--color-gold) transparent transparent transparent;
  opacity: 0.7;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  position: relative;
  z-index: 1;
}

.title-section {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.business-name {
  margin: 0;
  font-family: 'Georgia', serif;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-dark);
  letter-spacing: 0.3px;
  line-height: 1.2;
}

.city {
  margin: 0;
  font-size: 0.75rem;
  color: var(--color-text-light);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.badge {
  color: #fff;
  font-size: 0.65rem;
  padding: 0.5rem 0.75rem;
  border-radius: 0;
  white-space: nowrap;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.4px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--color-gold), transparent);
  position: relative;
  z-index: 1;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  position: relative;
  z-index: 1;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.stars {
  font-size: 1.2rem;
  letter-spacing: 0.1em;
  color: var(--color-gold);
  text-shadow: 0 2px 4px rgba(197, 105, 86, 0.1);
}

.score {
  font-family: 'Georgia', serif;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: 1px;
}

.reviews-count {
  font-size: 0.8rem;
  color: var(--color-text-light);
  font-style: italic;
}

.description {
  margin: 0;
  color: var(--color-dark);
  font-size: 0.85rem;
  line-height: 1.5;
  position: relative;
  z-index: 1;
  letter-spacing: 0.2px;
}

.footer-accent {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--color-coral), var(--color-gold), transparent);
  opacity: 0.5;
}

@media (max-width: 640px) {
  .business-name {
    font-size: 1.1rem;
  }

  .card-inner {
    padding: 1rem;
  }
}
</style>
