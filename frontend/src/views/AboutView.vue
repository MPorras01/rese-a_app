<template>
  <div class="about-page">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-icon">★</div>
      <h1>ReseñaApp</h1>
      <p>La plataforma de reseñas para negocios locales</p>
    </section>

    <!-- Misión -->
    <section class="section">
      <h2>Nuestra misión</h2>
      <p>
        Conectamos a las personas con los mejores negocios de su ciudad a través de reseñas
        auténticas y transparentes. Creemos que cada experiencia cuenta y que compartirla
        ayuda a toda la comunidad.
      </p>
    </section>

    <!-- Valores -->
    <section class="section">
      <h2>Nuestros valores</h2>
      <div class="values-grid">
        <div v-for="v in values" :key="v.title" class="value-card">
          <span class="value-icon">{{ v.icon }}</span>
          <strong>{{ v.title }}</strong>
          <p>{{ v.desc }}</p>
        </div>
      </div>
    </section>

    <!-- Cómo funciona -->
    <section class="section">
      <h2>¿Cómo funciona?</h2>
      <ol class="steps">
        <li v-for="(step, i) in steps" :key="i" class="step">
          <span class="step-num">{{ i + 1 }}</span>
          <div>
            <strong>{{ step.title }}</strong>
            <p>{{ step.desc }}</p>
          </div>
        </li>
      </ol>
    </section>

    <!-- Equipo -->
    <section class="section">
      <h2>El equipo</h2>
      <div class="team-grid">
        <div v-for="member in team" :key="member.name" class="team-card">
          <div class="team-avatar">{{ member.initials }}</div>
          <strong>{{ member.name }}</strong>
          <span>{{ member.role }}</span>
        </div>
      </div>
    </section>

    <!-- Contacto rápido -->
    <section class="section cta-section">
      <div class="cta-card">
        <h3>¿Tienes preguntas?</h3>
        <p>Escríbenos y te respondemos en menos de 24 horas.</p>
        <router-link to="/contact" class="cta-btn">Contactar →</router-link>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const values = [
  { icon: '🤝', title: 'Transparencia', desc: 'Reseñas reales de usuarios verificados.' },
  { icon: '🌱', title: 'Comunidad', desc: 'Apoyamos el comercio local y los emprendedores.' },
  { icon: '🔒', title: 'Confianza', desc: 'Tus datos siempre seguros y privados.' },
  { icon: '⚡', title: 'Agilidad', desc: 'Encuentra lo que buscas en segundos.' },
];

const steps = [
  { title: 'Explora negocios', desc: 'Busca por categoría, ciudad o nombre.' },
  { title: 'Lee las reseñas', desc: 'Opiniones reales de clientes como tú.' },
  { title: 'Comparte tu experiencia', desc: 'Escribe una reseña y ayuda a otros.' },
  { title: 'Registra tu negocio', desc: 'Gratis, rápido y sin complicaciones.' },
];

const team = [
  { name: 'Ana García', role: 'CEO & Fundadora', initials: 'AG' },
  { name: 'Carlos López', role: 'CTO', initials: 'CL' },
  { name: 'María Torres', role: 'Diseño UX', initials: 'MT' },
];
</script>

<style scoped>
.about-page {
  display: grid;
  gap: 0;
}

.hero {
  background: linear-gradient(145deg, #0f172a, #1e3a8a);
  color: #fff;
  padding: 2.5rem 1.25rem;
  text-align: center;
  display: grid;
  gap: 0.5rem;
}

.hero-icon {
  font-size: 3rem;
  color: #fbbf24;
}

.hero h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 800;
}

.hero p {
  margin: 0;
  color: #94a3b8;
  font-size: 0.95rem;
}

.section {
  padding: 1.5rem 1rem;
  display: grid;
  gap: 0.85rem;
  border-bottom: 1px solid #f1f5f9;
}

.section h2 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
}

.section p {
  margin: 0;
  color: #475569;
  font-size: 0.92rem;
  line-height: 1.6;
}

.values-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.value-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 1rem;
  display: grid;
  gap: 0.3rem;
}

.value-icon { font-size: 1.5rem; }

.value-card strong {
  font-size: 0.9rem;
}

.value-card p {
  font-size: 0.8rem;
  color: #64748b;
}

.steps {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 0.85rem;
}

.step {
  display: flex;
  gap: 0.85rem;
  align-items: flex-start;
}

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #0f172a;
  color: #fff;
  font-weight: 700;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.step strong {
  display: block;
  font-size: 0.92rem;
  margin-bottom: 0.15rem;
}

.step p {
  font-size: 0.82rem;
  color: #64748b;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}

.team-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 1rem 0.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.35rem;
  text-align: center;
}

.team-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #0f172a;
  color: #fff;
  font-weight: 700;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.team-card strong {
  font-size: 0.82rem;
}

.team-card span {
  font-size: 0.72rem;
  color: #64748b;
}

.cta-section {
  border-bottom: none;
}

.cta-card {
  background: linear-gradient(135deg, #1e3a8a, #0f172a);
  color: #fff;
  border-radius: 18px;
  padding: 1.5rem;
  display: grid;
  gap: 0.6rem;
}

.cta-card h3 { margin: 0; font-size: 1.05rem; }
.cta-card p { margin: 0; color: #94a3b8; font-size: 0.88rem; }

.cta-btn {
  display: inline-block;
  background: #fbbf24;
  color: #0f172a;
  font-weight: 700;
  border-radius: 999px;
  padding: 0.65rem 1.2rem;
  text-decoration: none;
  font-size: 0.88rem;
  width: fit-content;
}
</style>
