import { NavLink, useNavigate } from 'react-router-dom';
import { FiHome, FiUsers, FiMusic, FiList, FiLogOut } from 'react-icons/fi';
import { useAuthStore } from '../store/authStore';
import './Layout.css';

function Layout({ children }) {
    const { logout } = useAuthStore();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div className="admin-layout">
            <aside className="sidebar">
                <div className="sidebar-header">
                    <h2>音乐平台</h2>
                    <span>管理后台</span>
                </div>

                <nav className="sidebar-nav">
                    <NavLink to="/" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <FiHome /> 仪表盘
                    </NavLink>
                    <NavLink to="/users" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <FiUsers /> 用户管理
                    </NavLink>
                    <NavLink to="/songs" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <FiMusic /> 歌曲管理
                    </NavLink>
                    <NavLink to="/playlists" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <FiList /> 歌单管理
                    </NavLink>
                </nav>
                
                <div className="sidebar-footer">
                    <button onClick={handleLogout} className="logout-btn">
                        <FiLogOut /> 退出登录
                    </button>
                </div>
            </aside>
            
            <main className="main-content">
                {children}
            </main>
        </div>
    );
}

export default Layout;
