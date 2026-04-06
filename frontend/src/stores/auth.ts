import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { useLocalStorage } from '@vueuse/core';
import router from '@/router';
import apiClient from '@/api/axios';

export interface User {
  id: string;
  email: string;
  name: string;
  avatarUrl: string | null;
  status: string;
  role: string;
  phoneVerified: boolean;
  emailVerified: boolean;
}

interface OtpRequestResponse {
  otpToken: string;
}

interface OtpVerifyResponse {
  token: string;
}

interface LoginResponse {
  token: string;
}

export const useAuthStore = defineStore('auth', () => {
  const token = useLocalStorage<string | null>('token', null);
  const user = ref<User | null>(null);
  const otpToken = ref<string | null>(null);
  const loading = ref(false);

  const isAuthenticated = computed(() => Boolean(token.value));
  const isActive = computed(() => user.value?.status === 'ACTIVE');
  const isAdmin = computed(() => user.value?.role === 'ADMIN');

  async function initAuth(): Promise<void> {
    if (!token.value) {
      return;
    }

    try {
      await fetchMe();
    } catch {
      logout();
    }
  }

  function loginWithGoogle(): void {
    window.location.href = '/oauth2/authorization/google';
  }

  function loginWithFacebook(): void {
    window.location.href = '/oauth2/authorization/facebook';
  }

  async function loginWithPassword(email: string, password: string): Promise<void> {
    loading.value = true;
    try {
      const response = await apiClient.post<LoginResponse>('/api/auth/login', {
        email,
        password
      });

      token.value = response.data.token;
      await fetchMe();
      await router.push('/');
    } finally {
      loading.value = false;
    }
  }

  async function handleOAuthCallback(newToken: string): Promise<void> {
    token.value = newToken;
    await fetchMe();
    await router.push('/');
  }

  async function fetchMe(): Promise<void> {
    loading.value = true;
    try {
      const response = await apiClient.get<User>('/api/auth/me');
      user.value = response.data;
    } finally {
      loading.value = false;
    }
  }

  async function requestOtp(phone: string): Promise<void> {
    loading.value = true;
    try {
      const response = await apiClient.post<OtpRequestResponse>('/api/auth/otp/request', { phone });
      otpToken.value = response.data.otpToken;
    } finally {
      loading.value = false;
    }
  }

  async function verifyOtp(code: string): Promise<void> {
    if (!otpToken.value) {
      throw new Error('OTP token is missing');
    }

    loading.value = true;
    try {
      const response = await apiClient.post<OtpVerifyResponse>('/api/auth/otp/verify', {
        otpToken: otpToken.value,
        code
      });

      token.value = response.data.token;
      await fetchMe();
      otpToken.value = null;
    } finally {
      loading.value = false;
    }
  }

  function logout(): void {
    user.value = null;
    otpToken.value = null;
    token.value = null;
    localStorage.removeItem('token');
    void router.push('/');
  }

  return {
    user,
    token,
    otpToken,
    loading,
    isAuthenticated,
    isActive,
    isAdmin,
    initAuth,
    loginWithPassword,
    loginWithGoogle,
    loginWithFacebook,
    handleOAuthCallback,
    fetchMe,
    requestOtp,
    verifyOtp,
    logout
  };
});
