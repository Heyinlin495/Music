import { useState, useEffect } from 'react';
import { FiUsers, FiMusic, FiList, FiMessageSquare, FiPlay, FiShield } from 'react-icons/fi';
import { adminApi } from '../api';
import './Dashboard.css';

function Dashboard() {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadStats();
    }, []);

    const loadStats = async () => {
        try {
            const response = await adminApi.getDashboard();
            setStats(response.data);
        } catch (error) {
            console.error('Failed to load stats:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="loading">加载中...</div>;
    }

    const statCards = [
        { label: '用户总数', value: stats?.totalUsers || 0, icon: FiUsers, color: '#6366f1' },
        { label: '歌曲总数', value: stats?.totalSongs || 0, icon: FiMusic, color: '#10b981' },
        { label: '歌单总数', value: stats?.totalPlaylists || 0, icon: FiList, color: '#f59e0b' },
        { label: '评论总数', value: stats?.totalComments || 0, icon: FiMessageSquare, color: '#ef4444' },
        { label: '播放总数', value: stats?.totalPlays || 0, icon: FiPlay, color: '#8b5cf6' },
        { label: '管理员数', value: stats?.adminCount || 0, icon: FiShield, color: '#06b6d4' },
    ];

    return (
        <div className="dashboard">
            <h1>仪表盘</h1>
            <p className="dashboard-subtitle">欢迎使用音乐平台管理后台</p>
            
            <div className="stats-grid">
                {statCards.map((stat, index) => (
                    <div key={index} className="stat-card">
                        <div className="stat-icon" style={{ background: stat.color }}>
                            <stat.icon size={24} />
                        </div>
                        <div className="stat-info">
                            <span className="stat-value">{stat.value.toLocaleString()}</span>
                            <span className="stat-label">{stat.label}</span>
                        </div>
                    </div>
                ))}
            </div>
            
            <div className="dashboard-info">
                <h2>快速信息</h2>
                <ul>
                    <li>活跃用户: {stats?.activeUsers || 0}</li>
                    <li>管理员账户: {stats?.adminCount || 0}</li>
                </ul>
            </div>
        </div>
    );
}

export default Dashboard;
