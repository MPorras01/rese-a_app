import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
    { path: '/explore', name: 'explore', component: () => import('../views/BusinessListView.vue') },
    {
      path: '/business/:id',
      name: 'business-detail',
      component: () => import('../views/BusinessDetailView.vue'),
      props: true
    },
    {
      path: '/business/:id/review/new',
      name: 'write-review',
      component: () => import('../views/WriteReviewView.vue'),
      props: true,
      meta: { requiresAuth: true }
    },
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
    { path: '/oauth2/callback', name: 'oauth2-callback', component: () => import('../views/auth/OAuthCallbackView.vue') },
    {
      path: '/verify-phone',
      name: 'verify-phone',
      component: () => import('../views/auth/VerifyPhoneView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/reviews',
      name: 'profile-reviews',
      component: () => import('../views/MisResenasView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/owner/dashboard',
      name: 'owner-dashboard',
      component: () => import('../views/owner/OwnerDashboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('../views/admin/AdminDashboardView.vue'),
      meta: { requiresAdmin: true }
    },
    { path: '/about', name: 'about', component: () => import('../views/AboutView.vue') },
    { path: '/contact', name: 'contact', component: () => import('../views/ContactView.vue') },
    { path: '/map', name: 'map', component: () => import('../views/MapView.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
});

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore();

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    next('/');
    return;
  }

  if (to.path === '/login' && auth.isAuthenticated) {
    next('/');
    return;
  }

  if (
    auth.isAuthenticated &&
    auth.user !== null &&
    auth.user.status !== 'ACTIVE' &&
    to.path !== '/verify-phone' &&
    to.path !== '/login' &&
    to.path !== '/oauth2/callback'
  ) {
    next('/verify-phone');
    return;
  }

  next();
});

export default router;
