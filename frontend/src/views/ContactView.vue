<template>
  <div class="contact-page">
    <section class="page-header">
      <h1>Contacto</h1>
      <p>Estamos aquí para ayudarte</p>
    </section>

    <!-- Canales rápidos -->
    <section class="channels">
      <a v-for="ch in channels" :key="ch.label" :href="ch.href" class="channel-card">
        <span class="ch-icon">{{ ch.icon }}</span>
        <div>
          <strong>{{ ch.label }}</strong>
          <span>{{ ch.value }}</span>
        </div>
        <span class="ch-arrow">›</span>
      </a>
    </section>

    <!-- Formulario -->
    <section class="section">
      <h2>Envíanos un mensaje</h2>

      <form class="form" @submit.prevent="send">
        <label class="field">
          <span>Nombre</span>
          <input v-model="form.name" type="text" placeholder="Tu nombre" required maxlength="100" />
        </label>

        <label class="field">
          <span>Email</span>
          <input v-model="form.email" type="email" placeholder="tu@email.com" required />
        </label>

        <label class="field">
          <span>Asunto</span>
          <select v-model="form.subject" required>
            <option value="">Selecciona un asunto</option>
            <option value="soporte">Soporte técnico</option>
            <option value="negocio">Registrar mi negocio</option>
            <option value="reporte">Reportar contenido</option>
            <option value="otro">Otro</option>
          </select>
        </label>

        <label class="field">
          <span>Mensaje</span>
          <textarea
            v-model="form.message"
            rows="4"
            placeholder="Cuéntanos en qué podemos ayudarte..."
            required
            maxlength="1000"
          />
          <small class="char-count">{{ form.message.length }} / 1000</small>
        </label>

        <p v-if="sent" class="success">✓ Mensaje enviado. Te responderemos pronto.</p>
        <p v-if="error" class="error">{{ error }}</p>

        <button type="submit" :disabled="sending" class="submit-btn">
          {{ sending ? 'Enviando...' : 'Enviar mensaje' }}
        </button>
      </form>
    </section>

    <!-- FAQ -->
    <section class="section">
      <h2>Preguntas frecuentes</h2>
      <div class="faq-list">
        <details v-for="faq in faqs" :key="faq.q" class="faq-item">
          <summary>{{ faq.q }}</summary>
          <p>{{ faq.a }}</p>
        </details>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';

const sending = ref(false);
const sent = ref(false);
const error = ref('');

const form = reactive({ name: '', email: '', subject: '', message: '' });

const channels = [
  { icon: '📧', label: 'Email', value: 'hola@reseniaapp.com', href: 'mailto:hola@reseniaapp.com' },
  { icon: '💬', label: 'WhatsApp', value: '+57 300 000 0000', href: 'https://wa.me/573000000000' },
  { icon: '📍', label: 'Ubicación', value: 'Bogotá, Colombia', href: 'https://maps.google.com' },
];

const faqs = [
  { q: '¿Cómo registro mi negocio?', a: 'Crea una cuenta, ve a "Mi negocio" y completa el formulario. Un administrador lo revisará en 24-48 horas.' },
  { q: '¿Las reseñas son verificadas?', a: 'Sí, solo usuarios con cuenta activa y teléfono verificado pueden escribir reseñas.' },
  { q: '¿Puedo eliminar una reseña?', a: 'Puedes eliminar tus propias reseñas desde "Mis reseñas" en tu perfil.' },
  { q: '¿El servicio es gratuito?', a: 'Sí, registrar y gestionar tu negocio es completamente gratuito.' },
];

async function send(): Promise<void> {
  sending.value = true;
  error.value = '';
  // Simulación — en producción conectar con un endpoint real
  await new Promise((r) => setTimeout(r, 1000));
  sent.value = true;
  sending.value = false;
  form.name = '';
  form.email = '';
  form.subject = '';
  form.message = '';
}
</script>

<style scoped>
.contact-page { display: grid; gap: 0; }

.page-header {
  background: linear-gradient(145deg, #0f172a, #1e3a8a);
  color: #fff;
  padding: 2rem 1.25rem 1.5rem;
  display: grid;
  gap: 0.3rem;
}

.page-header h1 { margin: 0; font-size: 1.5rem; font-weight: 800; }
.page-header p { margin: 0; color: #94a3b8; font-size: 0.9rem; }

.channels {
  display: grid;
  gap: 0;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
}

.channel-card {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 1rem 1.25rem;
  text-decoration: none;
  color: #0f172a;
  border-bottom: 1px solid #f1f5f9;
  -webkit-tap-highlight-color: transparent;
}

.channel-card:last-child { border-bottom: none; }
.channel-card:active { background: #f8fafc; }

.ch-icon { font-size: 1.4rem; }

.channel-card div {
  flex: 1;
  display: grid;
  gap: 0.1rem;
}

.channel-card strong { font-size: 0.9rem; }
.channel-card span { font-size: 0.8rem; color: #64748b; }
.ch-arrow { color: #94a3b8; font-size: 1.2rem; }

.section {
  padding: 1.5rem 1rem;
  display: grid;
  gap: 0.85rem;
  border-bottom: 1px solid #f1f5f9;
}

.section h2 { margin: 0; font-size: 1.05rem; font-weight: 700; }

.form { display: grid; gap: 0.85rem; }

.field {
  display: grid;
  gap: 0.35rem;
  font-size: 0.88rem;
  font-weight: 600;
  color: #334155;
}

input, select, textarea {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 0.7rem 0.85rem;
  font-size: 0.95rem;
  background: #fff;
  width: 100%;
  resize: vertical;
}

.char-count { text-align: right; color: #94a3b8; font-size: 0.75rem; font-weight: 400; }

.submit-btn {
  border: none;
  border-radius: 999px;
  background: #0f172a;
  color: #fff;
  font-weight: 700;
  padding: 0.85rem;
  cursor: pointer;
  font-size: 0.95rem;
}

.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.success {
  background: #dcfce7;
  color: #166534;
  border-radius: 10px;
  padding: 0.65rem 0.9rem;
  margin: 0;
  font-size: 0.88rem;
}

.error {
  background: #fee2e2;
  color: #991b1b;
  border-radius: 10px;
  padding: 0.65rem 0.9rem;
  margin: 0;
  font-size: 0.88rem;
}

.faq-list { display: grid; gap: 0.5rem; }

.faq-item {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
}

.faq-item summary {
  padding: 0.85rem 1rem;
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  list-style: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
  -webkit-tap-highlight-color: transparent;
}

.faq-item summary::after { content: '+'; font-size: 1.1rem; color: #64748b; }
.faq-item[open] summary::after { content: '−'; }

.faq-item p {
  margin: 0;
  padding: 0 1rem 0.85rem;
  font-size: 0.85rem;
  color: #475569;
  line-height: 1.5;
}
</style>
