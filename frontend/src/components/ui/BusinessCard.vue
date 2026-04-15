<template>
  <article class="card" @click="goToDetail">
    <div class="card-body">
      <div class="card-header">
        <h3>{{ business.name }}</h3>
        <span class="badge" :style="{ background: categoryColor }">{{ business.category }}</span>
      </div>

      <p v-if="business.city" class="city">📍 {{ business.city }}</p>
      <p class="description">{{ shortDescription }}</p>
    </div>

    <div class="card-footer">
      <div class="rating">
        <span class="stars">{{ stars }}</span>
        <strong>{{ avgRatingLabel }}</strong>
        <span class="count">({{ business.totalReviews ?? 0 }})</span>
      </div>
      <span class="arrow">›</span>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import type { BusinessDto } from '@/types/business';

const props = defineProps<{ business: BusinessDto }>();
const router = useRouter();

const shortDescription = computed(() => {
  const t = props.business.description || 'Sin descripción.';
  return t.length > 80 ? `${t.slice(0, 80)}...` : t;
});

const avg = computed(() => props.business.avgRating ?? 0);
const avgRatingLabel = computed(() => avg.value.toFixed(1));
const stars = computed(() => {
  const r = Math.round(avg.value);
  return '★'.repeat(r) + '☆'.repeat(5 - r);
});

const categoryColor = computed(() => {
  const p: Record<string, string> = {
    Restaurante: '#fb923c', Tienda: '#60a5fa', Servicio: '#34d399',
    Salud: '#f87171', Belleza: '#f472b6', Educacion: '#a78bfa', Otro: '#94a3b8'
  };
  return p[props.business.category] ?? '#94a3b8';
});

function goToDetail(): void {
  void router.push(`/business/${props.business.id}`);
}
</script>

<style scoped>
.card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  display: grid;
  -webkit-tap-highlight-color: transparent;
  transition: box-shadow 0.15s;
}

.card:active { box-shadow: 0 4px 12px rgba(15,23,42,0.08); }

.card-body {
  padding: 0.9rem 1rem 0.6rem;
  display: grid;
  gap: 0.35rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
}

h3 {
  margin: 0;
  font-size: 0.98rem;
  font-weight: 700;
  line-height: 1.3;
}

.badge {
  color: #fff;
  font-size: 0.7rem;
  padding: 0.15rem 0.5rem;
  border-radius: 999px;
  white-space: nowrap;
  flex-shrink: 0;
}

.city {
  margin: 0;
  font-size: 0.78rem;
  color: #64748b;
}

.description {
  margin: 0;
  font-size: 0.82rem;
  color: #475569;
  line-height: 1.4;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.6rem 1rem;
  border-top: 1px solid #f8fafc;
  background: #fafafa;
}

.rating {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.82rem;
}

.stars { color: #f59e0b; }
.count { color: #94a3b8; }
.arrow { color: #94a3b8; font-size: 1.1rem; }
</style>
