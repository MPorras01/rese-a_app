<template>
  <AppShell>
    <RouterView />
  </AppShell>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import AppShell from '@/components/layout/AppShell.vue';

onMounted(() => {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js').catch(() => {/* silencioso */});
  }
});
</script>

<style>
/* ── Reset global ── */
*, *::before, *::after { box-sizing: border-box; }

html {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 16px;
  -webkit-text-size-adjust: 100%;
  scroll-behavior: smooth;
}

body {
  margin: 0;
  background: #f8fafc;
  color: #0f172a;
  overscroll-behavior-y: none;
}

a { color: inherit; }

button, input, select, textarea {
  font-family: inherit;
  font-size: inherit;
}

img { max-width: 100%; display: block; }
</style>
