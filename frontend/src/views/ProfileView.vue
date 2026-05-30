<template>
  <div class="profile-page">
    <!-- Header con avatar -->
    <section class="profile-header">
      <div class="avatar-wrap">
        <img v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" :alt="auth.user.name" class="avatar" />
        <div v-else class="avatar-placeholder">{{ initials }}</div>
        <button type="button" class="avatar-edit-btn" @click="showEditProfile = true">✏️</button>
      </div>
      <h1>{{ auth.user?.name }}</h1>
      <p class="email">{{ auth.user?.email }}</p>
      <span class="role-badge" :class="auth.user?.role?.toLowerCase()">
        {{ auth.user?.role === 'ADMIN' ? '🛡️ Admin' : '👤 Usuario' }}
      </span>
    </section>

    <!-- Estado de verificación -->
    <section class="verify-section">
      <div class="verify-row" :class="auth.user?.emailVerified ? 'ok' : 'warn'">
        <span>📧 Email</span>
        <span class="verify-status">{{ auth.user?.emailVerified ? '✓ Verificado' : '⚠ Pendiente' }}</span>
      </div>
      <div class="verify-row" :class="auth.user?.phoneVerified ? 'ok' : 'warn'">
        <span>📱 Teléfono {{ auth.user?.phone ? `· ${auth.user.phone}` : '' }}</span>
        <span class="verify-status">{{ auth.user?.phoneVerified ? '✓ Verificado' : '⚠ Pendiente' }}</span>
      </div>
      <router-link v-if="!auth.user?.phoneVerified" to="/verify-phone" class="verify-link">
        Verificar teléfono →
      </router-link>
    </section>

    <!-- Menú de opciones -->
    <nav class="menu-list">
      <router-link to="/profile/reviews" class="menu-item">
        <span class="menu-icon">⭐</span>
        <span class="menu-label">Mis reseñas</span>
        <span class="menu-arrow">›</span>
      </router-link>

      <router-link v-if="!auth.isAdmin" to="/owner/dashboard" class="menu-item">
        <span class="menu-icon">🏪</span>
        <span class="menu-label">Mi negocio</span>
        <span class="menu-arrow">›</span>
      </router-link>

      <router-link v-if="auth.isAdmin" to="/admin" class="menu-item">
        <span class="menu-icon">🛡️</span>
        <span class="menu-label">Panel de administración</span>
        <span class="menu-arrow">›</span>
      </router-link>

      <button type="button" class="menu-item" @click="showEditProfile = true">
        <span class="menu-icon">✏️</span>
        <span class="menu-label">Editar perfil</span>
        <span class="menu-arrow">›</span>
      </button>

      <router-link to="/about" class="menu-item">
        <span class="menu-icon">ℹ️</span>
        <span class="menu-label">Acerca de ReseñaApp</span>
        <span class="menu-arrow">›</span>
      </router-link>

      <router-link to="/contact" class="menu-item">
        <span class="menu-icon">💬</span>
        <span class="menu-label">Contacto y soporte</span>
        <span class="menu-arrow">›</span>
      </router-link>

      <button type="button" class="menu-item danger" @click="confirmLogout">
        <span class="menu-icon">🚪</span>
        <span class="menu-label">Cerrar sesión</span>
      </button>
    </nav>

    <!-- App version -->
    <p class="app-version">ReseñaApp v1.0 · Hecho con ❤️ en Colombia</p>

    <!-- Bottom sheet: editar perfil -->
    <Teleport to="body">
      <div v-if="showEditProfile" class="sheet-overlay" @click.self="showEditProfile = false">
        <div class="sheet">
          <div class="sheet-handle" />
          <h2>Editar perfil</h2>

          <form class="form" @submit.prevent="saveProfile">
            <label class="field">
              <span>Nombre</span>
              <input v-model="form.name" type="text" required maxlength="100" />
            </label>

            <label class="field">
              <span>URL de avatar</span>
              <input v-model="form.avatarUrl" type="url" placeholder="https://..." />
            </label>

            <p v-if="saveError" class="error">{{ saveError }}</p>
            <p v-if="saveSuccess" class="success">✓ Perfil actualizado</p>

            <div class="sheet-actions">
              <button type="button" class="btn-cancel" @click="showEditProfile = false">Cancelar</button>
              <button type="submit" class="btn-save" :disabled="saving">
                {{ saving ? 'Guardando...' : 'Guardar' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import apiClient from '@/api/axios';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const showEditProfile = ref(false);
const saving = ref(false);
const saveError = ref('');
const saveSuccess = ref(false);

const form = reactive({ name: '', avatarUrl: '' });

const initials = computed(() =>
  (auth.user?.name ?? '').split(' ').slice(0, 2).map((w) => w[0]?.toUpperCase() ?? '').join('')
);

onMounted(() => {
  form.name = auth.user?.name ?? '';
  form.avatarUrl = auth.user?.avatarUrl ?? '';
});

async function saveProfile(): Promise<void> {
  saving.value = true;
  saveError.value = '';
  saveSuccess.value = false;
  try {
    await apiClient.put('/api/users/me/profile', {
      name: form.name,
      avatarUrl: form.avatarUrl || null
    });
    await auth.fetchMe();
    saveSuccess.value = true;
    setTimeout(() => { showEditProfile.value = false; saveSuccess.value = false; }, 1200);
  } catch {
    saveError.value = 'No se pudo actualizar el perfil.';
  } finally {
    saving.value = false;
  }
}

function confirmLogout(): void {
  if (confirm('¿Cerrar sesión?')) auth.logout();
}
</script>

<style scoped>
.profile-page {
  display: grid;
  gap: 0;
  min-height: 100%;
}

/* Header */
.profile-header {
  background: linear-gradient(145deg, #0f172a, #1e3a8a);
  color: #fff;
  padding: 2rem 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  text-align: center;
}

.avatar-wrap {
  position: relative;
  margin-bottom: 0.25rem;
}

.avatar, .avatar-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.3);
}

.avatar { object-fit: cover; }

.avatar-placeholder {
  background: #1e3a8a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.4rem;
  color: #fff;
}

.avatar-edit-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #fbbf24;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  font-size: 0.7rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-header h1 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
}

