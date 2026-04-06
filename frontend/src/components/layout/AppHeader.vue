<template>
  <header class="app-header">
    <div class="header-container">
      <div class="logo-section">
        <RouterLink to="/" class="logo-link">
          <div class="logo-mark">✦</div>
          <div class="logo-text">
            <h1>ReseñaApp</h1>
            <p class="tagline">Reseñas auténticas, comunidad verdadera</p>
          </div>
        </RouterLink>
      </div>

      <nav class="nav-menu" :class="{ 'nav-menu--active': mobileMenuOpen }">
        <RouterLink to="/" class="nav-link" active-class="nav-link--active">
          Descubrir
        </RouterLink>
        <RouterLink to="/businesses" class="nav-link" active-class="nav-link--active">
          Negocios
        </RouterLink>

        <div v-if="!authStore.isAuthenticated" class="auth-links">
          <RouterLink to="/login" class="nav-link nav-link--highlight">
            Ingresar
          </RouterLink>
        </div>

        <div v-else class="auth-links">
          <RouterLink to="/verify-phone" class="nav-link">
            Mi cuenta
          </RouterLink>
          <button class="nav-link nav-link--logout" @click="handleLogout">
            Salir
          </button>
        </div>
      </nav>

      <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen">
        <span></span>
        <span></span>
        <span></span>
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const mobileMenuOpen = ref(false);

function handleLogout(): void {
  authStore.logout();
  void router.push('/');
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

.app-header {
  background: linear-gradient(90deg, #FFFFFF 0%, var(--color-cream) 100%);
  border-bottom: 2px solid var(--color-gold);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(197, 105, 86, 0.08);
}

.header-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-section {
  display: flex;
  align-items: center;
}

.logo-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  text-decoration: none;
  color: var(--color-dark);
  transition: all 0.3s ease;
  border: none;
}

.logo-mark {
  font-size: 1.8rem;
  color: var(--color-gold);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.logo-text h1 {
  font-family: 'Georgia', serif;
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: 1px;
}

.tagline {
  font-size: 0.65rem;
  color: var(--color-text-light);
  margin: 0;
  letter-spacing: 0.5px;
  font-style: italic;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 2rem;
  list-style: none;
}

.nav-link {
  color: var(--color-dark);
  text-decoration: none;
  font-weight: 500;
  letter-spacing: 0.3px;
  padding: 0.5rem 1rem;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.9rem;
}

.nav-link:hover,
.nav-link--active {
  color: var(--color-gold);
  border-bottom-color: var(--color-gold);
}

.nav-link--highlight {
  background: var(--color-primary);
  color: #fff;
  border-radius: 0;
  padding: 0.6rem 1.4rem;
  border: none;
  font-weight: 600;
}

.nav-link--highlight:hover {
  background: var(--color-coral);
  border: none;
  color: #fff;
}

.nav-link--logout {
  color: var(--color-coral);
}

.nav-link--logout:hover {
  color: var(--color-primary);
}

.auth-links {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.mobile-menu-btn {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
}

.mobile-menu-btn span {
  width: 24px;
  height: 2px;
  background: var(--color-dark);
  transition: all 0.3s ease;
  border-radius: 1px;
}

@media (max-width: 768px) {
  .mobile-menu-btn {
    display: flex;
  }

  .nav-menu {
    position: fixed;
    top: 70px;
    left: 0;
    right: 0;
    bottom: 0;
    flex-direction: column;
    gap: 0;
    background: var(--color-cream);
    padding: 2rem;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    border-bottom: 2px solid var(--color-gold);
  }

  .nav-menu--active {
    transform: translateX(0);
  }

  .nav-link {
    width: 100%;
    text-align: left;
    padding: 1rem;
    border-left: 3px solid transparent;
    border-bottom: none;
  }

  .nav-link:hover,
  .nav-link--active {
    border-left-color: var(--color-gold);
    border-bottom: none;
  }

  .header-container {
    padding: 1rem;
  }

  .logo-text h1 {
    font-size: 1.1rem;
  }

  .tagline {
    font-size: 0.6rem;
  }
}
</style>
