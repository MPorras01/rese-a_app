<template>
  <div class="star-rating" :class="`star-rating--${size}`" :aria-label="`Puntuación: ${displayValue} de ${max}`">
    <div class="stars-container">
      <button
        v-for="star in max"
        :key="star"
        type="button"
        class="star-btn"
        :class="{ 
          'star-btn--readonly': readonly,
          'star-btn--active': star <= displayValue,
          'star-btn--hovered': star <= hovered
        }"
        :aria-label="`${star} estrella${star > 1 ? 's' : ''}`"
        :tabindex="readonly ? -1 : 0"
        :style="{ '--star-delay': `${star * 0.05}s` }"
        @mouseenter="!readonly && (hovered = star)"
        @mouseleave="!readonly && (hovered = 0)"
        @click="!readonly && emit('update:value', star)"
        @keydown.enter.prevent="!readonly && emit('update:value', star)"
        @keydown.space.prevent="!readonly && emit('update:value', star)"
      >
        <svg
          :width="sizePx"
          :height="sizePx"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
          class="star-icon"
        >
          <path
            d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
            :fill="star <= displayValue ? 'currentColor' : 'none'"
            :stroke="'currentColor'"
            stroke-width="0.5"
          />
        </svg>
      </button>
    </div>
    <span v-if="showLabel" class="rating-label">{{ displayValue }}.0</span>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

const props = withDefaults(
  defineProps<{
    value: number;
    max?: number;
    readonly?: boolean;
    size?: 'sm' | 'md' | 'lg';
    showLabel?: boolean;
  }>(),
  {
    max: 5,
    readonly: false,
    size: 'md',
    showLabel: false,
  }
);

const emit = defineEmits<{
  (e: 'update:value', value: number): void;
}>();

const hovered = ref(0);

const displayValue = computed(() => Math.max(hovered.value, props.value));

const sizePx = computed(() => {
  const sizes = { sm: 16, md: 24, lg: 32 };
  return sizes[props.size];
});
</script>

<style scoped>
:root {
  --color-tertiary: #C56956;
  --color-accent-gold: #D4AF37;
  --color-accent-coral: #E8644A;
  --color-text-primary: #1A1410;
  --color-text-light: #7A7470;
}

.star-rating {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.stars-container {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.star-btn {
  background: none;
  border: none;
  padding: 0.25rem;
  cursor: pointer;
  line-height: 0;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
  color: #E0E0E0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.star-btn:not(.star-btn--readonly):hover,
.star-btn--hovered {
  color: var(--color-accent-gold);
  transform: scale(1.2) rotate(-5deg);
  animation: star-pulse 0.4s ease-out;
}

.star-btn--active {
  color: var(--color-tertiary);
  animation: star-pop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  animation-delay: var(--star-delay);
}

.star-btn--readonly {
  cursor: default;
  pointer-events: none;
}

.star-btn:focus-visible {
  outline: 2px solid var(--color-accent-gold);
  outline-offset: 3px;
  border-radius: 4px;
}

.star-icon {
  filter: drop-shadow(0 1px 2px rgba(26, 20, 16, 0.1));
}

.rating-label {
  font-family: 'Georgia', serif;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text-light);
  margin-left: 0.25rem;
  letter-spacing: 0.5px;
}

@keyframes star-pulse {
  0% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.3);
  }
  100% {
    filter: brightness(1);
  }
}

@keyframes star-pop {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  60% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* Responsive sizes */
.star-rating--sm .star-btn {
  padding: 0.125rem;
}

.star-rating--lg .star-btn {
  padding: 0.5rem;
}
</style>
