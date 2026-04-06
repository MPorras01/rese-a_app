import { createRouter, createWebHistory } from 'vue-router';
import BusinessListView from '@/views/BusinessListView.vue';
import HomeView from '@/views/HomeView.vue';
import LoginView from '@/views/LoginView.vue';
import OAuthCallbackView from '@/views/OAuthCallbackView.vue';
import VerifyPhoneView from '@/views/VerifyPhoneView.vue';
import BusinessDetailView from '@/views/BusinessDetailView.vue';
import OwnerDashboardView from '@/views/OwnerDashboardView.vue';
import AdminDashboardView from '@/views/AdminDashboardView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/businesses',
      name: 'business-list',
      component: BusinessListView
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
    },
    {
      path: '/businesses/:id',
      name: 'business-detail',
      component: BusinessDetailView,
      props: true
    },
    {
      path: '/owner/dashboard',
      name: 'owner-dashboard',
      component: OwnerDashboardView
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: AdminDashboardView
    }
  ]
});

export default router;