.email {
  margin: 0;
  color: #94a3b8;
  font-size: 0.85rem;
}

.role-badge {
  background: rgba(255,255,255,0.15);
  border-radius: 999px;
  padding: 0.2rem 0.65rem;
  font-size: 0.78rem;
  font-weight: 600;
}

/* Verificación */
.verify-section {
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
  display: grid;
  gap: 0;
}

.verify-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1.25rem;
  font-size: 0.88rem;
  border-bottom: 1px solid #f8fafc;
}

.verify-row.ok { color: #166534; }
.verify-row.warn { color: #92400e; }

.verify-status { font-weight: 600; font-size: 0.8rem; }

.verify-link {
  padding: 0.75rem 1.25rem;
  font-size: 0.88rem;
  color: #1d4ed8;
  text-decoration: none;
  font-weight: 600;
}

/* Menu */
.menu-list {
  background: #fff;
  display: grid;
  gap: 0;
  margin-top: 0.5rem;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 1rem 1.25rem;
  text-decoration: none;
  color: #0f172a;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  border-bottom: 1px solid #f8fafc;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  font-size: 0.95rem;
}

.menu-item:active { background: #f8fafc; }
.menu-item.danger { color: #dc2626; }

.menu-icon { font-size: 1.2rem; }
.menu-label { flex: 1; }
.menu-arrow { color: #94a3b8; font-size: 1.1rem; }

.app-version {
  text-align: center;
  color: #94a3b8;
  font-size: 0.75rem;
  padding: 1.5rem 1rem;
  margin: 0;
}

/* Bottom sheet */
.sheet-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}

.sheet {
  background: #fff;
  border-radius: 20px 20px 0 0;
  padding: 1rem 1.25rem 2rem;
  width: 100%;
  max-height: 90dvh;
  overflow-y: auto;
  display: grid;
  gap: 1rem;
}

.sheet-handle {
  width: 40px;
  height: 4px;
  background: #e2e8f0;
  border-radius: 999px;
  margin: 0 auto;
}

.sheet h2 { margin: 0; font-size: 1.1rem; }

.form { display: grid; gap: 0.85rem; }

.field {
  display: grid;
  gap: 0.35rem;
  font-size: 0.88rem;
  font-weight: 600;
  color: #334155;
}

input {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 0.7rem 0.85rem;
  font-size: 0.95rem;
  width: 100%;
}

.sheet-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.6rem;
}

.btn-cancel {
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 999px;
  padding: 0.75rem;
  font-weight: 600;
  cursor: pointer;
}

.btn-save {
  border: none;
  background: #0f172a;
  color: #fff;
  border-radius: 999px;
  padding: 0.75rem;
  font-weight: 700;
  cursor: pointer;
}

.btn-save:disabled { opacity: 0.5; cursor: not-allowed; }

.error {
  background: #fee2e2;
  color: #991b1b;
  border-radius: 10px;
  padding: 0.6rem 0.85rem;
  margin: 0;
  font-size: 0.85rem;
}

.success {
  background: #dcfce7;
  color: #166534;
  border-radius: 10px;
  padding: 0.6rem 0.85rem;
  margin: 0;
  font-size: 0.85rem;
}
</style>
