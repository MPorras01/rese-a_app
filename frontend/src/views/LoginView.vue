<template>
  <main class="login-page">
    <section class="card">
      <h1>Iniciar sesion</h1>
      <p>Elige un proveedor para continuar.</p>

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
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
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

.btn-google {
  background: #ea4335;
}

.btn-facebook {
  background: #1877f2;
}
</style>
