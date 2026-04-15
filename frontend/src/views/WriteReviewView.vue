<template>
  <main class="review-page">
    <header class="page-header">
      <button type="button" class="back-btn" @click="router.back()">← Volver</button>
      <h1>Escribir reseña</h1>
    </header>

    <section v-if="business" class="business-info">
      <strong>{{ business.name }}</strong>
      <span class="badge">{{ business.category }}</span>
    </section>

    <form class="form" @submit.prevent="submitReview">
      <!-- Rating -->
      <fieldset class="rating-field">
        <legend>Calificación *</legend>
        <div class="stars">
          <button
            v-for="star in 5"
            :key="star"
            type="button"
            class="star-btn"
            :class="{ active: star <= form.rating }"
            @click="form.rating = star"
            @mouseover="hoverRating = star"
            @mouseleave="hoverRating = 0"
          >
            {{ star <= (hoverRating || form.rating) ? '★' : '☆' }}
          </button>
        </div>
        <span v-if="form.rating" class="rating-label">{{ ratingLabel }}</span>
      </fieldset>

      <!-- Producto opcional -->
      <label v-if="products.length > 0" class="field">
        <span>Producto (opcional)</span>
        <select v-model="form.productId">
          <option value="">Sin producto específico</option>
          <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
      </label>

      <!-- Cuerpo -->
      <label class="field">
        <span>Tu reseña * <small>(mínimo 10 caracteres)</small></span>
        <textarea
          v-model="form.body"
          rows="5"
          placeholder="Cuéntanos tu experiencia con este negocio..."
          maxlength="2000"
          required
        />
        <small class="char-count">{{ form.body.length }} / 2000</small>
      </label>

      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>

      <button type="submit" class="submit-btn" :disabled="!canSubmit || submitting">
        {{ submitting ? 'Enviando...' : 'Publicar reseña' }}
      </button>
    </form>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/axios';
import type { BusinessDto, ProductDto } from '@/types/business';

const props = defineProps<{
  id: string;
}>();

const router = useRouter();

const business = ref<BusinessDto | null>(null);
const products = ref<ProductDto[]>([]);
const submitting = ref(false);
const errorMsg = ref('');
const hoverRating = ref(0);

const form = reactive({
  rating: 0,
  productId: '',
  body: ''
});

const ratingLabels: Record<number, string> = {
  1: 'Muy malo',
  2: 'Malo',
  3: 'Regular',
  4: 'Bueno',
  5: 'Excelente'
};

const ratingLabel = computed(() => ratingLabels[form.rating] ?? '');
const canSubmit = computed(() => form.rating > 0 && form.body.trim().length >= 10);

onMounted(async () => {
  await Promise.all([loadBusiness(), loadProducts()]);
});

async function loadBusiness(): Promise<void> {
  try {
    const response = await apiClient.get<BusinessDto>(`/api/businesses/${props.id}`);
    business.value = response.data;
  } catch {
    errorMsg.value = 'No se pudo cargar el negocio.';
  }
}

async function loadProducts(): Promise<void> {
  try {
    const response = await apiClient.get<ProductDto[]>('/api/products', {
      params: { businessId: props.id }
    });
    products.value = response.data;
  } catch {
    // productos opcionales, no bloquear
  }
}

async function submitReview(): Promise<void> {
  if (!canSubmit.value) return;

  submitting.value = true;
  errorMsg.value = '';

  try {
    await apiClient.post('/api/reviews', {
      businessId: props.id,
      productId: form.productId || null,
      rating: form.rating,
      body: form.body.trim()
    });

    await router.push(`/business/${props.id}`);
  } catch (err: unknown) {
    const axiosErr = err as { response?: { data?: { error?: string } } };
    errorMsg.value =
      axiosErr.response?.data?.error ?? 'No se pudo publicar la reseña. Intenta nuevamente.';
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.review-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 1.25rem;
  display: grid;
  gap: 1.25rem;
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

.back-btn {
  background: none;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 0.4rem 0.75rem;
  cursor: pointer;
  color: #334155;
}

.business-info {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 0.75rem 1rem;
}

.badge {
  background: #e2e8f0;
  border-radius: 999px;
  padding: 0.15rem 0.55rem;
  font-size: 0.8rem;
  color: #475569;
}

.form {
  display: grid;
  gap: 1rem;
}

.rating-field {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 0.85rem 1rem;
  display: grid;
  gap: 0.5rem;
}

.rating-field legend {
  font-weight: 600;
  color: #0f172a;
  padding: 0 0.25rem;
}

.stars {
  display: flex;
  gap: 0.25rem;
}

.star-btn {
  background: none;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  color: #cbd5e1;
  line-height: 1;
  padding: 0;
  transition: color 0.1s;
}

.star-btn.active,
.star-btn:hover {
  color: #f59e0b;
}

.rating-label {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.field {
  display: grid;
  gap: 0.4rem;
  color: #334155;
  font-weight: 600;
}

.field span small {
  font-weight: 400;
  color: #64748b;
}

select,
textarea {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 0.7rem 0.85rem;
  font-size: 0.95rem;
  font-family: inherit;
  resize: vertical;
}

.char-count {
  text-align: right;
  color: #94a3b8;
  font-size: 0.8rem;
  font-weight: 400;
}

.error {
  background: #fee2e2;
  color: #991b1b;
  border-radius: 10px;
  padding: 0.65rem 0.9rem;
  margin: 0;
  font-size: 0.9rem;
}

.submit-btn {
  border: none;
  border-radius: 10px;
  background: #16a34a;
  color: #fff;
  font-weight: 600;
  padding: 0.85rem 1.25rem;
  cursor: pointer;
  font-size: 1rem;
  justify-self: start;
}

.submit-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}
</style>
