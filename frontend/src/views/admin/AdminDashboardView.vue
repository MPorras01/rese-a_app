<template>
  <main class="admin-page">
    <h1>Panel de administración</h1>

    <nav class="tabs">
      <button
        type="button"
        :class="{ active: activeTab === 'pending' }"
        @click="activeTab = 'pending'"
      >
        Negocios pendientes
        <span v-if="pendingTotal > 0" class="badge">{{ pendingTotal }}</span>
      </button>
    </nav>

    <!-- Negocios pendientes -->
    <section v-if="activeTab === 'pending'">
      <p v-if="loading" class="state-msg">Cargando...</p>
      <p v-else-if="pendingBusinesses.length === 0" class="state-msg">
        No hay negocios pendientes de revisión.
      </p>

      <article
        v-for="biz in pendingBusinesses"
        :key="biz.id"
        class="business-card"
      >
        <div class="biz-info">
          <strong>{{ biz.name }}</strong>
          <span class="category">{{ biz.category }}</span>
          <p v-if="biz.description" class="description">{{ biz.description }}</p>
          <ul class="meta">
            <li v-if="biz.city"><strong>Ciudad:</strong> {{ biz.city }}</li>
            <li v-if="biz.address"><strong>Dirección:</strong> {{ biz.address }}</li>
            <li v-if="biz.phone"><strong>Teléfono:</strong> {{ biz.phone }}</li>
            <li v-if="biz.email"><strong>Email:</strong> {{ biz.email }}</li>
            <li v-if="biz.website">
              <strong>Web:</strong>
              <a :href="biz.website" target="_blank" rel="noreferrer">{{ biz.website }}</a>
            </li>
            <li><strong>Propietario:</strong> {{ biz.ownerName }}</li>
            <li><strong>Registrado:</strong> {{ formatDate(biz.createdAt) }}</li>
          </ul>
        </div>

        <div class="biz-actions">
          <button
            type="button"
            class="btn approve"
            :disabled="actionLoading === biz.id"
            @click="approve(biz.id)"
          >
            ✓ Aprobar
          </button>

          <div class="reject-group">
            <textarea
              v-model="rejectReasons[biz.id]"
              placeholder="Motivo de rechazo (opcional)"
              rows="2"
            />
            <button
              type="button"
              class="btn reject"
              :disabled="actionLoading === biz.id"
              @click="reject(biz.id)"
            >
              ✗ Rechazar
            </button>
          </div>
        </div>
      </article>

      <!-- Paginación -->
      <div v-if="pendingTotalPages > 1" class="pagination">
        <button
          type="button"
          :disabled="pendingPage === 0"
          @click="loadPending(pendingPage - 1)"
        >
          ← Anterior
        </button>
        <span>{{ pendingPage + 1 }} / {{ pendingTotalPages }}</span>
        <button
          type="button"
          :disabled="pendingPage + 1 >= pendingTotalPages"
          @click="loadPending(pendingPage + 1)"
        >
          Siguiente →
        </button>
      </div>
    </section>

    <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>
    <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
  </main>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import apiClient from '@/api/axios';
import type { BusinessDto, PageResponse } from '@/types/business';

const activeTab = ref<'pending'>('pending');
const loading = ref(false);
const actionLoading = ref<string | null>(null);
const pendingBusinesses = ref<BusinessDto[]>([]);
const pendingPage = ref(0);
const pendingTotalPages = ref(1);
const pendingTotal = ref(0);
const rejectReasons = reactive<Record<string, string>>({});
const successMsg = ref('');
const errorMsg = ref('');

onMounted(() => loadPending(0));

async function loadPending(page: number): Promise<void> {
  loading.value = true;
  errorMsg.value = '';
  try {
    const response = await apiClient.get<PageResponse<BusinessDto>>(
      '/api/admin/businesses/pending',
      { params: { page, size: 10 } }
    );
    pendingBusinesses.value = response.data.content;
    pendingPage.value = response.data.number;
    pendingTotalPages.value = response.data.totalPages;
    pendingTotal.value = response.data.totalElements;
  } catch {
    errorMsg.value = 'No se pudo cargar la lista de negocios pendientes.';
  } finally {
    loading.value = false;
  }
}

