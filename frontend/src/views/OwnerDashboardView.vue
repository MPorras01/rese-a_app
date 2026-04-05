<template>
  <main class="owner-page">
    <h1>Panel de propietario</h1>

    <section v-if="loading" class="panel">
      <p>Cargando...</p>
    </section>

    <section v-else-if="business === null" class="panel">
      <h2>Registrar negocio</h2>
      <form class="form" @submit.prevent="registerBusiness">
        <input v-model="form.name" type="text" placeholder="Nombre" required />
        <textarea v-model="form.description" placeholder="Descripcion" rows="3" />
        <select v-model="form.category" required>
          <option value="">Selecciona categoria</option>
          <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
        </select>
        <input v-model="form.address" type="text" placeholder="Direccion" />
        <input v-model="form.city" type="text" placeholder="Ciudad" />
        <input v-model="form.phone" type="text" placeholder="Telefono" />
        <input v-model="form.email" type="email" placeholder="Email" />
        <input v-model="form.website" type="url" placeholder="Website" />
        <button type="submit" :disabled="saving">Crear negocio</button>
      </form>
    </section>

    <section v-else-if="business.status === 'PENDING'" class="panel banner pending">
      <strong>Tu negocio esta en revision</strong>
      <p>Te notificaremos cuando el proceso termine.</p>
    </section>

    <section v-else-if="business.status === 'REJECTED'" class="panel">
      <div class="banner rejected">
        <strong>Tu negocio fue rechazado</strong>
        <p>{{ business.rejectionReason || 'Sin detalle de rechazo.' }}</p>
      </div>

      <h2>Corrige y reenvia tu negocio</h2>
      <form class="form" @submit.prevent="updateBusinessProfile">
        <input v-model="form.name" type="text" placeholder="Nombre" required />
        <textarea v-model="form.description" placeholder="Descripcion" rows="3" />
        <select v-model="form.category" required>
          <option value="">Selecciona categoria</option>
          <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
        </select>
        <input v-model="form.address" type="text" placeholder="Direccion" />
        <input v-model="form.city" type="text" placeholder="Ciudad" />
        <input v-model="form.phone" type="text" placeholder="Telefono" />
        <input v-model="form.email" type="email" placeholder="Email" />
        <input v-model="form.website" type="url" placeholder="Website" />
        <button type="submit" :disabled="saving">Actualizar negocio</button>
      </form>
    </section>

    <section v-else class="dashboard">
      <div class="stats-grid">
        <article class="stat-card">
          <h3>Total reseñas</h3>
          <p>{{ reviewStats.totalReviews }}</p>
        </article>
        <article class="stat-card">
          <h3>Rating promedio</h3>
          <p>{{ reviewStats.avgRating.toFixed(1) }}</p>
        </article>
        <article class="stat-card">
          <h3>Distribucion 1-5</h3>
          <p>{{ distributionText }}</p>
        </article>
      </div>

      <section class="panel">
        <h2>Ultimas 5 reseñas</h2>
        <ReviewCard v-for="review in latestReviews" :key="review.id" :review="review" />
        <p v-if="latestReviews.length === 0">No hay reseñas aún.</p>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>Productos</h2>
          <button type="button" @click="showAddProduct = !showAddProduct">
            Agregar producto
          </button>
        </div>

        <form v-if="showAddProduct" class="form-inline" @submit.prevent="addProduct">
          <input v-model="newProduct.name" type="text" placeholder="Nombre" required />
          <input v-model="newProduct.description" type="text" placeholder="Descripcion" />
          <input v-model="newProduct.priceRange" type="text" placeholder="Precio" />
          <button type="submit">Guardar</button>
        </form>

        <article v-for="product in products" :key="product.id" class="product-row">
          <div>
            <strong>{{ product.name }}</strong>
            <p>{{ product.active ? 'Activo' : 'Inactivo' }}</p>
          </div>
          <button type="button" @click="toggleProduct(product)">
            {{ product.active ? 'Desactivar' : 'Activar' }}
          </button>
        </article>
      </section>

      <section class="panel">
        <button type="button" @click="showEditBusiness = !showEditBusiness">
          Editar perfil del negocio
        </button>

        <form v-if="showEditBusiness" class="form" @submit.prevent="updateBusinessProfile">
          <input v-model="form.name" type="text" placeholder="Nombre" required />
          <textarea v-model="form.description" placeholder="Descripcion" rows="3" />
          <select v-model="form.category" required>
            <option value="">Selecciona categoria</option>
            <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
          </select>
          <input v-model="form.address" type="text" placeholder="Direccion" />
          <input v-model="form.city" type="text" placeholder="Ciudad" />
          <input v-model="form.phone" type="text" placeholder="Telefono" />
          <input v-model="form.email" type="email" placeholder="Email" />
          <input v-model="form.website" type="url" placeholder="Website" />
          <button type="submit" :disabled="saving">Guardar cambios</button>
        </form>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import apiClient from '@/api/axios';
import ReviewCard from '@/components/ui/ReviewCard.vue';
import type { BusinessDto, ProductDto, ReviewDto, PageResponse } from '@/types/business';

interface ReviewStatsResponse {
  totalReviews: number;
  avgRating: number;
  distribution: Record<string, number>;
}

