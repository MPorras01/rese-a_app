<template>
  <main class="admin-dashboard">
    <!-- Sidebar -->
    <aside class="sidebar">
      <nav class="nav">
        <button
          type="button"
          class="nav-btn"
          :class="{ active: activeSection === 'businesses' }"
          @click="activeSection = 'businesses'"
        >
          Negocios pendientes
        </button>
        <button
          type="button"
          class="nav-btn"
          :class="{ active: activeSection === 'reports' }"
          @click="activeSection = 'reports'"
        >
          Reseñas reportadas
        </button>
      </nav>
    </aside>

    <!-- Content -->
    <section class="content">
      <!-- Sección: Negocios ────────────────────────────────────────────────── -->
      <div v-if="activeSection === 'businesses'" class="section">
        <h1>Negocios pendientes</h1>

        <div class="stat-card">
          <p class="stat-label">Total pendientes</p>
          <p class="stat-value">{{ totalPending }}</p>
        </div>

        <DataTable
          :value="businesses"
          :lazy="true"
          :total-records="totalPending"
          :rows="10"
          paginator
          @page="onPageBusinesses"
          class="data-table"
          responsive-layout="scroll"
        >
          <Column field="name" header="Negocio" />
          <Column field="owner.email" header="Email dueño" />
          <Column field="city" header="Ciudad" />
          <Column field="category" header="Categoría" />
          <Column header="Solicitud" body-style="text-align:center">
            <template #body="{ data }">
              {{ formatDate(data.createdAt) }}
            </template>
          </Column>
          <Column header="Acciones" body-style="text-align:center">
            <template #body="{ data }">
              <div class="action-btns">
                <Button
                  label="Aprobar"
                  severity="success"
                  size="small"
                  @click="approveBusiness(data.id)"
                  :loading="approvingId === data.id"
                />
                <Button
                  label="Rechazar"
                  severity="danger"
                  size="small"
                  @click="openRejectDialog(data.id)"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </div>

      <!-- Sección: Reseñas Reportadas ──────────────────────────────────────── -->
      <div v-if="activeSection === 'reports'" class="section">
        <h1>Reseñas reportadas</h1>

        <div class="stat-card">
          <p class="stat-label">Total reportes pendientes</p>
          <p class="stat-value">{{ totalReports }}</p>
        </div>

        <DataTable
          :value="reports"
          :lazy="true"
          :total-records="totalReports"
          :rows="10"
          paginator
          @page="onPageReports"
          class="data-table"
          responsive-layout="scroll"
        >
          <Column header="Reseña" body-style="text-align:left">
            <template #body="{ data }">
              <span :title="data.review.body">
                {{ truncate(data.review.body, 80) }}
              </span>
            </template>
          </Column>
          <Column header="Autor">
            <template #body="{ data }">
              {{ data.review.userName ?? 'Anónimo' }}
            </template>
          </Column>
          <Column field="review.business.name" header="Negocio" />
          <Column field="reason" header="Motivo" />
          <Column header="Fecha" body-style="text-align:center">
            <template #body="{ data }">
              {{ formatDate(data.createdAt) }}
            </template>
          </Column>
          <Column header="Acciones" body-style="text-align:center">
            <template #body="{ data }">
              <div class="action-btns">
                <Button
                  label="Ocultar"
                  severity="warning"
                  size="small"
                  @click="hideReview(data.review.id)"
                  :loading="hidingId === data.review.id"
                />
                <Button
                  label="Descartar"
                  severity="secondary"
                  size="small"
                  @click="dismissReport(data.id)"
                  :loading="dismissingId === data.id"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </section>

    <!-- Dialog: Rechazar negocio ────────────────────────────────────────────── -->
    <Dialog
      v-model:visible="showRejectDialog"
      header="Rechazar negocio"
      :modal="true"
      :closable="true"
      :style="{ width: '500px' }"
    >
      <template #header>
        <span>Motivo del rechazo</span>
      </template>

      <Textarea
        v-model="rejectReason"
        rows="4"
        placeholder="Explica por qué rechazas este negocio..."
        class="w-full"
      />

      <template #footer>
        <Button label="Cancelar" severity="secondary" @click="showRejectDialog = false" />
        <Button
          label="Confirmar rechazo"
          severity="danger"
          @click="confirmReject"
          :loading="rejectingId !== null"
        />
      </template>
    </Dialog>

    <!-- Toast global -->
    <Toast />
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import apiClient from '@/api/axios';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';
import Toast from 'primevue/toast';
import { useToast } from 'primevue/usetoast';

// ── Setup ──────────────────────────────────────────────────────────────────
const router = useRouter();
const authStore = useAuthStore();
const toast = useToast();

// Guard: solo admins pueden acceder
if (!authStore.isAdmin) {
  void router.replace('/');
}

// ── Estado ─────────────────────────────────────────────────────────────────
const activeSection = ref<'businesses' | 'reports'>('businesses');

