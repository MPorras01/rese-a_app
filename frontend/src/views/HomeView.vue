<template>
  <main class="home-view">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-background">
        <div class="hero-decoration hero-decoration--1"></div>
        <div class="hero-decoration hero-decoration--2"></div>
        <div class="hero-decoration hero-decoration--3"></div>
      </div>

      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            Descubre negocios<br />
            <span class="hero-highlight">auténticos</span>
          </h1>
          <p class="hero-subtitle">
            Lee reseñas verdaderas de nuestra comunidad. Conecta con negocios locales que importan.
          </p>

          <div class="hero-actions">
            <RouterLink to="/businesses" class="btn btn--primary">
              Explorar negocios
            </RouterLink>
            <RouterLink to="/login" class="btn btn--secondary">
              Escribe una reseña
            </RouterLink>
          </div>
        </div>

        <div class="hero-visual">
          <div class="hero-card hero-card--1">
            <div class="card-star">★</div>
            <p>Café increíble</p>
          </div>
          <div class="hero-card hero-card--2">
            <div class="card-star">★★★★★</div>
            <p>Muy recomendado</p>
          </div>
          <div class="hero-card hero-card--3">
            <div class="card-star">★★★★</div>
            <p>Buena experiencia</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features">
      <div class="features-inner">
        <h2>¿Por qué ReseñaApp?</h2>

        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">📝</div>
            <h3>Reseñas auténticas</h3>
            <p>Comunidad verificada, opiniones honestas sobre negocios locales.</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">🔍</div>
            <h3>Descubre fácilmente</h3>
            <p>Filtra por categoría, ubicación y calificación para encontrar lo que buscas.</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">⭐</div>
            <h3>Sistema de ratings</h3>
            <p>Calificaciones claras con fotos y comentarios detallados de usuarios.</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon">🛡️</div>
            <h3>Seguro y moderado</h3>
            <p>Todas las reseñas son revisadas para mantener la calidad y confianza.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta">
      <div class="cta-content">
        <h2>Únete a nuestra comunidad</h2>
        <p>Comparte tus experiencias y ayuda a otros a encontrar los mejores negocios.</p>
        <RouterLink to="/login" class="btn btn--highlight">
          Comenzar a escribir reseñas
        </RouterLink>
      </div>
    </section>
  </main>
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

.home-view {
  display: flex;
  flex-direction: column;
}

