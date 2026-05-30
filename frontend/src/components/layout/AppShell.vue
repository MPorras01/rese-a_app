<template>
  <div class="app-shell">
    <!-- Top bar (solo en páginas que no son detalle/auth) -->
    <header v-if="showTopBar" class="top-bar">
      <div class="top-bar-inner">
        <router-link to="/" class="brand">
          <span class="brand-icon">★</span>
          <span class="brand-name">ReseñaApp</span>
        </router-link>

        <div class="top-actions">
          <router-link v-if="!auth.isAuthenticated" to="/login" class="btn-login">
            Entrar
          </router-link>
          <router-link v-else to="/profile" class="avatar-btn" :title="auth.user?.name">
            <img v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" :alt="auth.user.name" />
            <span v-else class="avatar-initials">{{ initials }}</span>
          </router-link>
        </div>
      </div>
    </header>

    <!-- Contenido principal -->
    <main class="page-content" :class="{ 'with-topbar': showTopBar, 'with-navbar': showBottomNav }">
      <slot />
    </main>

    <!-- Bottom navigation (tipo app nativa) -->
    <nav v-if="showBottomNav" class="bottom-nav">
      <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">
        <span class="nav-icon">🏠</span>
        <span class="nav-label">Inicio</span>
      </router-link>

      <router-link to="/explore" class="nav-item" :class="{ active: route.path.startsWith('/explore') }">
        <span class="nav-icon">🔍</span>
        <span class="nav-label">Explorar</span>
      </router-link>

      <router-link to="/map" class="nav-item" :class="{ active: route.path === '/map' }">
        <span class="nav-icon">🗺️</span>
        <span class="nav-label">Mapa</span>
      </router-link>

      <template v-if="auth.isAuthenticated">
        <router-link
          v-if="auth.isAdmin"
          to="/admin"
          class="nav-item"
          :class="{ active: route.path.startsWith('/admin') }"
        >
          <span class="nav-icon">🛡️</span>
          <span class="nav-label">Admin</span>
        </router-link>

        <router-link
          v-else
          to="/owner/dashboard"
          class="nav-item"
          :class="{ active: route.path.startsWith('/owner') }"
        >
          <span class="nav-icon">🏪</span>
          <span class="nav-label">Mi negocio</span>
        </router-link>

        <router-link to="/profile" class="nav-item" :class="{ active: route.path.startsWith('/profile') }">
          <span class="nav-icon">👤</span>
          <span class="nav-label">Perfil</span>
        </router-link>
      </template>

      <template v-else>
        <router-link to="/login" class="nav-item" :class="{ active: route.path === '/login' }">
          <span class="nav-icon">🔑</span>
          <span class="nav-label">Entrar</span>
        </router-link>
      </template>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const auth = useAuthStore();

// Rutas donde NO mostramos el shell (auth flow)
const noShellRoutes = ['/login', '/oauth2/callback', '/verify-phone'];
const showBottomNav = computed(() => !noShellRoutes.some((r) => route.path.startsWith(r)));
const showTopBar = computed(() => !noShellRoutes.some((r) => route.path.startsWith(r)));

const initials = computed(() => {
  const name = auth.user?.name ?? '';
  return name.split(' ').slice(0, 2).map((w) => w[0]?.toUpperCase() ?? '').join('');
});
</script>

<style scoped>
.app-shell {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

/* ── Top bar ── */
.top-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #0f172a;
  color: #fff;
  height: 56px;
  /* safe area para notch */
  padding-top: env(safe-area-inset-top);
}

.top-bar-inner {
  max-width: 640px;
  margin: 0 auto;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  text-decoration: none;
  color: #fff;
}

.brand-icon {
  font-size: 1.3rem;
  color: #fbbf24;
}

.brand-name {
  font-weight: 700;
  font-size: 1.05rem;
  letter-spacing: -0.02em;
}

.btn-login {
  background: #fbbf24;
  color: #0f172a;
  border-radius: 999px;
  padding: 0.35rem 0.9rem;
  font-weight: 700;
  font-size: 0.85rem;
  text-decoration: none;
}

.avatar-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1e3a8a;
  text-decoration: none;
}

.avatar-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-initials {
  color: #fff;
  font-weight: 700;
  font-size: 0.8rem;
}

/* ── Page content ── */
.page-content {
  flex: 1;
  width: 100%;
  max-width: 640px;
  margin: 0 auto;
}

.page-content.with-topbar {
  padding-top: calc(56px + env(safe-area-inset-top));
}

.page-content.with-navbar {
  padding-bottom: calc(64px + env(safe-area-inset-bottom));
}

/* ── Bottom nav ── */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #fff;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 64px;
  padding-bottom: env(safe-area-inset-bottom);
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.15rem;
  text-decoration: none;
  color: #94a3b8;
  padding: 0.4rem 0;
  transition: color 0.15s;
  -webkit-tap-highlight-color: transparent;
}

.nav-item.active {
  color: #0f172a;
}

.nav-icon {
  font-size: 1.3rem;
  line-height: 1;
}

.nav-label {
  font-size: 0.65rem;
  font-weight: 600;
  letter-spacing: 0.01em;
}
</style>
