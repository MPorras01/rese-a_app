<template>
  <div class="login-page">
    <div class="login-card">
      <!-- Logo -->
      <div class="logo">
        <span class="logo-star">★</span>
        <h1>ReseñaApp</h1>
        <p>Descubre y reseña los mejores negocios</p>
      </div>

      <!-- OAuth -->
      <div class="oauth-buttons">
        <button type="button" class="oauth-btn google" @click="auth.loginWithGoogle()">
          <span class="oauth-icon">G</span>
          Continuar con Google
        </button>
        <button type="button" class="oauth-btn facebook" @click="auth.loginWithFacebook()">
          <span class="oauth-icon">f</span>
          Continuar con Facebook
        </button>
      </div>

      <div class="divider"><span>o usa tu email</span></div>

      <!-- Local login -->
      <form class="form" @submit.prevent="submitLogin">
        <label class="field">
          <span>Email</span>
          <input v-model="email" type="email" autocomplete="username" required placeholder="tu@email.com" />
        </label>

        <label class="field">
          <span>Contraseña</span>
          <div class="password-wrap">
            <input
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              required
              placeholder="••••••••"
            />
            <button type="button" class="toggle-pw" @click="showPassword = !showPassword">
              {{ showPassword ? '🙈' : '👁️' }}
            </button>
          </div>
        </label>

        <p v-if="error" class="error">{{ error }}</p>

        <button type="submit" class="submit-btn" :disabled="auth.loading">
          {{ auth.loading ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>

      <!-- Demo hint -->
      <details class="demo-hint">
        <summary>Cuenta de prueba</summary>
        <div class="demo-body">
          <p>📧 admin@resena.local</p>
          <p>🔑 Admin12345!</p>
          <button type="button" @click="fillDemo">Usar cuenta demo</button>
        </div>
      </details>

      <p class="terms">
        Al continuar aceptas nuestros
        <router-link to="/about">Términos de uso</router-link>
        y
        <router-link to="/about">Política de privacidad</router-link>.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const email = ref('');
const password = ref('');
const showPassword = ref(false);
const error = ref('');

async function submitLogin(): Promise<void> {
  error.value = '';
  try {
    await auth.loginWithPassword(email.value, password.value);
  } catch {
    error.value = 'Email o contraseña incorrectos.';
  }
}

function fillDemo(): void {
  email.value = 'admin@resena.local';
  password.value = 'Admin12345!';
}
</script>

<style scoped>
.login-page {
  min-height: 100dvh;
  display: flex;
  align-items: flex-end;
  background: linear-gradient(160deg, #0f172a 0%, #1e3a8a 60%, #0f172a 100%);
}

.login-card {
  background: #fff;
  border-radius: 24px 24px 0 0;
  padding: 1.75rem 1.25rem 2rem;
  width: 100%;
  display: grid;
  gap: 1rem;
}

.logo {
  text-align: center;
  display: grid;
  gap: 0.25rem;
}

.logo-star {
  font-size: 2.5rem;
  color: #fbbf24;
}

.logo h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 800;
}

.logo p {
  margin: 0;
  color: #64748b;
  font-size: 0.88rem;
}

.oauth-buttons {
  display: grid;
  gap: 0.6rem;
}

.oauth-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.65rem;
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 12px;
  padding: 0.85rem;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.oauth-btn:active { background: #f8fafc; }

.oauth-icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 0.85rem;
}

.google .oauth-icon { background: #ea4335; color: #fff; }
.facebook .oauth-icon { background: #1877f2; color: #fff; }

.divider {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #94a3b8;
  font-size: 0.82rem;
}

.divider::before, .divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e2e8f0;
}

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
  padding: 0.75rem 0.9rem;
  font-size: 0.95rem;
  width: 100%;
}

.password-wrap {
  position: relative;
}

.password-wrap input {
  padding-right: 2.5rem;
}

.toggle-pw {
  position: absolute;
  right: 0.6rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  padding: 0.2rem;
}

.submit-btn {
  border: none;
  border-radius: 999px;
  background: #0f172a;
  color: #fff;
  font-weight: 700;
  padding: 0.9rem;
  font-size: 1rem;
  cursor: pointer;
}

.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.error {
  background: #fee2e2;
  color: #991b1b;
  border-radius: 10px;
  padding: 0.6rem 0.85rem;
  margin: 0;
  font-size: 0.85rem;
}

.demo-hint {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
}

.demo-hint summary {
  padding: 0.75rem 1rem;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  color: #475569;
  list-style: none;
  -webkit-tap-highlight-color: transparent;
}

.demo-body {
  padding: 0 1rem 0.85rem;
  display: grid;
  gap: 0.25rem;
}

.demo-body p {
  margin: 0;
  font-size: 0.82rem;
  color: #64748b;
  font-family: monospace;
}

.demo-body button {
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  border-radius: 8px;
  padding: 0.45rem 0.85rem;
  font-size: 0.82rem;
  cursor: pointer;
  margin-top: 0.35rem;
  width: fit-content;
}

.terms {
  text-align: center;
  font-size: 0.75rem;
  color: #94a3b8;
  margin: 0;
}

.terms a { color: #1d4ed8; text-decoration: none; }
</style>
