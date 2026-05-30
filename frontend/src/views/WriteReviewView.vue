<template>
  <div class="write-review-page">
    <div class="page-header">
      <button type="button" class="back-btn" @click="router.back()">‹</button>
      <h1>Escribir reseña</h1>
    </div>

    <div v-if="business" class="business-banner">
      <span class="biz-icon">📸</span>
      <div>
        <strong>{{ business.name }}</strong>
        <span>{{ business.category }} · Comparte tu experiencia con fotos</span>
      </div>
    </div>

    <form class="form" @submit.prevent="submitReview">
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

      <div v-if="products.length > 0" class="field">
        <label>Producto (opcional)</label>
        <select v-model="form.productId">
          <option value="">Sin producto específico</option>
          <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
      </div>

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

      <div class="field upload-field">
        <div class="upload-head">
          <label>Fotos de tu experiencia (opcional)</label>
          <span class="upload-counter">{{ form.photos.length }}/{{ maxPhotos }}</span>
        </div>

        <input
          ref="fileInput"
          type="file"
          class="hidden-input"
          accept="image/*"
          multiple
          @change="onPhotoPicked"
        >

        <button
          type="button"
          class="upload-dropzone"
          :disabled="form.photos.length >= maxPhotos"
          @click="openFilePicker"
        >
          <span class="camera">📷</span>
          <span class="drop-main">Haz clic para subir imágenes</span>
          <span class="drop-sub">Máximo {{ maxPhotos }} fotos · JPG, PNG, WEBP · hasta {{ maxFileMb }}MB cada una</span>
        </button>

        <p v-if="photoError" class="photo-error">{{ photoError }}</p>

        <div v-if="form.photos.length > 0" class="photos-grid">
          <figure v-for="(photo, index) in form.photos" :key="`${photo.slice(0, 20)}-${index}`" class="photo-tile">
            <img :src="photo" :alt="`Foto de reseña ${index + 1}`">
            <button type="button" class="remove-photo" @click="removePhoto(index)">✕</button>
          </figure>
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
const photoError = ref('');
const hoverRating = ref(0);
const fileInput = ref<HTMLInputElement | null>(null);

const maxPhotos = 4;
const maxFileMb = 3;
const maxImageEdge = 1400;

const form = reactive({ rating: 0, productId: '', body: '', photos: [] as string[] });

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
      body: form.body.trim(),
      photos: form.photos
    });
    await router.push(`/business/${props.id}`);
  } catch (err: unknown) {
    const e = err as { response?: { data?: { error?: string } } };
    errorMsg.value = e.response?.data?.error ?? 'No se pudo publicar la reseña.';
  } finally {
    submitting.value = false;
  }
}

function openFilePicker(): void {
  fileInput.value?.click();
}

async function onPhotoPicked(event: Event): Promise<void> {
  photoError.value = '';
  const input = event.target as HTMLInputElement;
  const files = input.files;

  if (!files || files.length === 0) {
    return;
  }

  const availableSlots = maxPhotos - form.photos.length;
  if (availableSlots <= 0) {
    photoError.value = `Solo puedes subir hasta ${maxPhotos} fotos.`;
    input.value = '';
    return;
  }

  const selected = Array.from(files).slice(0, availableSlots);
  if (files.length > availableSlots) {
    photoError.value = `Se agregaron ${availableSlots} fotos. Límite: ${maxPhotos}.`;
  }

  for (const file of selected) {
    if (!file.type.startsWith('image/')) {
      photoError.value = 'Solo se permiten archivos de imagen.';
      continue;
    }

    if (file.size > maxFileMb * 1024 * 1024) {
      photoError.value = `Cada imagen debe pesar menos de ${maxFileMb}MB.`;
      continue;
    }

    try {
      const dataUrl = await readAsDataUrl(file);
      const optimized = await optimizeImage(dataUrl, file.type);
      form.photos.push(optimized);
    } catch {
      photoError.value = 'No pudimos procesar una de las imágenes. Intenta otra vez.';
    }
  }

  input.value = '';
}

function removePhoto(index: number): void {
  form.photos.splice(index, 1);
}

function readAsDataUrl(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result ?? ''));
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

function optimizeImage(dataUrl: string, mimeType: string): Promise<string> {
  return new Promise((resolve) => {
    const image = new Image();

    image.onload = () => {
      const maxEdge = Math.max(image.width, image.height);
      const ratio = maxEdge > maxImageEdge ? maxImageEdge / maxEdge : 1;

      const canvas = document.createElement('canvas');
      canvas.width = Math.round(image.width * ratio);
      canvas.height = Math.round(image.height * ratio);

      const context = canvas.getContext('2d');
      if (!context) {
        resolve(dataUrl);
        return;
      }

      context.drawImage(image, 0, 0, canvas.width, canvas.height);

      const targetType = mimeType === 'image/png' ? 'image/png' : 'image/jpeg';
      resolve(canvas.toDataURL(targetType, 0.82));
    };

    image.onerror = () => resolve(dataUrl);
    image.src = dataUrl;
  });
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Fraunces:opsz,wght@9..144,700&display=swap');

.write-review-page {
  --ink-900: #0f172a;
  --ink-700: #334155;
  --ink-500: #64748b;
  --line: #e2e8f0;
  --line-strong: #d1dce8;
  --card: #ffffff;
  --mint-500: #059669;
  --mint-600: #047857;
  --amber: #f59e0b;
  --salmon: #ef4444;
  display: grid;
  gap: 0;
  min-height: 100%;
  background:
    radial-gradient(circle at 90% -20%, rgba(251, 191, 36, 0.25), transparent 40%),
    radial-gradient(circle at 0% 10%, rgba(16, 185, 129, 0.18), transparent 45%),
    #f5f8fb;
  font-family: 'Plus Jakarta Sans', sans-serif;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--line);
  position: sticky;
  top: 56px;
  z-index: 40;
}

