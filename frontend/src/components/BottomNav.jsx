import { useNavigate, useLocation } from 'react-router-dom';
import { FaHome, FaUser, FaUsers, FaMusic } from 'react-icons/fa';
import './BottomNav.css';

const navItems = [
  { path: '/', label: '首页', icon: FaHome },
  { path: '/profile', label: '我的', icon: FaUser },
  { path: '/community', label: '社区', icon: FaUsers },
  { path: '/explore', label: '乐馆', icon: FaMusic },
];

function BottomNav() {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <nav className="bottom-nav">
      {navItems.map((item) => {
        const Icon = item.icon;
        const isActive = location.pathname === item.path;
        return (
          <button
            key={item.path}
            className={`bottom-nav-item ${isActive ? 'active' : ''}`}
            onClick={() => navigate(item.path)}
          >
            <Icon className="bottom-nav-icon" />
            <span className="bottom-nav-label">{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
}

export default BottomNav;
