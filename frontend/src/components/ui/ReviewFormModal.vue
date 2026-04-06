<template>
  <Teleport to="body">
    <div v-if="modelValue" class="overlay" @click.self="closeModal">
      <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="review-form-title">
        <div class="modal-header">
          <h2 id="review-form-title">Reseñar: {{ businessName }}</h2>
          <button type="button" class="close-btn" aria-label="Cerrar" @click="closeModal">✕</button>
        </div>

        <!-- Rating -->
        <div class="field-group">
          <label class="field-label">Puntuación <span class="required">*</span></label>
          <StarRating v-model:value="form.rating" :readonly="false" size="md" />
          <p v-if="errors.rating" class="field-error">{{ errors.rating }}</p>
        </div>

        <!-- Producto (opcional) -->
        <div v-if="products.length > 0" class="field-group">
          <label class="field-label" for="review-product">Reseñar un producto específico (opcional)</label>
          <select id="review-product" v-model="form.productId" class="field-input">
            <option value="">— Negocio en general —</option>
            <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>

        <!-- Body -->
        <div class="field-group">
          <label class="field-label" for="review-body">
            Tu reseña <span class="required">*</span>
          </label>
          <textarea
            id="review-body"
            v-model="form.body"
            class="field-input"
            rows="4"
            minlength="10"
            maxlength="1000"
            placeholder="Contá tu experiencia (mínimo 10 caracteres)..."
          />
          <span class="char-count" :class="{ 'char-count--warn': form.body.length > 900 }">
            {{ form.body.length }} / 1000
          </span>
          <p v-if="errors.body" class="field-error">{{ errors.body }}</p>
        </div>

        <!-- Fotos -->
        <div class="field-group">
          <label class="field-label">Fotos (máx. 3)</label>

          <!-- Capacitor nativo -->
          <button
            v-if="isNative"
            type="button"
            class="btn-secondary"
            :disabled="photoPreviews.length >= 3"
            @click="takeNativePhoto"
          >
            📷 Tomar foto
          </button>

          <!-- Web: input file -->
          <input
            v-else
            ref="fileInputRef"
            type="file"
            accept="image/*"
            multiple
            class="file-input"
            :disabled="photoPreviews.length >= 3"
            @change="onFileChange"
          />

          <!-- Previews -->
          <div v-if="photoPreviews.length > 0" class="photo-previews">
            <div v-for="(preview, idx) in photoPreviews" :key="idx" class="photo-preview-wrap">
              <img :src="preview" alt="Foto preview" class="photo-preview" />
              <button type="button" class="photo-remove" aria-label="Eliminar foto" @click="removePhoto(idx)">✕</button>
            </div>
          </div>
        </div>

        <!-- Mensajes de estado -->
        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

        <!-- Acciones -->
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeModal">Cancelar</button>
          <button
            type="button"
            class="btn-primary"
            :disabled="submitDisabled"
            @click="submitReview"
          >
            <span v-if="loading" class="spinner" aria-hidden="true" />
            {{ loading ? 'Enviando...' : 'Publicar reseña' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import apiClient from '@/api/axios';
import type { ProductDto } from '@/types/business';
import StarRating from './StarRating.vue';

// ── Props / Emits ──────────────────────────────────────────────────────────
const props = defineProps<{
  businessId: string;
  businessName: string;
  products: ProductDto[];
  modelValue: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'submitted'): void;
}>();

// ── Capacitor detection ────────────────────────────────────────────────────
// Detectamos si estamos en plataforma nativa consultando el objeto global que
// Capacitor inyecta en el WebView; así evitamos importar el módulo en entorno web.
const isNative = computed(() => {
  const win = window as { Capacitor?: { isNativePlatform?: () => boolean } };
  return win.Capacitor?.isNativePlatform?.() ?? false;
});

// ── Estado del formulario ──────────────────────────────────────────────────
const form = reactive({
  rating: 0,
  body: '',
  productId: '' as string,
});

const errors = reactive({
  rating: '',
  body: '',
});

const photoFiles = ref<File[]>([]);
const photoPreviews = ref<string[]>([]);
const fileInputRef = ref<HTMLInputElement | null>(null);
const loading = ref(false);
const errorMessage = ref('');

const submitDisabled = computed(
  () => loading.value || form.rating === 0 || form.body.length < 10
);

// Resetear al abrir/cerrar
watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      form.rating = 0;
      form.body = '';
      form.productId = '';
      photoFiles.value = [];
      photoPreviews.value = [];
      errorMessage.value = '';
      errors.rating = '';
      errors.body = '';
    }
  }
);

// ── Métodos ────────────────────────────────────────────────────────────────
function closeModal(): void {
  emit('update:modelValue', false);
}

function onFileChange(event: Event): void {
  const input = event.target as HTMLInputElement;
  const files = Array.from(input.files ?? []);
  const remaining = 3 - photoFiles.value.length;
  const toAdd = files.slice(0, remaining);

  for (const file of toAdd) {
    photoFiles.value.push(file);
    const reader = new FileReader();
    reader.onload = (e) => {
      photoPreviews.value.push(e.target?.result as string);
    };
    reader.readAsDataURL(file);
  }

  // Limpiar input para permitir re-selección
  if (fileInputRef.value) {
    fileInputRef.value.value = '';
  }
}