.back-btn {
  background: none;
  border: none;
  font-size: 1.6rem;
  cursor: pointer;
  color: var(--ink-900);
  line-height: 1;
  padding: 0;
}

.page-header h1 {
  margin: 0;
  font-size: 1.08rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.business-banner {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  background: linear-gradient(140deg, #052e2b, #0f766e 42%, #10b981 115%);
  border-bottom: 1px solid #0d5c4f;
  color: #ecfeff;
}

.biz-icon {
  font-size: 1.4rem;
  width: 2.2rem;
  height: 2.2rem;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.16);
}

.business-banner div {
  display: grid;
  gap: 0.2rem;
}

.business-banner strong {
  font-size: 0.97rem;
  font-weight: 700;
}

.business-banner span {
  font-size: 0.78rem;
  color: rgba(236, 254, 255, 0.85);
}

.form {
  display: grid;
  gap: 1rem;
  padding: 1rem;
}

.rating-section {
  padding: 1.3rem 1.2rem;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 18px;
  box-shadow: 0 16px 30px -30px rgba(15, 23, 42, 0.5);
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
  color: #d1dbe8;
  line-height: 1;
  padding: 0;
  transition: color 0.12s, transform 0.12s;
  -webkit-tap-highlight-color: transparent;
}

.star-btn.active { color: var(--amber); transform: scale(1.1); }

.rating-text {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--ink-900);
}

.field {
  padding: 1rem;
  display: grid;
  gap: 0.45rem;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 18px;
  box-shadow: 0 16px 30px -30px rgba(15, 23, 42, 0.5);
}

.field label {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--ink-700);
}

.required { color: #dc2626; }

select, textarea {
  border: 1px solid var(--line-strong);
  border-radius: 12px;
  padding: 0.7rem 0.85rem;
  font-size: 0.95rem;
  background: #f8fafc;
  width: 100%;
  resize: vertical;
  font-family: inherit;
}

select:focus,
textarea:focus {
  outline: 2px solid rgba(5, 150, 105, 0.2);
  border-color: #6ee7b7;
}

.char-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: #94a3b8;
}

.char-row .warn { color: var(--amber); }

.upload-field {
  gap: 0.75rem;
}

.upload-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.upload-counter {
  font-size: 0.76rem;
  font-weight: 700;
  color: #0369a1;
  background: #e0f2fe;
  border-radius: 999px;
  padding: 0.2rem 0.55rem;
}

.hidden-input {
  display: none;
}

.upload-dropzone {
  border: 1px dashed #22c55e;
  background:
    linear-gradient(130deg, rgba(240, 253, 244, 0.95), rgba(209, 250, 229, 0.9)),
    repeating-linear-gradient(
      -45deg,
      rgba(21, 128, 61, 0.05),
      rgba(21, 128, 61, 0.05) 10px,
      rgba(21, 128, 61, 0.11) 10px,
      rgba(21, 128, 61, 0.11) 20px
    );
  border-radius: 14px;
  width: 100%;
  padding: 1.1rem;
  display: grid;
  gap: 0.25rem;
  text-align: center;
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.upload-dropzone:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.upload-dropzone:not(:disabled):active {
  transform: translateY(1px) scale(0.995);
}

.camera {
  font-size: 1.4rem;
}

.drop-main {
  font-weight: 700;
  color: #14532d;
}

.drop-sub {
  font-size: 0.78rem;
  color: #166534;
}

.photo-error {
  margin: 0;
  color: #9f1239;
  background: #ffe4e6;
  border-radius: 10px;
  font-size: 0.78rem;
  padding: 0.45rem 0.6rem;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.6rem;
}

.photo-tile {
  margin: 0;
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--line);
  background: #e2e8f0;
  aspect-ratio: 1 / 1;
}

.photo-tile img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.remove-photo {
  position: absolute;
  top: 0.35rem;
  right: 0.35rem;
  width: 1.65rem;
  height: 1.65rem;
  border-radius: 999px;
  border: none;
  background: rgba(15, 23, 42, 0.72);
  color: #fff;
  font-size: 0.8rem;
  cursor: pointer;
}

.error {
  margin: 0;
  padding: 0.75rem 1rem;
  background: #fee2e2;
  color: #991b1b;
  border-radius: 14px;
  border: 1px solid #fecaca;
  font-size: 0.88rem;
}

.submit-btn {
  border: none;
  border-radius: 999px;
  background: linear-gradient(110deg, var(--mint-500), var(--mint-600));
  color: #fff;
  font-weight: 700;
  padding: 0.9rem;
  font-size: 1rem;
  cursor: pointer;
  letter-spacing: 0.02em;
  box-shadow: 0 18px 24px -20px rgba(5, 150, 105, 0.95);
}

.submit-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
}

@media (min-width: 720px) {
  .form {
    max-width: 760px;
    margin: 0 auto;
    width: 100%;
  }

  .photos-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}
</style>
