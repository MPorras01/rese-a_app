import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import { useAuthStore } from '@/stores/auth';
import 'primeicons/primeicons.css';

const pinia = createPinia();
const app = createApp(App);

app.use(pinia);
app.use(router);

const authStore = useAuthStore(pinia);
void authStore.initAuth();

app.mount('#app');
