import axios from 'axios';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/frontend/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  login: (username, password, captchaId, captchaCode) =>
    api.post('/auth/login', { username, password, captchaId, captchaCode }),
  register: (data) => api.post('/auth/register', data),
  getCaptcha: () => api.get('/auth/captcha'),
};

// Songs API
export const songsApi = {
  getAll: (page = 0, size = 20) => api.get(`/songs?page=${page}&size=${size}`),
  getById: (id) => api.get(`/songs/${id}`),
  search: (keyword, page = 0) => api.get(`/songs/search?keyword=${keyword}&page=${page}`),
  getTop: (limit = 10) => api.get(`/songs/top?limit=${limit}`),
  getLatest: (limit = 10) => api.get(`/songs/latest?limit=${limit}`),
  getDaily: (limit = 10) => api.get(`/songs/daily?limit=${limit}`),
  getRandom: (limit = 10) => api.get(`/songs/random?limit=${limit}`),
  getGenres: () => api.get('/songs/genres'),
  getGenreSummaries: () => api.get('/songs/genres/summary'),
  getByGenre: (genre, page = 0, size = 20) => api.get(`/songs/genre/${genre}?page=${page}&size=${size}`),
  getRecommendedAlbums: (page = 0, size = 8) => api.get(`/songs/albums/recommended?page=${page}&size=${size}`),
  play: (id) => api.post(`/songs/${id}/play`),
  upload: (formData) => api.post('/songs', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  delete: (id) => api.delete(`/songs/${id}`),
};

// Playlists API
export const playlistsApi = {
  getPublic: (page = 0) => api.get(`/playlists?page=${page}`),
  getFeatured: (limit = 6) => api.get(`/playlists/featured?limit=${limit}`),
  getMy: () => api.get('/playlists/my'),
  getById: (id) => api.get(`/playlists/${id}`),
  create: (data) => api.post('/playlists', data),
  update: (id, data) => api.put(`/playlists/${id}`, data),
  delete: (id) => api.delete(`/playlists/${id}`),
  uploadCover: (id, formData) => api.post(`/playlists/${id}/cover`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  addSong: (playlistId, songId) => api.post(`/playlists/${playlistId}/songs/${songId}`),
  removeSong: (playlistId, songId) => api.delete(`/playlists/${playlistId}/songs/${songId}`),
};

// Users API
export const usersApi = {
  getMe: () => api.get('/users/me'),
  getById: (id) => api.get(`/users/${id}`),
  updateProfile: (data) => api.put('/users/me', data),
  uploadAvatar: (formData) => api.post('/users/me/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  changePassword: (data) => api.put('/users/me/password', data),
  getFavorites: () => api.get('/users/me/favorites'),
  addFavorite: (songId) => api.post(`/users/me/favorites/${songId}`),
  removeFavorite: (songId) => api.delete(`/users/me/favorites/${songId}`),
  getHistory: (page = 0) => api.get(`/users/me/history?page=${page}`),
  clearHistory: () => api.delete('/users/me/history'),
  deleteHistoryItem: (songId) => api.delete(`/users/me/history/${songId}`),
  deleteHistoryBatch: (songIds) => api.delete('/users/me/history/batch', { data: songIds }),
};

export default api;
