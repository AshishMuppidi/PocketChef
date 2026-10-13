import axios from 'axios';
import { getToken, removeToken } from '../utils/authStorage';

const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
    timeout: 15000,
});

// Request interceptor: attach Authorization header if token exists
apiClient.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: handle 401 Unauthorized globally
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const requestUrl = error.config?.url || '';
      // Only clear token if 401 occurs on protected endpoints, not on failed login attempts
      if (!requestUrl.includes('/auth/login') && !requestUrl.includes('/auth/register')) {
        removeToken();
        // Dispatch custom event for context to react if needed
        window.dispatchEvent(new CustomEvent('auth:unauthorized'));
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