// Negocios
const businesses = ref<any[]>([]);
const totalPending = ref(0);
const approvingId = ref<string | null>(null);
const rejectingId = ref<string | null>(null);
const showRejectDialog = ref(false);
const rejectReason = ref('');
const selectedBusinessId = ref<string | null>(null);

// Reportes
const reports = ref<any[]>([]);
const totalReports = ref(0);
const hidingId = ref<string | null>(null);
const dismissingId = ref<string | null>(null);

// ── Utilities ──────────────────────────────────────────────────────────────
function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return '';
  return new Intl.DateTimeFormat('es-CO', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}

function truncate(str: string, len: number): string {
  return str.length > len ? `${str.slice(0, len)}...` : str;
}

// ── Negocios ───────────────────────────────────────────────────────────────
async function loadBusinesses(page: number): Promise<void> {
  try {
    const response = await apiClient.get('/api/admin/businesses/pending', {
      params: { page, size: 10 }
    });
    businesses.value = response.data.content || [];
    totalPending.value = response.data.totalElements ?? 0;
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudieron cargar los negocios pendientes.',
      life: 3000
    });
  }
}

function onPageBusinesses(event: any): void {
  void loadBusinesses(event.page);
}

async function approveBusiness(businessId: string): Promise<void> {
  approvingId.value = businessId;
  try {
    await apiClient.put(`/api/admin/businesses/${businessId}/approve`);
    toast.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Negocio aprobado.',
      life: 2000
    });
    void loadBusinesses(0);
  } catch {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudo aprobar el negocio.',
      life: 3000
    });
  } finally {
    approvingId.value = null;
  }
}

function openRejectDialog(businessId: string): void {
  selectedBusinessId.value = businessId;
  rejectReason.value = '';
  showRejectDialog.value = true;
}

async function confirmReject(): Promise<void> {
  if (!selectedBusinessId.value) return;
  rejectingId.value = selectedBusinessId.value;
  try {
    await apiClient.put(`/api/admin/businesses/${selectedBusinessId.value}/reject`, {
      reason: rejectReason.value
    });
    toast.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Negocio rechazado.',
      life: 2000
    });
    showRejectDialog.value = false;
    void loadBusinesses(0);
  } catch {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudo rechazar el negocio.',
      life: 3000
    });
  } finally {
    rejectingId.value = null;
  }
}

// ── Reportes ───────────────────────────────────────────────────────────────
async function loadReports(page: number): Promise<void> {
  try {
    const response = await apiClient.get('/api/admin/reviews/reported', {
      params: { page, size: 10 }
    });
    reports.value = response.data.content || [];
    totalReports.value = response.data.totalElements ?? 0;
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudieron cargar los reportes.',
      life: 3000
    });
  }
}

function onPageReports(event: any): void {
  void loadReports(event.page);
}

async function hideReview(reviewId: string): Promise<void> {
  hidingId.value = reviewId;
  try {
    await apiClient.put(`/api/admin/reviews/${reviewId}/hide`);
    toast.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Reseña ocultada.',
      life: 2000
    });
    void loadReports(0);
  } catch {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudo ocultar la reseña.',
      life: 3000
    });
  } finally {
    hidingId.value = null;
  }
}

async function dismissReport(reportId: string): Promise<void> {
  dismissingId.value = reportId;
  try {
    await apiClient.put(`/api/admin/reports/${reportId}/dismiss`);
    toast.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Reporte descartado.',
      life: 2000
    });
    void loadReports(0);
  } catch {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'No se pudo descartar el reporte.',
      life: 3000
    });
  } finally {
    dismissingId.value = null;
  }
}

// ── Lifecycle ──────────────────────────────────────────────────────────────
onMounted(async () => {
  await loadBusinesses(0);
  await loadReports(0);
});
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
}

/* ── Sidebar ── */
.sidebar {
  width: 12rem;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  padding: 1rem;
  display: flex;
  flex-direction: column;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-btn {
  background: transparent;
  border: none;
  border-left: 3px solid transparent;
  padding: 0.75rem 1rem;
  text-align: left;
  color: #475569;
  cursor: pointer;
  border-radius: 0;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.15s;
}

.nav-btn:hover {
  background: #f1f5f9;
  color: #334155;
}

.nav-btn.active {
  background: #f0f9ff;
  border-left-color: #0369a1;
  color: #0369a1;
}

/* ── Content ── */
.content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

.section {
  display: grid;
  gap: 1.5rem;
}

.section h1 {
  margin: 0;
  font-size: 1.5rem;
  color: #0f172a;
}

/* ── Stats Card ── */
.stat-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.25rem;
  display: grid;
  gap: 0.5rem;
  max-width: 300px;
}

.stat-label {
  margin: 0;
  font-size: 0.85rem;
  color: #94a3b8;
  font-weight: 500;
}

.stat-value {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: #0f172a;
}

/* ── DataTable ── */
.data-table {
  width: 100%;
}

.action-btns {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

/* ── Utilidades ── */
.w-full {
  width: 100%;
}
</style>
