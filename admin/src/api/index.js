import axios from 'axios';

const api = axios.create({
    baseURL: '/api'
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('admin_token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401 || error.response?.status === 403) {
            localStorage.removeItem('admin_token');
            window.location.href = '/admin/login';
        }
        return Promise.reject(error);
    }
);

export const authApi = {
    login: (username, password, captchaId, captchaCode) =>
        api.post('/auth/login', { username, password, captchaId, captchaCode }),
    checkAdmin: () => api.get('/admin/check'),
    getCaptcha: () => api.get('/auth/captcha'),
};

export const adminApi = {
    getDashboard: () => api.get('/admin/dashboard'),

    // Users
    getUsers: (page = 0, size = 10, keyword = '') =>
        api.get('/admin/users', { params: { page, size, keyword: keyword || undefined } }),
    getUser: (id) => api.get(`/admin/users/${id}`),
    updateUserRole: (id, role) => api.put(`/admin/users/${id}/role`, null, { params: { role } }),
    toggleUserStatus: (id) => api.put(`/admin/users/${id}/toggle-status`),
    deleteUser: (id) => api.delete(`/admin/users/${id}`),
    createAdmin: (data) => api.post('/admin/users/create-admin', data),

    // Songs
    getSongs: (page = 0, size = 10, keyword = '') =>
        api.get('/admin/songs', { params: { page, size, keyword: keyword || undefined } }),
    updateSong: (id, data) => api.put(`/admin/songs/${id}`, data),
    deleteSong: (id) => api.delete(`/admin/songs/${id}`),

    // Playlists
    getPlaylists: (page = 0, size = 10, keyword = '') =>
        api.get('/admin/playlists', { params: { page, size, keyword: keyword || undefined } }),
    getPlaylistDetail: (id) => api.get(`/admin/playlists/${id}`),
    createPlaylist: (data) => api.post('/admin/playlists', data),
    updatePlaylist: (id, data) => api.put(`/admin/playlists/${id}`, data),
    uploadPlaylistCover: (id, formData) => api.post(`/admin/playlists/${id}/cover`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),
    deletePlaylist: (id) => api.delete(`/admin/playlists/${id}`),
    addSongToPlaylist: (playlistId, songId) => api.post(`/admin/playlists/${playlistId}/songs/${songId}`),
    removeSongFromPlaylist: (playlistId, songId) => api.delete(`/admin/playlists/${playlistId}/songs/${songId}`),

    // Import local music
    importLocalMusic: (directory = '/host') => api.post(`/admin/music/import?directory=${encodeURIComponent(directory)}`),

    // Batch upload local music files from browser
    batchUploadMusic: (formData, onUploadProgress) => api.post('/admin/music/batch-upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress,
        timeout: 600000
    })
};

export default api;
