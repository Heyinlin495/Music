import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { FaHome, FaSearch, FaMusic, FaPlus, FaHeart, FaUser, FaSignOutAlt, FaList, FaGlobe } from 'react-icons/fa';
import { useAuthStore } from '../store/authStore';
import { playlistsApi } from '../api';
import './Sidebar.css';

function Sidebar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, user, logout, playlistVersion } = useAuthStore();
  const [userPlaylists, setUserPlaylists] = useState([]);

  useEffect(() => {
    if (isAuthenticated) {
      playlistsApi.getMy()
        .then(res => setUserPlaylists(res.data.data || []))
        .catch(() => {});
    }
  }, [isAuthenticated, location, playlistVersion]);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <aside className="sidebar">
      {/* Logo / User Info */}
      {isAuthenticated && user ? (
        <div className="sidebar-logo sidebar-user-top">
          <Link to="/profile" className="sidebar-logo-icon">
            {user.avatar ? (
              <img src={user.avatar} alt={user.nickname || user.username} style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '50%' }} />
            ) : (
              <FaUser />
            )}
          </Link>
          <div className="sidebar-user-top-info">
            <span className="sidebar-logo-text">{user.nickname || user.username}</span>
            <span className="sidebar-user-top-email">{user.email || ''}</span>
          </div>
          <button onClick={handleLogout} className="sidebar-logout-btn" title="退出登录">
            <FaSignOutAlt />
          </button>
        </div>
      ) : (
        <div className="sidebar-logo">
          <div className="sidebar-logo-icon">
            <FaMusic />
          </div>
          <span className="sidebar-logo-text">MusicHub</span>
        </div>
      )}

      {/* Main Navigation */}
      <nav className="sidebar-nav">
        <Link to="/" className={`sidebar-link ${isActive('/') ? 'active' : ''}`}>
          <FaHome className="sidebar-link-icon" />
          <span>首页</span>
        </Link>
        <Link to="/search" className={`sidebar-link ${isActive('/search') ? 'active' : ''}`}>
          <FaSearch className="sidebar-link-icon" />
          <span>搜索</span>
        </Link>
        <Link to="/songs" className={`sidebar-link ${isActive('/songs') ? 'active' : ''}`}>
          <FaGlobe className="sidebar-link-icon" />
          <span>所有歌曲</span>
        </Link>
        {isAuthenticated && (
          <Link to="/playlists" className={`sidebar-link ${isActive('/playlists') ? 'active' : ''}`}>
            <FaMusic className="sidebar-link-icon" />
            <span>音乐库</span>
          </Link>
        )}
      </nav>

      <div className="sidebar-divider" />

      {/* Secondary Actions */}
      <div className="sidebar-secondary">
        {isAuthenticated ? (
          <>
            <Link to="/playlists" className="sidebar-link">
              <FaPlus className="sidebar-link-icon" />
              <span>创建播放列表</span>
            </Link>
            <Link to="/profile" className="sidebar-link sidebar-link-liked">
              <FaHeart className="sidebar-link-icon" />
              <span>我喜欢的音乐</span>
            </Link>
          </>
        ) : (
          <>
            <Link to="/login" className="sidebar-link">
              <FaUser className="sidebar-link-icon" />
              <span>登录</span>
            </Link>
            <Link to="/register" className="sidebar-link">
              <FaPlus className="sidebar-link-icon" />
              <span>注册账号</span>
            </Link>
          </>
        )}
      </div>

      {/* User Playlists */}
      {isAuthenticated && userPlaylists.length > 0 && (
        <>
          <div className="sidebar-divider" />
          <div className="sidebar-playlists">
            <div className="sidebar-playlists-header">
              <span>我的歌单</span>
            </div>
            <div className="sidebar-playlists-list">
              {userPlaylists.map(playlist => (
                <Link
                  key={playlist.id}
                  to={`/playlist/${playlist.id}`}
                  className={`sidebar-link sidebar-playlist-link ${isActive(`/playlist/${playlist.id}`) ? 'active' : ''}`}
                >
                  <FaList className="sidebar-link-icon" />
                  <span>{playlist.name}</span>
                </Link>
              ))}
            </div>
          </div>
        </>
      )}

    </aside>
  );
}

export default Sidebar;
