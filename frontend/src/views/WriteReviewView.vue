<template>
  <div class="write-review-page">
    <!-- Header -->
    <div class="page-header">
      <button type="button" class="back-btn" @click="router.back()">‹</button>
      <h1>Escribir reseña</h1>
    </div>

    <!-- Negocio -->
    <div v-if="business" class="business-banner">
      <span class="biz-icon">🏪</span>
      <div>
        <strong>{{ business.name }}</strong>
        <span>{{ business.category }}</span>
      </div>
    </div>

    <form class="form" @submit.prevent="submitReview">
      <!-- Rating -->
      <div class="rating-section">
        <p class="rating-label">¿Cómo fue tu experiencia?</p>
        <div class="stars-row">
          <button
            v-for="star in 5"
            :key="star"
            type="button"
            class="star-btn"
            :class="{ active: star <= (hoverRating || form.rating) }"
            @click="form.rating = star"
            @mouseover="hoverRating = star"
            @mouseleave="hoverRating = 0"
          >★</button>
        </div>
        <p v-if="form.rating" class="rating-text">{{ ratingLabel }}</p>
      </div>

      <!-- Producto -->
      <div v-if="products.length > 0" class="field">
        <label>Producto (opcional)</label>
        <select v-model="form.productId">
          <option value="">Sin producto específico</option>
          <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
      </div>

      <!-- Texto -->
      <div class="field">
        <label>Tu reseña <span class="required">*</span></label>
        <textarea
          v-model="form.body"
          rows="5"
          placeholder="Cuéntanos tu experiencia..."
          maxlength="2000"
          required
        />
        <div class="char-row">
          <span :class="{ warn: form.body.length < 10 }">Mínimo 10 caracteres</span>
          <span>{{ form.body.length }}/2000</span>
        </div>
      </div>

      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>

      <button
        type="submit"
        class="submit-btn"
        :disabled="!canSubmit || submitting"
      >
        {{ submitting ? 'Publicando...' : '✓ Publicar reseña' }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/axios';
import type { BusinessDto, ProductDto } from '@/types/business';

const props = defineProps<{ id: string }>();
const router = useRouter();

const business = ref<BusinessDto | null>(null);
const products = ref<ProductDto[]>([]);
const submitting = ref(false);
const errorMsg = ref('');
const hoverRating = ref(0);

const form = reactive({ rating: 0, productId: '', body: '' });

const ratingLabels: Record<number, string> = {
  1: '😞 Muy malo', 2: '😕 Malo', 3: '😐 Regular', 4: '😊 Bueno', 5: '🤩 Excelente'
};

const ratingLabel = computed(() => ratingLabels[form.rating] ?? '');
const canSubmit = computed(() => form.rating > 0 && form.body.trim().length >= 10);

onMounted(async () => {
  await Promise.all([
    apiClient.get<BusinessDto>(`/api/businesses/${props.id}`).then((r) => { business.value = r.data; }),
    apiClient.get<ProductDto[]>('/api/products', { params: { businessId: props.id } }).then((r) => { products.value = r.data; }).catch(() => {})
  ]);
});

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
    const e = err as { response?: { data?: { error?: string } } };
    errorMsg.value = e.response?.data?.error ?? 'No se pudo publicar la reseña.';
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.write-review-page {
  display: grid;
  gap: 0;
  min-height: 100%;
}

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

.page-header h1 {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
}

.business-banner {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.85rem 1.25rem;
  background: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
}

.biz-icon { font-size: 1.4rem; }

.business-banner div {
  display: grid;
  gap: 0.1rem;
}

.business-banner strong { font-size: 0.92rem; }
.business-banner span { font-size: 0.78rem; color: #64748b; }

.form {
  display: grid;
  gap: 0;
}

.rating-section {
  padding: 1.5rem 1.25rem;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
  display: grid;
  gap: 0.75rem;
  text-align: center;
}

.rating-label {
  margin: 0;
  font-weight: 700;
  font-size: 1rem;
}

.stars-row {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
}

.star-btn {
  background: none;
  border: none;
  font-size: 2.5rem;
  cursor: pointer;
  color: #e2e8f0;
  line-height: 1;
  padding: 0;
  transition: color 0.1s, transform 0.1s;
  -webkit-tap-highlight-color: transparent;
}

.star-btn.active { color: #f59e0b; transform: scale(1.1); }

.rating-text {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #0f172a;
}

.field {
  padding: 1rem 1.25rem;
  display: grid;
  gap: 0.4rem;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
}

.field label {
  font-size: 0.88rem;
  font-weight: 600;
  color: #334155;
}

.required { color: #dc2626; }

select, textarea {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 0.7rem 0.85rem;
  font-size: 0.95rem;
  background: #f8fafc;
  width: 100%;
  resize: vertical;
}

.char-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: #94a3b8;
}

.char-row .warn { color: #f59e0b; }

.error {
  margin: 0;
  padding: 0.75rem 1.25rem;
  background: #fee2e2;
  color: #991b1b;
  font-size: 0.88rem;
}

.submit-btn {
  margin: 1rem 1.25rem;
  border: none;
  border-radius: 999px;
  background: #16a34a;
  color: #fff;
  font-weight: 700;
  padding: 0.9rem;
  font-size: 1rem;
  cursor: pointer;
}

.submit-btn:disabled { background: #94a3b8; cursor: not-allowed; }
</style>
