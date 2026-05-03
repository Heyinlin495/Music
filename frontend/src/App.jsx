import { BrowserRouter as Router, Routes, Route, useLocation, Link } from 'react-router-dom';
import { FaHome, FaSearch } from 'react-icons/fa';
import Sidebar from './components/Sidebar';
import Player from './components/Player';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Search from './pages/Search';
import Playlists from './pages/Playlists';
import PlaylistDetail from './pages/PlaylistDetail';
import Profile from './pages/Profile';
import Upload from './pages/Upload';
import GenrePage from './pages/GenrePage';
import AllSongs from './pages/AllSongs';
import './App.css';

const noSidebarRoutes = ['/profile'];

function NotFound() {
  return (
    <div className="not-found-page">
      <div className="not-found-content">
        <h1>404</h1>
        <p>页面未找到</p>
        <div className="not-found-actions">
          <Link to="/" className="not-found-btn"><FaHome /> 返回首页</Link>
          <Link to="/search" className="not-found-btn"><FaSearch /> 搜索音乐</Link>
        </div>
      </div>
    </div>
  );
}

function AppLayout() {
  const location = useLocation();
  const hideSidebar = noSidebarRoutes.some(r => location.pathname.startsWith(r));

  return (
    <div className="app">
      {!hideSidebar && <Sidebar />}
      <div className={`app-content ${hideSidebar ? 'no-player' : ''}`}>
        <main className={`main-content ${hideSidebar ? 'no-player' : ''}`}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/search" element={<Search />} />
            <Route path="/playlists" element={<Playlists />} />
            <Route path="/playlist/:id" element={<PlaylistDetail />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/upload" element={<Upload />} />
            <Route path="/genre/:genre" element={<GenrePage />} />
            <Route path="/songs" element={<AllSongs />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </main>
        <Player />
      </div>
    </div>
  );
}

function App() {
  return (
    <Router>
      <AppLayout />
    </Router>
  );
}

export default App;
