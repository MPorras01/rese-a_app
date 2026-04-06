<template>
  <main class="login-page">
    <section class="card">
      <div class="card-intro">
        <span class="eyebrow">Acceso local</span>
        <h1>Iniciar sesion</h1>
        <p>Ingresa con un usuario de prueba o usa OAuth si ya lo tienes configurado.</p>
      </div>

      <form class="login-form" @submit.prevent="submitLogin">
        <label class="field">
          <span>Email</span>
          <input v-model="email" type="email" autocomplete="username" placeholder="admin@resenaapp.local" />
        </label>

        <label class="field">
          <span>Contrasena</span>
          <input v-model="password" type="password" autocomplete="current-password" placeholder="••••••••" />
        </label>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <button class="btn btn-primary" type="submit" :disabled="auth.loading">
          {{ auth.loading ? 'Ingresando...' : 'Entrar con email' }}
        </button>
      </form>

      <div class="demo-users">
        <h2>Usuarios de prueba</h2>
        <ul>
          <li>
            <strong>Admin</strong>
            <span>admin@resenaapp.local</span>
            <code>Admin123!</code>
          </li>
          <li>
            <strong>Owner</strong>
            <span>owner@resenaapp.local</span>
            <code>Owner123!</code>
          </li>
          <li>
            <strong>Usuario</strong>
            <span>user@resenaapp.local</span>
            <code>User123!</code>
          </li>
        </ul>
      </div>

      <div class="oauth-divider">
        <span>o continuar con</span>
      </div>

      <div class="oauth-actions">
        <button class="btn btn-google" type="button" @click="auth.loginWithGoogle()">
          Entrar con Google
        </button>

        <button class="btn btn-facebook" type="button" @click="auth.loginWithFacebook()">
          Entrar con Facebook
        </button>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import axios from 'axios';
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const email = ref('admin@resenaapp.local');
const password = ref('Admin123!');
const errorMessage = ref('');

async function submitLogin(): Promise<void> {
  errorMessage.value = '';
  try {
    await auth.loginWithPassword(email.value.trim(), password.value);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      errorMessage.value = error.response?.data?.error ?? 'No se pudo iniciar sesion.';
      return;
    }
    errorMessage.value = 'No se pudo iniciar sesion.';
  }
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 180px);
  display: grid;
  place-items: center;
  background: linear-gradient(140deg, #f6f0e6, #fffdf7);
  padding: 2rem 1.5rem;
}

.card {
  width: min(560px, 100%);
  background: linear-gradient(135deg, #ffffff 0%, #f5f1e8 100%);
  border: 2px solid #d4af37;
  box-shadow: 0 20px 40px rgba(197, 105, 86, 0.12);
  padding: 2rem;
  display: grid;
  gap: 1.2rem;
}

.card-intro {
  display: grid;
  gap: 0.4rem;
}

.eyebrow {
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #c56956;
}

h1 {
  margin: 0;
  font-family: Georgia, serif;
}

p {
  margin: 0;
  color: #7a7470;
}

.login-form {
  display: grid;
  gap: 1rem;
}

.field {
  display: grid;
  gap: 0.45rem;
}

.field span {
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #1a1410;
}

.field input {
  border: 1px solid #d4af37;
  background: rgba(255, 255, 255, 0.88);
  padding: 0.9rem 1rem;
  font-size: 0.95rem;
}

.field input:focus {
  outline: 2px solid rgba(197, 105, 86, 0.18);
  border-color: #c56956;
}

.error-message {
  color: #b42318;
  font-size: 0.9rem;
}

.demo-users {
  display: grid;
  gap: 0.75rem;
  padding: 1rem;
  border: 1px dashed rgba(212, 175, 55, 0.7);
  background: rgba(255, 255, 255, 0.72);
}

.demo-users h2 {
  margin: 0;
  font-size: 1rem;
  font-family: Georgia, serif;
}

.demo-users ul {
  list-style: none;
  display: grid;
  gap: 0.75rem;
  padding: 0;
}

.demo-users li {
  display: grid;
  gap: 0.2rem;
}

.demo-users strong {
  color: #1a1410;
}

.demo-users span,
.demo-users code {
  font-size: 0.9rem;
  color: #7a7470;
}

.oauth-divider {
  position: relative;
  text-align: center;
}

.oauth-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(212, 175, 55, 0.5);
}

.oauth-divider span {
  position: relative;
  padding: 0 0.75rem;
  background: #f9f6ef;
  color: #7a7470;
}

.oauth-actions {
  display: grid;
  gap: 0.85rem;
}

.btn {
  border: 0;
  padding: 0.85rem 1rem;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.btn-primary {
  background: #c56956;
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-google {
  background: #ea4335;
}

.btn-facebook {
  background: #1877f2;
}

@media (max-width: 640px) {
  .card {
    padding: 1.25rem;
  }
}
</style>
