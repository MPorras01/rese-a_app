<template>
  <main class="login-page">
    <section class="card">
      <h1>Iniciar sesion</h1>
      <p>Usa el acceso local de prueba o un proveedor OAuth.</p>

      <form class="local-form" @submit.prevent="submitLocalLogin">
        <label>
          <span>Email</span>
          <input v-model="email" type="email" autocomplete="username" required />
        </label>

        <label>
          <span>Contrasena</span>
          <input v-model="password" type="password" autocomplete="current-password" required />
        </label>

        <button class="btn btn-local" type="submit" :disabled="auth.loading">
          Entrar con email
        </button>
      </form>

      <div class="demo-box">
        <strong>Admin de prueba</strong>
        <p>Email: admin@resena.local</p>
        <p>Contrasena: Admin12345!</p>
      </div>

      <div class="divider">o continua con</div>

      <button class="btn btn-google" type="button" @click="auth.loginWithGoogle()">
        Entrar con Google
      </button>

      <button class="btn btn-facebook" type="button" @click="auth.loginWithFacebook()">
        Entrar con Facebook
      </button>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const email = ref('admin@resena.local');
const password = ref('Admin12345!');

async function submitLocalLogin(): Promise<void> {
  await auth.loginWithPassword(email.value, password.value);
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(140deg, #eef6ff, #fff4ea);
  padding: 1.5rem;
}

.card {
  width: min(420px, 100%);
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(11, 18, 31, 0.12);
  padding: 2rem;
  display: grid;
  gap: 0.9rem;
}

.local-form {
  display: grid;
  gap: 0.8rem;
}

label {
  display: grid;
  gap: 0.35rem;
  color: #334155;
  font-weight: 600;
}

input {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 0.8rem 0.9rem;
  font-size: 0.95rem;
}

h1 {
  margin: 0;
}

p {
  margin: 0 0 1rem;
  color: #475569;
}

.btn {
  border: 0;
  border-radius: 10px;
  padding: 0.85rem 1rem;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.btn-local {
  background: #0f172a;
}

.btn-google {
  background: #ea4335;
}

.btn-facebook {
  background: #1877f2;
}

.demo-box {
  border: 1px solid #dbeafe;
  background: #eff6ff;
  border-radius: 12px;
  padding: 0.9rem 1rem;
}

.demo-box p,
.demo-box strong {
  margin: 0;
}

.divider {
  text-align: center;
  color: #64748b;
  font-size: 0.9rem;
}
</style>
