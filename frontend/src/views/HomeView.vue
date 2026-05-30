<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-content">
        <h1>Descubre los mejores<br /><span class="accent">negocios</span> cerca de ti</h1>
        <p>Lee reseñas reales, comparte tu experiencia y apoya a los negocios locales.</p>
        <router-link to="/explore" class="cta-btn">
          🔍 Explorar negocios
        </router-link>
      </div>
      <div class="hero-illustration">★</div>
    </section>

    <!-- Stats rápidas -->
    <section class="stats-row">
      <div class="stat">
        <strong>{{ stats.businesses }}+</strong>
        <span>Negocios</span>
      </div>
      <div class="stat-divider" />
      <div class="stat">
        <strong>{{ stats.reviews }}+</strong>
        <span>Reseñas</span>
      </div>
      <div class="stat-divider" />
      <div class="stat">
        <strong>{{ stats.cities }}+</strong>
        <span>Ciudades</span>
      </div>
    </section>

    <!-- Categorías -->
    <section class="section">
      <h2 class="section-title">Categorías</h2>
      <div class="categories-grid">
        <button
          v-for="cat in categories"
          :key="cat.name"
          class="cat-chip"
          @click="goToCategory(cat.name)"
        >
          <span class="cat-icon">{{ cat.icon }}</span>
          <span>{{ cat.name }}</span>
        </button>
      </div>
    </section>

    <!-- Negocios destacados -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">Destacados</h2>
        <router-link to="/explore" class="see-all">Ver todos →</router-link>
      </div>
      <div v-if="loadingFeatured" class="skeleton-list">
        <div v-for="n in 3" :key="n" class="skeleton-card" />
      </div>
      <div v-else class="featured-list">
        <BusinessCard
          v-for="biz in featured"
          :key="biz.id"
          :business="biz"
        />
      </div>
    </section>

    <!-- CTA registro -->
    <section v-if="!auth.isAuthenticated" class="cta-section">
      <div class="cta-card">
        <h3>¿Tienes un negocio?</h3>
        <p>Regístralo gratis y empieza a recibir reseñas de tus clientes.</p>
        <router-link to="/login" class="cta-btn cta-dark">Registrar mi negocio</router-link>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/axios';
import { useAuthStore } from '@/stores/auth';
import BusinessCard from '@/components/ui/BusinessCard.vue';
import type { BusinessDto, PageResponse } from '@/types/business';

const router = useRouter();
const auth = useAuthStore();

const featured = ref<BusinessDto[]>([]);
const loadingFeatured = ref(true);
const stats = ref({ businesses: 0, reviews: 0, cities: 0 });

const categories = [
  { name: 'Restaurante', icon: '🍽️' },
  { name: 'Tienda', icon: '🛍️' },
  { name: 'Salud', icon: '🏥' },
  { name: 'Belleza', icon: '💅' },
  { name: 'Servicio', icon: '🔧' },
  { name: 'Educacion', icon: '📚' },
];

onMounted(async () => {
  try {
    const res = await apiClient.get<PageResponse<BusinessDto>>('/api/businesses', {
      params: { page: 0, size: 6 }
    });
    featured.value = res.data.content;
    stats.value.businesses = res.data.totalElements;
    stats.value.reviews = res.data.totalElements * 3; // estimado
    stats.value.cities = Math.max(1, Math.ceil(res.data.totalElements / 2));
  } catch {
    // silencioso
  } finally {
    loadingFeatured.value = false;
  }
});

function goToCategory(name: string): void {
  void router.push({ path: '/explore', query: { category: name } });
}
</script>

<style scoped>
.home {
  display: grid;
  gap: 0;
}

/* Hero */
.hero {
  background: linear-gradient(145deg, #0f172a 0%, #1e3a8a 100%);
  color: #fff;
  padding: 2rem 1.25rem 2.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  position: relative;
  overflow: hidden;
}

.hero-content {
  flex: 1;
  display: grid;
  gap: 0.75rem;
}

.hero-content h1 {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 800;
  line-height: 1.2;
}

.accent { color: #fbbf24; }

.hero-content p {
  margin: 0;
  color: #94a3b8;
  font-size: 0.9rem;
  line-height: 1.5;
}

.hero-illustration {
  font-size: 5rem;
  opacity: 0.12;
  position: absolute;
  right: -0.5rem;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

.cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  background: #fbbf24;
  color: #0f172a;
  font-weight: 700;
  border-radius: 999px;
  padding: 0.7rem 1.25rem;
  text-decoration: none;
  font-size: 0.9rem;
  width: fit-content;
  -webkit-tap-highlight-color: transparent;
}

/* Stats */
.stats-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #fff;
  padding: 1rem;
  border-bottom: 1px solid #f1f5f9;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.1rem;
}

.stat strong {
  font-size: 1.3rem;
  font-weight: 800;
  color: #0f172a;
}

.stat span {
  font-size: 0.72rem;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: #e2e8f0;
}

/* Sections */
.section {
  padding: 1.25rem 1rem;
  display: grid;
  gap: 0.85rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
}

.see-all {
  font-size: 0.85rem;
  color: #1d4ed8;
  text-decoration: none;
  font-weight: 600;
}

/* Categories */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.6rem;
}

.cat-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 0.75rem 0.5rem;
  cursor: pointer;
  font-size: 0.78rem;
  font-weight: 600;
  color: #334155;
  -webkit-tap-highlight-color: transparent;
  transition: background 0.15s;
}

.cat-chip:active { background: #f1f5f9; }

.cat-icon { font-size: 1.5rem; }

/* Featured */
.featured-list {
  display: grid;
  gap: 0.75rem;
}

.skeleton-list {
  display: grid;
  gap: 0.75rem;
}

.skeleton-card {
  height: 120px;
  border-radius: 14px;
  background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}

@keyframes shimmer {
  to { background-position: -200% 0; }
}

/* CTA section */
.cta-section {
  padding: 1rem;
}

.cta-card {
  background: linear-gradient(135deg, #1e3a8a, #0f172a);
  color: #fff;
  border-radius: 18px;
  padding: 1.5rem;
  display: grid;
  gap: 0.6rem;
}

.cta-card h3 {
  margin: 0;
  font-size: 1.1rem;
}

.cta-card p {
  margin: 0;
  color: #94a3b8;
  font-size: 0.88rem;
}

.cta-dark {
  background: #fbbf24;
  color: #0f172a;
}
</style>
