<template>
  <main class="profile-page">
    <h1>Mi perfil</h1>

    <section class="card">
      <div class="avatar-row">
        <img
          v-if="form.avatarUrl"
          :src="form.avatarUrl"
          alt="Avatar"
          class="avatar"
        />
        <div v-else class="avatar-placeholder">{{ initials }}</div>
        <div>
          <strong>{{ auth.user?.name }}</strong>
          <p>{{ auth.user?.email }}</p>
          <span class="role-badge">{{ auth.user?.role }}</span>
        </div>
      </div>

      <form class="form" @submit.prevent="saveProfile">
        <label class="field">
          <span>Nombre</span>
          <input v-model="form.name" type="text" required maxlength="100" />
        </label>

        <label class="field">
          <span>URL de avatar</span>
          <input v-model="form.avatarUrl" type="url" placeholder="https://..." />
        </label>

        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <p v-if="successMsg" class="success">{{ successMsg }}</p>

        <button type="submit" :disabled="saving">
          {{ saving ? 'Guardando...' : 'Guardar cambios' }}
        </button>
      </form>
    </section>

    <section class="card">
      <h2>Verificación</h2>
      <ul class="verification-list">
        <li :class="auth.user?.emailVerified ? 'verified' : 'pending'">
          Email: {{ auth.user?.email }}
          <span>{{ auth.user?.emailVerified ? '✓ Verificado' : '⏳ Pendiente' }}</span>
        </li>
        <li :class="auth.user?.phoneVerified ? 'verified' : 'pending'">
          Teléfono: {{ auth.user?.phone || 'No registrado' }}
          <span>{{ auth.user?.phoneVerified ? '✓ Verificado' : '⏳ Pendiente' }}</span>
        </li>
      </ul>
      <router-link v-if="!auth.user?.phoneVerified" to="/verify-phone" class="verify-link">
        Verificar teléfono →
      </router-link>
    </section>

    <section class="card links">
      <router-link to="/profile/reviews">Ver mis reseñas →</router-link>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import apiClient from '@/api/axios';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const saving = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

const form = reactive({
  name: '',
  avatarUrl: ''
});

const initials = computed(() => {
  const name = auth.user?.name ?? '';
  return name
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0]?.toUpperCase() ?? '')
    .join('');
});

onMounted(() => {
  form.name = auth.user?.name ?? '';
  form.avatarUrl = auth.user?.avatarUrl ?? '';
});

async function saveProfile(): Promise<void> {
  saving.value = true;
  errorMsg.value = '';
  successMsg.value = '';
  try {
    await apiClient.put('/api/users/me/profile', {
      name: form.name,
      avatarUrl: form.avatarUrl || null
    });
    await auth.fetchMe();
    successMsg.value = 'Perfil actualizado correctamente.';
  } catch {
    errorMsg.value = 'No se pudo actualizar el perfil.';
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 1.5rem;
  display: grid;
  gap: 1.25rem;
}

h1 {
  margin: 0;
  font-size: 1.5rem;
}

h2 {
  margin: 0 0 0.75rem;
  font-size: 1.1rem;
}

.card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  padding: 1.25rem;
  display: grid;
  gap: 0.9rem;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #0f172a;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.1rem;
}

.avatar-row strong {
  display: block;
  font-size: 1rem;
}

.avatar-row p {
  margin: 0;
  color: #64748b;
  font-size: 0.9rem;
}

.role-badge {
  background: #f1f5f9;
  border-radius: 999px;
  padding: 0.1rem 0.5rem;
  font-size: 0.75rem;
  color: #475569;
}

.form {
  display: grid;
  gap: 0.75rem;
}

.field {
  display: grid;
  gap: 0.35rem;
  font-weight: 600;
  color: #334155;
}

input {
  border: 1px solid #cbd5e1;
  border-radius: 9px;
  padding: 0.65rem 0.8rem;
  font-size: 0.95rem;
}

button {
  border: none;
  border-radius: 10px;
  background: #0f172a;
  color: #fff;
  padding: 0.7rem 1rem;
  font-weight: 600;
  cursor: pointer;
  justify-self: start;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.error {
  background: #fee2e2;
  color: #991b1b;
  border-radius: 8px;
  padding: 0.55rem 0.8rem;
  margin: 0;
  font-size: 0.9rem;
}

.success {
  background: #dcfce7;
  color: #166534;
  border-radius: 8px;
  padding: 0.55rem 0.8rem;
  margin: 0;
  font-size: 0.9rem;
}

.verification-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 0.5rem;
}

.verification-list li {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
}

.verified {
  background: #f0fdf4;
  color: #166534;
}

.pending {
  background: #fefce8;
  color: #854d0e;
}

.verify-link {
  color: #1d4ed8;
  font-size: 0.9rem;
  text-decoration: none;
}

.links a {
  color: #1d4ed8;
  text-decoration: none;
  font-size: 0.95rem;
}
</style>
