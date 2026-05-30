<template>
  <main class="callback-page">
    <div class="spinner" />
    <p>{{ message }}</p>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const message = ref('Validando autenticacion...');

onMounted(async () => {
  const queryToken = typeof route.query.token === 'string' ? route.query.token : null;
  const hashValue = window.location.hash.startsWith('#')
    ? window.location.hash.substring(1)
    : window.location.hash;
  const hashToken = new URLSearchParams(hashValue).get('token');
  const token = queryToken || hashToken;

  if (!token) {
    message.value = 'Token invalido.';
    await router.push('/login');
    return;
  }

  try {
    await auth.handleOAuthCallback(token);
  } catch {
    message.value = 'No fue posible iniciar sesion.';
    await router.push('/login');
  }
});
</script>

<style scoped>
.callback-page {
  min-height: 100vh;
  display: grid;
  place-content: center;
  justify-items: center;
  gap: 1rem;
  background: #f8fafc;
  color: #334155;
}

.spinner {
  width: 44px;
  height: 44px;
  border: 4px solid #cbd5e1;
  border-top-color: #0f172a;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
