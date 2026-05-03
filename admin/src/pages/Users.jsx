import { useState, useEffect } from 'react';
import { FiSearch, FiEdit2, FiTrash2, FiUserPlus, FiToggleLeft, FiToggleRight } from 'react-icons/fi';
import { adminApi } from '../api';
import './Users.css';

function Users() {
    const [users, setUsers] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [keyword, setKeyword] = useState('');
    const [loading, setLoading] = useState(true);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [newAdmin, setNewAdmin] = useState({ username: '', password: '', email: '', nickname: '' });

    useEffect(() => {
        loadUsers();
    }, [page]);

    const loadUsers = async () => {
        try {
            setLoading(true);
            const response = await adminApi.getUsers(page, 10, keyword);
            setUsers(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error('Failed to load users:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        setPage(0);
        loadUsers();
    };

    const handleToggleStatus = async (userId) => {
        try {
            await adminApi.toggleUserStatus(userId);
            loadUsers();
        } catch (error) {
            alert('切换用户状态失败');
        }
    };

    const handleUpdateRole = async (userId, role) => {
        try {
            await adminApi.updateUserRole(userId, role);
            loadUsers();
        } catch (error) {
            alert('更新角色失败');
        }
    };

    const handleDelete = async (userId) => {
        if (!confirm('确定要删除该用户吗？')) return;
        try {
            await adminApi.deleteUser(userId);
            loadUsers();
        } catch (error) {
            alert(error.response?.data?.message || '删除用户失败');
        }
    };

    const handleCreateAdmin = async (e) => {
        e.preventDefault();
        try {
            await adminApi.createAdmin(newAdmin);
            setShowCreateModal(false);
            setNewAdmin({ username: '', password: '', email: '', nickname: '' });
            loadUsers();
        } catch (error) {
            alert(error.response?.data?.message || '创建管理员失败');
        }
    };

    return (
        <div className="users-page">
            <div className="page-header">
                <h1>用户管理</h1>
                <button className="btn-primary" onClick={() => setShowCreateModal(true)}>
                    <FiUserPlus /> 创建管理员
                </button>
            </div>

            <form className="search-bar" onSubmit={handleSearch}>
                <input
                    type="text"
                    placeholder="搜索用户..."
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                />
                <button type="submit"><FiSearch /></button>
            </form>

            {loading ? (
                <div className="loading">加载中...</div>
            ) : (
                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>用户名</th>
                                <th>邮箱</th>
                                <th>昵称</th>
                                <th>角色</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map(user => (
                                <tr key={user.id}>
                                    <td>{user.id}</td>
                                    <td>{user.username}</td>
                                    <td>{user.email}</td>
                                    <td>{user.nickname || '-'}</td>
                                    <td>
                                        <select
                                            value={user.role}
                                            onChange={(e) => handleUpdateRole(user.id, e.target.value)}
                                            className={`role-select ${user.role.toLowerCase()}`}
                                        >
                                            <option value="USER">普通用户</option>
                                            <option value="ADMIN">管理员</option>
                                        </select>
                                    </td>
                                    <td>
                                        <button
                                            className={`status-btn ${user.enabled ? 'active' : 'inactive'}`}
                                            onClick={() => handleToggleStatus(user.id)}
                                        >
                                            {user.enabled ? <FiToggleRight /> : <FiToggleLeft />}
                                            {user.enabled ? '正常' : '禁用'}
                                        </button>
                                    </td>
                                    <td>
                                        <button className="action-btn delete" onClick={() => handleDelete(user.id)}>
                                            <FiTrash2 />
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            <div className="pagination">
                <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>上一页</button>
                <span>第 {page + 1} 页 / 共 {totalPages || 1} 页</span>
                <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>下一页</button>
            </div>

            {showCreateModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <h2>创建管理员账户</h2>
                        <form onSubmit={handleCreateAdmin}>
                            <input
                                type="text"
                                placeholder="用户名"
                                value={newAdmin.username}
                                onChange={(e) => setNewAdmin({ ...newAdmin, username: e.target.value })}
                                required
                            />
                            <input
                                type="password"
                                placeholder="密码"
                                value={newAdmin.password}
                                onChange={(e) => setNewAdmin({ ...newAdmin, password: e.target.value })}
                                required
                            />
                            <input
                                type="email"
                                placeholder="邮箱"
                                value={newAdmin.email}
                                onChange={(e) => setNewAdmin({ ...newAdmin, email: e.target.value })}
                                required
                            />
                            <input
                                type="text"
                                placeholder="昵称"
                                value={newAdmin.nickname}
                                onChange={(e) => setNewAdmin({ ...newAdmin, nickname: e.target.value })}
                            />
                            <div className="modal-actions">
                                <button type="button" onClick={() => setShowCreateModal(false)}>取消</button>
                                <button type="submit" className="btn-primary">创建</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Users;
