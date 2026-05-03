import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import Songs from './pages/Songs';
import Playlists from './pages/Playlists';
import './App.css';

function ProtectedRoute({ children }) {
    const { isAuthenticated } = useAuthStore();
    
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }
    
    return <Layout>{children}</Layout>;
}

function App() {
    return (
        <Router basename="/admin">
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/" element={
                    <ProtectedRoute>
                        <Dashboard />
                    </ProtectedRoute>
                } />
                <Route path="/users" element={
                    <ProtectedRoute>
                        <Users />
                    </ProtectedRoute>
                } />
                <Route path="/songs" element={
                    <ProtectedRoute>
                        <Songs />
                    </ProtectedRoute>
                } />
                <Route path="/playlists" element={
                    <ProtectedRoute>
                        <Playlists />
                    </ProtectedRoute>
                } />
            </Routes>
        </Router>
    );
}

export default App;
