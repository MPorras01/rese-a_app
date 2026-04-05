import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/views/HomeView.vue';
import LoginView from '@/views/LoginView.vue';
import OAuthCallbackView from '@/views/OAuthCallbackView.vue';
import VerifyPhoneView from '@/views/VerifyPhoneView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/oauth2/callback',
      name: 'oauth2-callback',
      component: OAuthCallbackView
    },
    {
      path: '/verify-phone',
      name: 'verify-phone',
      component: VerifyPhoneView
    }
  ]
});

export default router;
