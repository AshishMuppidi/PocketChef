import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import apiClient from '../api/apiClient';
import { getToken, setToken, removeToken } from '../utils/authStorage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setTokenState] = useState(() => getToken());
  const [isLoading, setIsLoading] = useState(true);

  // Fetch current authenticated user profile using stored JWT
  const refreshUser = useCallback(async () => {
    const currentToken = getToken();
    if (!currentToken) {
      setUser(null);
      setTokenState(null);
      setIsLoading(false);
      return null;
    }

    try {
      const response = await apiClient.get('/auth/me');
      setUser(response.data);
      setTokenState(currentToken);
      return response.data;
    } catch (error) {
      if (error.response && error.response.status === 401) {
        removeToken();
        setUser(null);
        setTokenState(null);
      }
      return null;
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Initialize authentication on application startup
  useEffect(() => {
    refreshUser();

    // Listen to unauthorized global events from Axios interceptor
    const handleUnauthorized = () => {
      setUser(null);
      setTokenState(null);
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
    };
  }, [refreshUser]);

  // Login handler
  const login = async (email, password) => {
    const response = await apiClient.post('/auth/login', { email, password });
    const authData = response.data; // { accessToken, tokenType, expiresIn }

    if (authData?.accessToken) {
      setToken(authData.accessToken);
      setTokenState(authData.accessToken);

      // Fetch user profile with newly set token
      const profileResponse = await apiClient.get('/auth/me');
      setUser(profileResponse.data);
      return profileResponse.data;
    }
    throw new Error('Authentication failed: No access token received from server.');
  };

  // Register handler
  const register = async (username, email, password, displayName) => {
    const response = await apiClient.post('/auth/register', {
      username,
      email,
      password,
      displayName: displayName || username
    });
    const authData = response.data;

    if (authData?.accessToken) {
      setToken(authData.accessToken);
      setTokenState(authData.accessToken);

      const profileResponse = await apiClient.get('/auth/me');
      setUser(profileResponse.data);
      return profileResponse.data;
    }
    throw new Error('Registration failed: No access token received from server.');
  };

  // Logout handler
  const logout = () => {
    removeToken();
    setUser(null);
    setTokenState(null);
  };

  const value = {
    user,
    token,
    isAuthenticated: !!user && !!token,
    isLoading,
    login,
    register,
    logout,
    refreshUser
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
