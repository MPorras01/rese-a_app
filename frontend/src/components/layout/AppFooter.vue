<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-section">
        <h4>ReseñaApp</h4>
        <p>Plataforma de reseñas auténticas para negocios locales.</p>
      </div>

      <div class="footer-section">
        <h4>Enlaces</h4>
        <ul>
          <li><RouterLink to="/businesses">Negocios</RouterLink></li>
          <li><RouterLink to="/">Inicio</RouterLink></li>
          <li><a href="#">Contacto</a></li>
        </ul>
      </div>

      <div class="footer-section">
        <h4>Legal</h4>
        <ul>
          <li><a href="#">Términos de servicio</a></li>
          <li><a href="#">Privacidad</a></li>
          <li><a href="#">Cookies</a></li>
        </ul>
      </div>
    </div>

    <div class="footer-bottom">
      <p>&copy; 2026 ReseñaApp. Todos los derechos reservados.</p>
      <div class="footer-social">
        <a href="#" aria-label="Twitter">𝕏</a>
        <a href="#" aria-label="Instagram">📷</a>
        <a href="#" aria-label="Email">✉</a>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
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

.app-footer {
  background: var(--color-dark);
  color: var(--color-cream);
  padding: 3rem 2rem 1.5rem;
  border-top: 2px solid var(--color-gold);
  margin-top: auto;
}

.footer-content {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.footer-section h4 {
  font-family: 'Georgia', serif;
  font-size: 1.1rem;
  margin-bottom: 1rem;
  color: var(--color-gold);
  letter-spacing: 0.5px;
}

.footer-section p {
  color: var(--color-text-light);
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.footer-section a {
  color: var(--color-text-light);
  text-decoration: none;
  border: none;
  transition: color 0.2s ease;
  font-size: 0.9rem;
}

.footer-section a:hover {
  color: var(--color-gold);
}

.footer-bottom {
  max-width: 1280px;
  margin: 0 auto;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(212, 175, 55, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.footer-bottom p {
  color: var(--color-text-light);
  font-size: 0.85rem;
  margin: 0;
}

.footer-social {
  display: flex;
  gap: 1rem;
}

.footer-social a {
  color: var(--color-gold);
  font-size: 1.2rem;
  transition: all 0.2s ease;
  border: none;
  text-decoration: none;
}

.footer-social a:hover {
  color: var(--color-coral);
  transform: scale(1.1);
}

@media (max-width: 768px) {
  .footer-content {
    gap: 1.5rem;
  }

  .footer-bottom {
    justify-content: center;
    flex-direction: column;
  }
}
</style>