/* ── Hero Section ── */
.hero {
  position: relative;
  padding: 6rem 2rem;
  background: linear-gradient(135deg, #FFFFFF 0%, var(--color-cream) 100%);
  overflow: hidden;
}

.hero-background {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-decoration {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}

.hero-decoration--1 {
  width: 400px;
  height: 400px;
  background: var(--color-gold);
  top: -100px;
  right: -100px;
}

.hero-decoration--2 {
  width: 300px;
  height: 300px;
  background: var(--color-primary);
  bottom: -50px;
  left: 10%;
}

.hero-decoration--3 {
  width: 200px;
  height: 200px;
  background: var(--color-coral);
  top: 50%;
  right: 5%;
}

.hero-content {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
  position: relative;
  z-index: 1;
}

.hero-text {
  animation: slide-up 0.8s ease-out;
}

.hero-title {
  font-family: 'Georgia', serif;
  font-size: 3rem;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 1rem;
  color: var(--color-dark);
  letter-spacing: 0.5px;
}

.hero-highlight {
  color: var(--color-primary);
  position: relative;
  text-decoration: underline;
  text-decoration-color: var(--color-gold);
  text-decoration-thickness: 3px;
  text-underline-offset: 0.5rem;
}

.hero-subtitle {
  font-size: 1.2rem;
  color: var(--color-text-light);
  line-height: 1.7;
  margin-bottom: 2rem;
  letter-spacing: 0.2px;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.btn {
  padding: 1rem 2rem;
  border-radius: 0;
  border: 2px solid var(--color-primary);
  text-decoration: none;
  font-weight: 600;
  letter-spacing: 0.4px;
  text-transform: uppercase;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  display: inline-block;
  cursor: pointer;
  text-align: center;
}

.btn--primary {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

.btn--primary:hover {
  background: var(--color-coral);
  border-color: var(--color-coral);
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(197, 105, 86, 0.3);
}

.btn--secondary {
  background: transparent;
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.btn--secondary:hover {
  background: var(--color-primary);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(197, 105, 86, 0.2);
}

.btn--highlight {
  background: var(--color-gold);
  color: var(--color-dark);
  border-color: var(--color-gold);
}

.btn--highlight:hover {
  background: var(--color-coral);
  border-color: var(--color-coral);
  color: #fff;
  transform: translateY(-2px);
}

.hero-visual {
  position: relative;
  height: 300px;
}

.hero-card {
  position: absolute;
  background: #fff;
  border: 2px solid var(--color-gold);
  padding: 1.5rem;
  border-radius: 2px;
  box-shadow: 0 8px 24px rgba(197, 105, 86, 0.1);
  animation: float 3s ease-in-out infinite;
}

.hero-card--1 {
  width: 200px;
  top: 0;
  left: 0;
  animation-delay: 0s;
}

.hero-card--2 {
  width: 220px;
  top: 60px;
  right: 0;
  animation-delay: 0.5s;
}

.hero-card--3 {
  width: 180px;
  bottom: 20px;
  left: 100px;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.card-star {
  font-size: 1.5rem;
  color: var(--color-gold);
  margin-bottom: 0.5rem;
  letter-spacing: 0.2em;
}

.hero-card p {
  margin: 0;
  color: var(--color-dark);
  font-weight: 600;
  letter-spacing: 0.3px;
}

/* ── Features Section ── */
.features {
  padding: 6rem 2rem;
  background: linear-gradient(135deg, var(--color-cream) 0%, #FFFFFF 100%);
}

.features-inner {
  max-width: 1280px;
  margin: 0 auto;
}

.features h2 {
  text-align: center;
  margin-bottom: 4rem;
  font-size: 2.5rem;
  color: var(--color-dark);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: linear-gradient(135deg, #FFFFFF 0%, var(--color-cream) 100%);
  border: 1px solid var(--color-gold);
  padding: 2rem;
  border-radius: 0;
  text-align: center;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
  border-color: var(--color-primary);
  box-shadow: 0 12px 28px rgba(197, 105, 86, 0.15);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  display: block;
}

.feature-card h3 {
  font-size: 1.3rem;
  margin-bottom: 0.75rem;
  color: var(--color-dark);
}

.feature-card p {
  color: var(--color-text-light);
  line-height: 1.6;
  margin: 0;
}

/* ── CTA Section ── */
.cta {
  padding: 5rem 2rem;
  background: var(--color-dark);
  text-align: center;
  position: relative;
  overflow: hidden;
}

.cta::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.cta-content {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.cta h2 {
  color: var(--color-gold);
  font-size: 2rem;
  margin-bottom: 1rem;
}

.cta p {
  color: var(--color-cream);
  font-size: 1.1rem;
  margin-bottom: 2rem;
  line-height: 1.6;
}

/* Animations */
@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .hero-visual {
    display: none;
  }

  .hero-title {
    font-size: 2.2rem;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 4rem 1.5rem;
  }

  .hero-title {
    font-size: 1.8rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .hero-actions {
    flex-direction: column;
  }

  .btn {
    padding: 0.85rem 1.5rem;
    font-size: 0.85rem;
  }

  .features {
    padding: 4rem 1.5rem;
  }

  .features h2 {
    font-size: 1.8rem;
    margin-bottom: 2rem;
  }

  .features-grid {
    gap: 1.5rem;
  }

  .cta {
    padding: 3rem 1.5rem;
  }

  .cta h2 {
    font-size: 1.5rem;
  }
}
</style>
