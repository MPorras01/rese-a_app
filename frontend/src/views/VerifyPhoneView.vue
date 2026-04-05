<template>
  <main class="verify-page">
    <section class="card">
      <h1>Verificar telefono</h1>

      <div v-if="step === 1" class="step-content">
        <label for="phone">Telefono (+57XXXXXXXXXX)</label>
        <input
          id="phone"
          v-model="phone"
          type="tel"
          placeholder="+573001112233"
          :disabled="authStore.loading"
        />
        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
        <button type="button" :disabled="authStore.loading" @click="sendCode">
          Enviar codigo
        </button>
      </div>

      <div v-else class="step-content">
        <p>Ingresa el codigo de 6 digitos enviado a {{ phone }}.</p>

        <div class="otp-grid">
          <input
            v-for="(_, idx) in digits"
            :key="idx"
            :ref="(el) => setDigitRef(el, idx)"
            v-model="digits[idx]"
            type="text"
            inputmode="numeric"
            maxlength="1"
            autocomplete="one-time-code"
            :disabled="authStore.loading || countdown === 0"
            @input="onDigitInput(idx, $event)"
            @keydown="onDigitKeydown(idx, $event)"
          />
        </div>

        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

        <p v-if="countdown > 0" class="countdown">{{ countdownLabel }}</p>
        <button v-else type="button" :disabled="authStore.loading" @click="resetToStepOne">
          Reenviar codigo
        </button>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, nextTick, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const PHONE_REGEX = /^\+57\d{10}$/;

const router = useRouter();
const authStore = useAuthStore();

const step = ref(1);
const phone = ref('');
const errorMessage = ref('');
const digits = ref<string[]>(Array.from({ length: 6 }, () => ''));
const inputRefs = ref<Array<HTMLInputElement | null>>(Array.from({ length: 6 }, () => null));

const countdown = ref(300);
let countdownHandle: number | null = null;

const countdownLabel = computed(() => {
  const minutes = String(Math.floor(countdown.value / 60)).padStart(2, '0');
  const seconds = String(countdown.value % 60).padStart(2, '0');
  return `${minutes}:${seconds}`;
});

function setDigitRef(el: Element | object | null, index: number): void {
  inputRefs.value[index] = el instanceof HTMLInputElement ? el : null;
}

async function sendCode(): Promise<void> {
  errorMessage.value = '';
  if (!PHONE_REGEX.test(phone.value.trim())) {
    errorMessage.value = 'Telefono invalido. Usa formato +57XXXXXXXXXX';
    return;
  }

  try {
    await authStore.requestOtp(phone.value.trim());
    step.value = 2;
    digits.value = Array.from({ length: 6 }, () => '');
    startCountdown();
    await nextTick();
    inputRefs.value[0]?.focus();
  } catch {
    errorMessage.value = 'No se pudo enviar el codigo. Intenta nuevamente.';
  }
}

function onDigitInput(index: number, event: Event): void {
  const target = event.target as HTMLInputElement;
  const clean = target.value.replace(/\D/g, '').slice(0, 1);
  digits.value[index] = clean;
  target.value = clean;

  if (clean && index < digits.value.length - 1) {
    inputRefs.value[index + 1]?.focus();
  }

  if (digits.value.every((d) => d !== '')) {
    void submitOtp();
  }
}

function onDigitKeydown(index: number, event: KeyboardEvent): void {
  if (event.key !== 'Backspace') {
    return;
  }

  const hasValue = digits.value[index] !== '';
  if (hasValue) {
    digits.value[index] = '';
    return;
  }

  if (index > 0) {
    digits.value[index - 1] = '';
    inputRefs.value[index - 1]?.focus();
  }
}

async function submitOtp(): Promise<void> {
  if (authStore.loading || countdown.value === 0) {
    return;
  }

  errorMessage.value = '';

  try {
    await authStore.verifyOtp(digits.value.join(''));
    stopCountdown();
    await router.push('/');
  } catch (error: unknown) {
    const status = (error as { response?: { status?: number } })?.response?.status;

    if (status === 410) {
      errorMessage.value = 'Codigo expirado, solicita uno nuevo';
      resetToStepOne();
      return;
    }

    if (status === 400) {
      errorMessage.value = 'Codigo incorrecto';
      digits.value = Array.from({ length: 6 }, () => '');
      await nextTick();
      inputRefs.value[0]?.focus();
      return;
    }

    errorMessage.value = 'No se pudo verificar el codigo.';
  }
}

function startCountdown(): void {
  stopCountdown();
  countdown.value = 300;

  countdownHandle = window.setInterval(() => {
    if (step.value !== 2) {
      stopCountdown();
      return;
    }

    if (countdown.value > 0) {
      countdown.value -= 1;
      return;
    }

    stopCountdown();
  }, 1000);
}

function stopCountdown(): void {
  if (countdownHandle !== null) {
    clearInterval(countdownHandle);
    countdownHandle = null;
  }
}

function resetToStepOne(): void {
  stopCountdown();
  step.value = 1;
  digits.value = Array.from({ length: 6 }, () => '');
}

onUnmounted(() => {
  stopCountdown();
});
</script>

<style scoped>
.verify-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #f5f7ff, #fff5ea);
  padding: 1.5rem;
}

.card {
  width: min(520px, 100%);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 18px 40px rgba(2, 8, 23, 0.1);
  padding: 2rem;
}

h1 {
  margin: 0 0 1rem;
}

.step-content {
  display: grid;
  gap: 0.9rem;
}

label {
  font-weight: 600;
}

input[type='tel'] {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 0.75rem 0.9rem;
  font-size: 1rem;
}

.otp-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 0.55rem;
}

.otp-grid input {
  text-align: center;
  font-size: 1.25rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  height: 44px;
}

button {
  border: 0;
  border-radius: 10px;
  background: #1d4ed8;
  color: #fff;
  font-weight: 600;
  padding: 0.75rem 1rem;
  cursor: pointer;
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error {
  margin: 0;
  color: #b91c1c;
}

.countdown {
  margin: 0;
  color: #334155;
  font-weight: 600;
}
</style>
