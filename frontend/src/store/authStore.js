import { create } from 'zustand';
import { authApi, usersApi } from '../api';

export const useAuthStore = create((set, get) => ({
  user: JSON.parse(localStorage.getItem('user')) || null,
  token: localStorage.getItem('token') || null,
  isAuthenticated: !!localStorage.getItem('token'),
  loading: false,
  error: null,
  playlistVersion: 0,

  login: async (username, password, captchaId, captchaCode) => {
    set({ loading: true, error: null });
    try {
      const response = await authApi.login(username, password, captchaId, captchaCode);
      const { token, userId, username: uname, nickname, avatar, email } = response.data.data;
      const user = { id: userId, username: uname, nickname, avatar, email };
      
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      
      set({ user, token, isAuthenticated: true, loading: false });
      return true;
    } catch (error) {
      set({ error: error.response?.data?.message || 'Login failed', loading: false });
      return false;
    }
  },

  register: async (data) => {
    set({ loading: true, error: null });
    try {
      const response = await authApi.register(data);
      const { token, userId, username, nickname, avatar, email } = response.data.data;
      const user = { id: userId, username, nickname, avatar, email };
      
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      
      set({ user, token, isAuthenticated: true, loading: false });
      return true;
    } catch (error) {
      set({ error: error.response?.data?.message || 'Registration failed', loading: false });
      return false;
    }
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    set({ user: null, token: null, isAuthenticated: false });
  },

  updateUser: (userData) => {
    const updatedUser = { ...get().user, ...userData };
    localStorage.setItem('user', JSON.stringify(updatedUser));
    set({ user: updatedUser });
  },

  clearError: () => set({ error: null }),
  refreshPlaylists: () => set((state) => ({ playlistVersion: state.playlistVersion + 1 })),
}));