async function approve(id: string): Promise<void> {
  actionLoading.value = id;
  errorMsg.value = '';
  successMsg.value = '';
  try {
    await apiClient.post(`/api/admin/businesses/${id}/approve`);
    successMsg.value = 'Negocio aprobado correctamente.';
    await loadPending(pendingPage.value);
  } catch {
    errorMsg.value = 'No se pudo aprobar el negocio.';
  } finally {
    actionLoading.value = null;
    clearMsgAfterDelay();
  }
}

async function reject(id: string): Promise<void> {
  actionLoading.value = id;
  errorMsg.value = '';
  successMsg.value = '';
  try {
    const reason = rejectReasons[id] ?? '';
    await apiClient.post(`/api/admin/businesses/${id}/reject`, null, {
      params: { reason: reason || undefined }
    });
    successMsg.value = 'Negocio rechazado.';
    delete rejectReasons[id];
    await loadPending(pendingPage.value);
  } catch {
    errorMsg.value = 'No se pudo rechazar el negocio.';
  } finally {
    actionLoading.value = null;
    clearMsgAfterDelay();
  }
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return Number.isNaN(d.getTime()) ? dateStr : d.toLocaleDateString();
}

function clearMsgAfterDelay(): void {
  setTimeout(() => {
    successMsg.value = '';
    errorMsg.value = '';
  }, 4000);
}
</script>

<style scoped>
.admin-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 1.5rem;
  display: grid;
  gap: 1.25rem;
}

h1 {
  margin: 0;
  font-size: 1.6rem;
}

.tabs {
  display: flex;
  gap: 0.5rem;
}

.tabs button {
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 10px;
  padding: 0.55rem 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.tabs button.active {
  background: #0f172a;
  color: #fff;
  border-color: #0f172a;
}

.badge {
  background: #ef4444;
  color: #fff;
  border-radius: 999px;
  font-size: 0.75rem;
  padding: 0.1rem 0.45rem;
  font-weight: 700;
}

.state-msg {
  color: #64748b;
  margin: 0;
}

.business-card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  padding: 1.1rem;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 1rem;
  align-items: start;
}

.biz-info {
  display: grid;
  gap: 0.4rem;
}

.biz-info strong {
  font-size: 1.05rem;
}

.category {
  background: #f1f5f9;
  border-radius: 999px;
  padding: 0.15rem 0.55rem;
  font-size: 0.8rem;
  color: #475569;
  width: fit-content;
}

.description {
  margin: 0;
  color: #475569;
  font-size: 0.9rem;
}

.meta {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 0.2rem;
  font-size: 0.88rem;
  color: #334155;
}

.meta a {
  color: #1d4ed8;
}

.biz-actions {
  display: grid;
  gap: 0.6rem;
  min-width: 180px;
}

.btn {
  border: none;
  border-radius: 9px;
  padding: 0.55rem 0.9rem;
  font-weight: 600;
  cursor: pointer;
  width: 100%;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.approve {
  background: #16a34a;
  color: #fff;
}

.reject {
  background: #dc2626;
  color: #fff;
}

.reject-group {
  display: grid;
  gap: 0.4rem;
}

.reject-group textarea {
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 0.5rem 0.65rem;
  font-size: 0.85rem;
  font-family: inherit;
  resize: vertical;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  justify-content: center;
}

.pagination button {
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 8px;
  padding: 0.45rem 0.8rem;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.success-msg {
  background: #dcfce7;
  color: #166534;
  border-radius: 10px;
  padding: 0.65rem 0.9rem;
  margin: 0;
}

.error-msg {
  background: #fee2e2;
  color: #991b1b;
  border-radius: 10px;
  padding: 0.65rem 0.9rem;
  margin: 0;
}
</style>
