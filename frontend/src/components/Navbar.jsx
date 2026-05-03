import { Link, useNavigate } from 'react-router-dom';
import { FaHome, FaSearch, FaMusic, FaUser, FaSignOutAlt, FaUpload } from 'react-icons/fa';
import { useAuthStore } from '../store/authStore';
import './Navbar.css';

function Navbar() {
  const { isAuthenticated, user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">
          <FaMusic className="brand-icon" />
          <span>MusicHub</span>
        </Link>
      </div>

      <div className="navbar-links">
        <Link to="/" className="nav-link">
          <FaHome />
          <span>首页</span>
        </Link>
        <Link to="/search" className="nav-link">
          <FaSearch />
          <span>搜索</span>
        </Link>
        {isAuthenticated && (
          <>
            <Link to="/playlists" className="nav-link">
              <FaMusic />
              <span>歌单</span>
            </Link>
            <Link to="/upload" className="nav-link">
              <FaUpload />
              <span>上传</span>
            </Link>
          </>
        )}
      </div>

      <div className="navbar-auth">
        {isAuthenticated ? (
          <div className="user-menu">
            <Link to="/profile" className="user-link">
              <div className="user-avatar">
                {user?.avatar ? (
                  <img src={user.avatar} alt={user.nickname} />
                ) : (
                  <FaUser />
                )}
              </div>
              <span>{user?.nickname || user?.username}</span>
            </Link>
            <button onClick={handleLogout} className="logout-btn">
              <FaSignOutAlt />
            </button>
          </div>
        ) : (
          <div className="auth-links">
            <Link to="/login" className="auth-link">登录</Link>
            <Link to="/register" className="auth-link register">注册</Link>
          </div>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
