<template>
  <article class="review-card">
    <!-- Header: avatar + info usuario -->
    <div class="user-row">
      <div class="avatar">
        <img v-if="review.userAvatar" :src="review.userAvatar" :alt="review.userName ?? 'Usuario'" />
        <span v-else class="avatar-initials">{{ initials }}</span>
      </div>
      <div class="user-info">
        <span class="user-name">{{ review.userName ?? 'Usuario' }}</span>
        <span class="review-time">{{ timeAgo }}</span>
        <StarRating :value="review.rating" size="sm" :readonly="true" />
      </div>
    </div>

    <!-- Body -->
    <p class="review-body">{{ review.body }}</p>

    <!-- Fotos -->
    <div v-if="(review.photos ?? []).length > 0" class="photos-grid">
      <img
        v-for="(url, idx) in review.photos"
        :key="idx"
        :src="url"
        :alt="`Foto ${idx + 1}`"
        class="photo-thumb"
      />
    </div>

    <!-- Acciones -->
    <div class="actions">
      <button type="button" class="report-btn" @click="showReportDialog = true">Reportar</button>
    </div>

    <!-- Dialog de reporte -->
    <Teleport to="body">
      <div v-if="showReportDialog" class="dialog-backdrop" @click.self="showReportDialog = false">
        <div class="dialog-card" role="dialog" aria-modal="true" aria-labelledby="report-dialog-title">
          <h3 id="report-dialog-title">Reportar reseña</h3>

          <label class="field-label" for="report-reason">Motivo</label>
          <select id="report-reason" v-model="reportReason" class="field-input">
            <option value="Spam">Spam</option>
            <option value="Falsa">Reseña falsa</option>
            <option value="Ofensiva">Contenido ofensivo</option>
            <option value="Otra">Otra razón</option>
          </select>

          <label class="field-label" for="report-detail">Detalle (opcional)</label>
          <textarea
            id="report-detail"
            v-model="reportDetail"
            class="field-input"
            rows="3"
            placeholder="Describe el problema..."
          />

          <p v-if="reportSuccess" class="report-ok">Reporte enviado. Gracias.</p>
          <p v-if="reportError" class="report-err">{{ reportError }}</p>

          <div class="dialog-actions">
            <button type="button" class="btn-secondary" @click="showReportDialog = false">Cancelar</button>
            <button type="button" class="btn-primary" :disabled="submitting" @click="submitReport">
              {{ submitting ? 'Enviando...' : 'Enviar' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </article>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useTimeAgo } from '@vueuse/core';
import apiClient from '@/api/axios';
import type { ReviewDto } from '@/types/business';
import StarRating from './StarRating.vue';

const props = defineProps<{
  review: ReviewDto;
}>();

// ── Avatar iniciales ───────────────────────────────────────────────────────
const initials = computed(() => {
  const name = props.review.userName ?? '';
  return name
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0] ?? '')
    .join('')
    .toUpperCase() || '?';
});

// ── Tiempo relativo ────────────────────────────────────────────────────────
const timeAgo = useTimeAgo(computed(() => new Date(props.review.createdAt)));

// ── Reporte ────────────────────────────────────────────────────────────────
const showReportDialog = ref(false);
const reportReason = ref<string>('Spam');
const reportDetail = ref('');
const submitting = ref(false);
const reportSuccess = ref(false);
const reportError = ref('');

async function submitReport(): Promise<void> {
  submitting.value = true;
  reportError.value = '';
  try {
    const reason = reportDetail.value.trim()
      ? `${reportReason.value}: ${reportDetail.value.trim()}`
      : reportReason.value;

    await apiClient.post(`/api/reviews/${props.review.id}/report`, { reason });
    reportSuccess.value = true;
    setTimeout(() => {
      showReportDialog.value = false;
      reportSuccess.value = false;
      reportDetail.value = '';
      reportReason.value = 'Spam';
    }, 1500);
  } catch {
    reportError.value = 'No se pudo enviar el reporte. Intentá de nuevo.';
  } finally {
    submitting.value = false;
  }
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

.review-card {
  background: linear-gradient(135deg, #FFFFFF 0%, var(--color-cream) 100%);
  border: 2px solid var(--color-gold);
  border-radius: 2px;
  padding: 1.5rem;
  display: grid;
  gap: 1rem;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 16px rgba(197, 105, 86, 0.08);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.review-card::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.08) 0%, transparent 70%);
  pointer-events: none;
}