const categories = ['Restaurante', 'Tienda', 'Servicio', 'Salud', 'Belleza', 'Educacion', 'Otro'];

const loading = ref(true);
const saving = ref(false);
const business = ref<BusinessDto | null>(null);
const latestReviews = ref<ReviewDto[]>([]);
const products = ref<ProductDto[]>([]);
const reviewStats = ref<ReviewStatsResponse>({
  totalReviews: 0,
  avgRating: 0,
  distribution: { '1': 0, '2': 0, '3': 0, '4': 0, '5': 0 }
});

const showAddProduct = ref(false);
const showEditBusiness = ref(false);

const form = reactive({
  name: '',
  description: '',
  category: '',
  address: '',
  city: '',
  phone: '',
  email: '',
  website: ''
});

const newProduct = reactive({
  name: '',
  description: '',
  priceRange: ''
});

const distributionText = computed(() => {
  const values = reviewStats.value.distribution;
  return [1, 2, 3, 4, 5].map((star) => `${star}:${values[String(star)] ?? 0}`).join(' | ');
});

onMounted(async () => {
  await fetchMyBusiness();
});

async function fetchMyBusiness(): Promise<void> {
  loading.value = true;
  try {
    const response = await apiClient.get<BusinessDto | null>('/api/businesses/me');
    business.value = response.data;

    if (!business.value) {
      return;
    }

    fillForm(business.value);

    if (business.value.status === 'APPROVED') {
      await Promise.all([loadReviewStats(), loadLatestReviews(), loadProducts()]);
    }
  } finally {
    loading.value = false;
  }
}

function fillForm(value: BusinessDto): void {
  form.name = value.name || '';
  form.description = value.description || '';
  form.category = value.category || '';
  form.address = value.address || '';
  form.city = value.city || '';
  form.phone = value.phone || '';
  form.email = value.email || '';
  form.website = value.website || '';
}

function buildBusinessPayload() {
  return {
    name: form.name,
    description: form.description || null,
    category: form.category,
    address: form.address || null,
    city: form.city || null,
    phone: form.phone || null,
    email: form.email || null,
    website: form.website || null
  };
}

async function registerBusiness(): Promise<void> {
  saving.value = true;
  try {
    await apiClient.post<BusinessDto>('/api/businesses', buildBusinessPayload());
    await fetchMyBusiness();
  } finally {
    saving.value = false;
  }
}

async function updateBusinessProfile(): Promise<void> {
  if (!business.value) {
    return;
  }

  saving.value = true;
  try {
    await apiClient.put<BusinessDto>(`/api/businesses/${business.value.id}`, buildBusinessPayload());
    showEditBusiness.value = false;
    await fetchMyBusiness();
  } finally {
    saving.value = false;
  }
}

async function loadReviewStats(): Promise<void> {
  if (!business.value) {
    return;
  }

  const response = await apiClient.get<ReviewStatsResponse>('/api/reviews/stats', {
    params: {
      businessId: business.value.id
    }
  });
  reviewStats.value = response.data;
}

async function loadLatestReviews(): Promise<void> {
  if (!business.value) {
    return;
  }

  const response = await apiClient.get<PageResponse<ReviewDto>>('/api/reviews', {
    params: {
      businessId: business.value.id,
      page: 0,
      size: 5
    }
  });

  latestReviews.value = response.data.content;
}

async function loadProducts(): Promise<void> {
  if (!business.value) {
    return;
  }

  const response = await apiClient.get<ProductDto[]>('/api/products', {
    params: {
      businessId: business.value.id
    }
  });

  products.value = response.data;
}

async function toggleProduct(product: ProductDto): Promise<void> {
  if (!product.id) {
    return;
  }

  await apiClient.put(`/api/products/${product.id}`, {
    active: !product.active
  });

  await loadProducts();
}

async function addProduct(): Promise<void> {
  if (!business.value) {
    return;
  }

  await apiClient.post('/api/products', {
    businessId: business.value.id,
    name: newProduct.name,
    description: newProduct.description || null,
    priceRange: newProduct.priceRange || null
  });

  newProduct.name = '';
  newProduct.description = '';
  newProduct.priceRange = '';
  showAddProduct.value = false;

  await loadProducts();
}
</script>

<style scoped>
.owner-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 1rem;
  display: grid;
  gap: 1rem;
}

.panel {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 1rem;
  background: #fff;
  display: grid;
  gap: 0.75rem;
}

.banner {
  border-radius: 10px;
  padding: 0.75rem;
}

.pending {
  background: #fef3c7;
  color: #92400e;
}

.rejected {
  background: #fee2e2;
  color: #991b1b;
}

.form,
.form-inline {
  display: grid;
  gap: 0.65rem;
}

input,
textarea,
select {
  border: 1px solid #cbd5e1;
  border-radius: 9px;
  padding: 0.65rem 0.8rem;
}

button {
  width: fit-content;
  border: 0;
  border-radius: 10px;
  background: #0f172a;
  color: #fff;
  padding: 0.55rem 0.9rem;
  cursor: pointer;
}

.dashboard {
  display: grid;
  gap: 1rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.75rem;
}

.stat-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  padding: 0.85rem;
}

.stat-card h3,
.stat-card p {
  margin: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}

.product-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 0.7rem;
}

.product-row p,
.product-row strong {
  margin: 0;
}
</style>
