import { create } from 'zustand';
import { authApi } from '../api';

export const useAuthStore = create((set) => ({
    user: JSON.parse(localStorage.getItem('admin_user')) || null,
    token: localStorage.getItem('admin_token'),
    isAuthenticated: !!localStorage.getItem('admin_token'),
    isAdmin: !!localStorage.getItem('admin_user'),
    loading: false,

    login: async (username, password, captchaId, captchaCode) => {
        try {
            set({ loading: true });
            const response = await authApi.login(username, password, captchaId, captchaCode);
            const result = response.data;
            
            if (!result.success) {
                throw new Error(result.message || '登录失败');
            }
            
            const { token, userId, username: userName, nickname, avatar } = result.data;

            localStorage.setItem('admin_token', token);

            // Check if user is admin
            const adminCheck = await authApi.checkAdmin();
            if (!adminCheck.data.isAdmin) {
                localStorage.removeItem('admin_token');
                throw new Error('拒绝访问，需要管理员权限');
            }

            const user = { userId, username: userName, nickname, avatar };
            localStorage.setItem('admin_user', JSON.stringify(user));

            set({
                user,
                token,
                isAuthenticated: true,
                isAdmin: true,
                loading: false
            });
            return { success: true };
        } catch (error) {
            set({ loading: false });
            localStorage.removeItem('admin_token');
            return { 
                success: false, 
                error: error.response?.data?.message || error.message || '登录失败' 
            };
        }
    },

    logout: () => {
        localStorage.removeItem('admin_token');
        localStorage.removeItem('admin_user');
        set({ user: null, token: null, isAuthenticated: false, isAdmin: false });
    },

    checkAuth: async () => {
        const token = localStorage.getItem('admin_token');
        if (!token) {
            set({ isAuthenticated: false, isAdmin: false });
            return false;
        }
        try {
            const response = await authApi.checkAdmin();
            if (response.data.isAdmin) {
                set({ isAuthenticated: true, isAdmin: true });
                return true;
            }
        } catch (error) {
            localStorage.removeItem('admin_token');
        }
        set({ isAuthenticated: false, isAdmin: false });
        return false;
    }
}));