.review-card:hover {
  box-shadow: 0 12px 28px rgba(197, 105, 86, 0.15);
  transform: translateY(-2px);
}

/* ── Usuario Header ── */
.user-row {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  position: relative;
  z-index: 1;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
}

.avatar {
  width: 52px;
  height: 52px;
  border-radius: 0;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--color-gold), var(--color-coral));
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--color-gold);
  position: relative;
}

.avatar::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.2), transparent);
  pointer-events: none;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-initials {
  font-family: 'Georgia', serif;
  font-size: 1.1rem;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.user-name {
  font-family: 'Georgia', serif;
  font-weight: 700;
  color: var(--color-dark);
  font-size: 1rem;
  letter-spacing: 0.3px;
}

.review-time {
  font-size: 0.75rem;
  color: var(--color-text-light);
  font-style: italic;
  letter-spacing: 0.2px;
}

/* ── Body ── */
.review-body {
  margin: 0;
  color: var(--color-dark);
  font-size: 0.95rem;
  line-height: 1.6;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  letter-spacing: 0.3px;
  position: relative;
  z-index: 1;
}

/* ── Fotos ── */
.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 0.75rem;
  position: relative;
  z-index: 1;
}

.photo-thumb {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border: 2px solid var(--color-gold);
  box-shadow: 0 4px 8px rgba(197, 105, 86, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.photo-thumb:hover {
  transform: scale(1.05) rotate(1deg);
  box-shadow: 0 8px 16px rgba(197, 105, 86, 0.2);
}

/* ── Acciones ── */
.actions {
  display: flex;
  justify-content: flex-end;
  position: relative;
  z-index: 1;
  padding-top: 0.5rem;
}

.report-btn {
  background: none;
  border: none;
  font-size: 0.7rem;
  color: var(--color-text-light);
  cursor: pointer;
  padding: 0.4rem 0.8rem;
  border-radius: 0;
  border-bottom: 1px solid var(--color-text-light);
  transition: all 0.2s;
  font-weight: 600;
  letter-spacing: 0.4px;
  text-transform: uppercase;
}

.report-btn:hover {
  color: var(--color-coral);
  border-bottom-color: var(--color-coral);
}

/* ── Dialog ── */
.dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(26, 20, 16, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
  animation: fade-in 0.3s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.dialog-card {
  background: linear-gradient(135deg, #FFFFFF 0%, var(--color-cream) 100%);
  border: 2px solid var(--color-gold);
  border-radius: 0;
  padding: 2rem;
  width: 100%;
  max-width: 420px;
  display: grid;
  gap: 1rem;
  box-shadow: 0 20px 40px rgba(197, 105, 86, 0.2);
  animation: slide-up 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes slide-up {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.dialog-card h3 {
  margin: 0;
  font-family: 'Georgia', serif;
  font-size: 1.3rem;
  color: var(--color-dark);
  font-weight: 700;
  letter-spacing: 0.5px;
}

.field-label {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-dark);
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

.field-input {
  width: 100%;
  border: 1px solid var(--color-gold);
  border-radius: 0;
  padding: 0.75rem;
  font-size: 0.9rem;
  color: var(--color-dark);
  background: rgba(245, 241, 232, 0.8);
  box-sizing: border-box;
  font-family: inherit;
  transition: all 0.2s;
}

.field-input:focus {
  outline: none;
  border-color: var(--color-coral);
  box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.1);
  background: #fff;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.btn-primary {
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: 0;
  padding: 0.7rem 1.4rem;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 600;
  letter-spacing: 0.4px;
  text-transform: uppercase;
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-coral);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(197, 105, 86, 0.3);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: transparent;
  color: var(--color-text-light);
  border: 1px solid var(--color-text-light);
  border-radius: 0;
  padding: 0.7rem 1.4rem;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 600;
  letter-spacing: 0.4px;
  text-transform: uppercase;
}

.btn-secondary:hover {
  background: var(--color-text-light);
  color: #fff;
}

.report-ok {
  margin: 0;
  color: #2d5016;
  font-size: 0.85rem;
  font-weight: 600;
  animation: pulse 0.6s ease;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.report-err {
  margin: 0;
  color: var(--color-coral);
  font-size: 0.85rem;
  font-weight: 600;
}
</style>