async function takeNativePhoto(): Promise<void> {
  try {
    // Usamos new Function para que vue-tsc no intente resolver el módulo
    // @capacitor/camera, que sólo está disponible en entorno Capacitor nativo.
    type CameraPlugin = {
      Camera: {
        getPhoto: (opts: {
          quality: number;
          resultType: string;
          source: string;
        }) => Promise<{ dataUrl?: string }>;
      };
      CameraResultType: { DataUrl: string };
      CameraSource: { Camera: string };
    };
    const dynamicImport = new Function('m', 'return import(m)') as (m: string) => Promise<unknown>;
    const cap = (await dynamicImport('@capacitor/camera')) as CameraPlugin;
    const { Camera, CameraResultType, CameraSource } = cap;
    const photo = await Camera.getPhoto({
      quality: 80,
      resultType: CameraResultType.DataUrl,
      source: CameraSource.Camera,
    });
    if (photo.dataUrl && photoPreviews.value.length < 3) {
      photoPreviews.value.push(photo.dataUrl);
      const res = await fetch(photo.dataUrl);
      const blob = await res.blob();
      photoFiles.value.push(new File([blob], `photo_${Date.now()}.jpg`, { type: 'image/jpeg' }));
    }
  } catch {
    /* usuario canceló la cámara o Capacitor no disponible */
  }
}

function removePhoto(idx: number): void {
  photoFiles.value.splice(idx, 1);
  photoPreviews.value.splice(idx, 1);
}

function validate(): boolean {
  let valid = true;
  errors.rating = '';
  errors.body = '';

  if (form.rating === 0) {
    errors.rating = 'Seleccioná una puntuación';
    valid = false;
  }
  if (form.body.length < 10) {
    errors.body = 'La reseña debe tener al menos 10 caracteres';
    valid = false;
  }
  return valid;
}

async function uploadPhotos(): Promise<string[]> {
  if (photoFiles.value.length === 0) return [];

  const urls: string[] = [];
  for (const file of photoFiles.value) {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post<{ url: string }>('/api/upload/photos', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    urls.push(response.data.url);
  }
  return urls;
}

async function submitReview(): Promise<void> {
  if (!validate()) return;

  loading.value = true;
  errorMessage.value = '';

  try {
    const photoUrls = await uploadPhotos();

    await apiClient.post(`/api/businesses/${props.businessId}/reviews`, {
      rating: form.rating,
      body: form.body,
      productId: form.productId || undefined,
      photos: photoUrls,
    });

    emit('submitted');
    emit('update:modelValue', false);
  } catch (err: unknown) {
    const status = (err as { response?: { status?: number } })?.response?.status;
    if (status === 409) {
      errorMessage.value = 'Ya reseñaste este negocio.';
      setTimeout(() => emit('update:modelValue', false), 2000);
    } else {
      errorMessage.value = 'No se pudo publicar la reseña. Intentá de nuevo.';
    }
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
/* ── Overlay ── */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1100;
  padding: 1rem;
  min-height: 500px;
}

/* ── Card ── */
.modal-card {
  background: #fff;
  border-radius: 16px;
  padding: 1.5rem;
  width: 100%;
  max-width: 32rem;
  display: grid;
  gap: 1rem;
  max-height: 90vh;
  overflow-y: auto;
}

/* ── Header ── */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.1rem;
  color: #0f172a;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.1rem;
  color: #94a3b8;
  cursor: pointer;
  padding: 0.2rem 0.4rem;
  border-radius: 6px;
  transition: color 0.15s;
}

.close-btn:hover {
  color: #ef4444;
}

/* ── Campos ── */
.field-group {
  display: grid;
  gap: 0.35rem;
}

.field-label {
  font-size: 0.82rem;
  font-weight: 600;
  color: #475569;
}

.required {
  color: #ef4444;
}

.field-input {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  color: #0f172a;
  background: #f8fafc;
  box-sizing: border-box;
  resize: vertical;
  font-family: inherit;
}

.field-input:focus {
  outline: 2px solid #3b82f6;
  outline-offset: 1px;
  background: #fff;
}

.char-count {
  font-size: 0.72rem;
  color: #94a3b8;
  text-align: right;
}

.char-count--warn {
  color: #f59e0b;
}

.field-error {
  margin: 0;
  font-size: 0.78rem;
  color: #ef4444;
}

/* ── Fotos ── */
.file-input {
  font-size: 0.82rem;
  color: #475569;
}

.photo-previews {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.photo-preview-wrap {
  position: relative;
  width: 80px;
  height: 80px;
}

.photo-preview {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.photo-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 0.7rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

/* ── Mensajes ── */
.form-error {
  margin: 0;
  padding: 0.5rem 0.75rem;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #ef4444;
  font-size: 0.82rem;
}

/* ── Acciones ── */
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding-top: 0.25rem;
}

.btn-primary {
  background: #1e3a8a;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0.55rem 1.25rem;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.btn-primary:hover:not(:disabled) {
  background: #1e40af;
}

.btn-primary:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f1f5f9;
  color: #475569;
  border: none;
  border-radius: 8px;
  padding: 0.55rem 1.1rem;
  font-size: 0.875rem;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-secondary:hover:not(:disabled) {
  background: #e2e8f0;
}

.btn-secondary:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* ── Spinner ── */
.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
