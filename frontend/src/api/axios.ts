import axios from 'axios';

const apiClient = axios.create({
  // Usar mismo origen evita desalineaciones entre Docker local, Vite dev y Vercel redirects.
  baseURL: ''
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');

  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export default apiClient;
