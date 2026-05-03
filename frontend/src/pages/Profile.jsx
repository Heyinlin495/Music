import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  FaMusic, FaHistory, FaHeart, FaPlus, FaSignOutAlt,
  FaList, FaSearch, FaChevronLeft, FaCamera, FaEdit, FaKey, FaTrashAlt, FaCheckSquare
} from 'react-icons/fa';
import { useAuthStore } from '../store/authStore';
import { usePlayerStore } from '../store/playerStore';
import { usersApi, playlistsApi } from '../api';
import ProfileSongRow from '../components/ProfileSongRow';
import PlaylistCard from '../components/PlaylistCard';
import './Profile.css';

function Profile() {
  const { isAuthenticated, user, logout, updateUser } = useAuthStore();
  const { setHidePlayer } = usePlayerStore();
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const [activeTab, setActiveTab] = useState('favorites');
  const [favorites, setFavorites] = useState([]);
  const [playlists, setPlaylists] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editForm, setEditForm] = useState({ nickname: '', bio: '', email: '' });
  const [saving, setSaving] = useState(false);
  const [editError, setEditError] = useState('');
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [passwordForm, setPasswordForm] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
  const [passwordError, setPasswordError] = useState('');
  const [changingPassword, setChangingPassword] = useState(false);
  const [historySelectMode, setHistorySelectMode] = useState(false);
  const [selectedHistory, setSelectedHistory] = useState(new Set());

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    loadData();
    setHidePlayer(true);
    return () => setHidePlayer(false);
  }, [isAuthenticated, navigate, setHidePlayer]);

  const loadData = async () => {
    try {
      const [favRes, playlistRes, historyRes] = await Promise.all([
        usersApi.getFavorites().catch(() => ({ data: { data: [] } })),
        playlistsApi.getMy().catch(() => ({ data: { data: [] } })),
        usersApi.getHistory(0).catch(() => ({ data: { data: { content: [] } } })),
      ]);
      setFavorites(favRes.data.data || []);
      setPlaylists(playlistRes.data.data || []);
      setHistory(historyRes.data.data?.content || []);
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const handleAvatarClick = () => {
    fileInputRef.current?.click();
  };

  const handleAvatarChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await usersApi.uploadAvatar(formData);
      updateUser({ avatar: res.data.data.avatar });
    } catch (error) {
      console.error('Avatar upload failed:', error);
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleEditClick = () => {
    setEditForm({ nickname: user?.nickname || '', bio: user?.bio || '', email: user?.email || '' });
    setEditError('');
    setShowEditModal(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const res = await usersApi.updateProfile(editForm);
      updateUser(res.data.data);
      setShowEditModal(false);
    } catch (error) {
      const msg = error.response?.data?.message || '更新失败';
      setEditError(msg);
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordClick = () => {
    setPasswordForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
    setPasswordError('');
    setShowPasswordModal(true);
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setPasswordError('');

    if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
      setPasswordError('请填写所有字段');
      return;
    }
    if (passwordForm.newPassword.length < 6) {
      setPasswordError('新密码长度至少6位');
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordError('两次输入的新密码不一致');
      return;
    }

    setChangingPassword(true);
    try {
      await usersApi.changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      });
      setShowPasswordModal(false);
      alert('密码修改成功');
    } catch (error) {
      const msg = error.response?.data?.message || '密码修改失败';
      setPasswordError(msg);
    } finally {
      setChangingPassword(false);
    }
  };

  if (!isAuthenticated) return null;

  const displayName = user?.nickname || user?.username || '用户';
  const tagText = user?.bio && user.bio !== displayName ? user.bio : '音乐爱好者';

  const tabs = [
    { key: 'favorites', label: '我喜欢的', icon: <FaHeart /> },
    { key: 'playlists', label: '我的歌单', icon: <FaList /> },
    { key: 'history', label: '播放记录', icon: <FaHistory /> },
  ];

  return (
    <div className="profile-page">
      {/* Left Panel - User Info */}
      <div className="profile-left">
        <div className="profile-hero">
          <div className="profile-hero-bg"></div>
          <div className="profile-hero-content">
            <div className="profile-top-bar">
              <button className="profile-top-btn" onClick={() => navigate('/')}>
                <FaChevronLeft />
              </button>
              <button className="profile-top-btn" onClick={() => navigate('/search')}>
                <FaSearch />
              </button>
            </div>

            <div className="profile-user-section">
              <div className="profile-avatar-lg" onClick={handleAvatarClick}>
                {user?.avatar ? (
                  <img src={user.avatar} alt={displayName} />
                ) : (
                  <span>{displayName.charAt(0)}</span>
                )}
                <div className="profile-avatar-overlay">
                  {uploading ? <div className="spinner-sm"></div> : <FaCamera />}
                </div>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  onChange={handleAvatarChange}
                  style={{ display: 'none' }}
                />
              </div>
              <div className="profile-user-detail">
                <h1 className="profile-display-name">{displayName}</h1>
                <p className="profile-user-tag">{tagText}</p>
                <p className="profile-user-email">{user?.email || '用户未填写邮箱'}</p>
              </div>
              <button className="profile-edit-btn" onClick={handleEditClick}>
                <FaEdit />
              </button>
              <button className="profile-logout-pill" onClick={handleLogout}>
                <FaSignOutAlt />
              </button>
            </div>

            <div className="profile-stats-row">
              <div className="profile-stat" onClick={() => setActiveTab('favorites')}>
                <strong>{favorites.length}</strong>
                <span>喜欢</span>
              </div>
              <div className="profile-stat-sep"></div>
              <div className="profile-stat" onClick={() => setActiveTab('playlists')}>
                <strong>{playlists.length}</strong>
                <span>歌单</span>
              </div>
              <div className="profile-stat-sep"></div>
              <div className="profile-stat" onClick={() => setActiveTab('history')}>
                <strong>{history.length}</strong>
                <span>记录</span>
              </div>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="profile-shortcuts">
          <div className="profile-shortcut" onClick={() => setActiveTab('favorites')}>
            <div className="profile-shortcut-icon heart"><FaHeart /></div>
            <span>我喜欢的</span>
          </div>
          <div className="profile-shortcut" onClick={() => setActiveTab('history')}>
            <div className="profile-shortcut-icon history"><FaHistory /></div>
            <span>最近播放</span>
          </div>
          <div className="profile-shortcut" onClick={() => navigate('/playlists')}>
            <div className="profile-shortcut-icon list"><FaList /></div>
            <span>歌单管理</span>
          </div>
          <div className="profile-shortcut" onClick={() => navigate('/')}>
            <div className="profile-shortcut-icon discover"><FaMusic /></div>
            <span>发现音乐</span>
          </div>
          <div className="profile-shortcut" onClick={handlePasswordClick}>
            <div className="profile-shortcut-icon password"><FaKey /></div>
            <span>修改密码</span>
          </div>
        </div>

        {/* Tabs (visible on mobile only) */}
        <div className="profile-tab-bar mobile-only">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              className={`profile-tab-btn ${activeTab === tab.key ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.key)}
            >
              {tab.icon}
              <span>{tab.label}</span>
              {activeTab === tab.key && <div className="profile-tab-indicator"></div>}
            </button>
          ))}
        </div>
      </div>

      {/* Right Panel - Content */}
      <div className="profile-right">
        {/* Tabs (visible on desktop) */}
        <div className="profile-tab-bar desktop-only">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              className={`profile-tab-btn ${activeTab === tab.key ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.key)}
            >
              {tab.icon}
              <span>{tab.label}</span>
              {activeTab === tab.key && <div className="profile-tab-indicator"></div>}
            </button>
          ))}
        </div>

        <div className="profile-body">
          {loading ? (
            <div className="profile-loading"><div className="spinner"></div></div>
          ) : (
            <>
              {activeTab === 'favorites' && (
                <div className="profile-section">
                  {favorites.length > 0 ? (
                    <div className="profile-song-list">
                      {favorites.map((song, index) => (
                        <ProfileSongRow
                          key={song.id}
                          song={song}
                          playlist={favorites}
                          index={index}
                          deleteIcon={<FaHeart />}
                          deleteBtnClassName="favorite-heart"
                          onDelete={async (id) => {
                            try {
                              await usersApi.removeFavorite(id);
                              setFavorites(prev => prev.filter(s => s.id !== id));
                            } catch (error) {
                              console.error('Failed to remove favorite:', error);
                            }
                          }}
                        />
                      ))}
                    </div>
                  ) : (
                    <div className="profile-empty-state">
                      <div className="profile-empty-visual"><FaHeart /></div>
                      <h3>还没有喜欢的歌曲</h3>
                      <p>去发现页面找到你喜欢的音乐吧</p>
                      <button className="profile-empty-action" onClick={() => navigate('/search')}>
                        <FaSearch /> 去发现
                      </button>
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'playlists' && (
                <div className="profile-section">
                  <div className="profile-create-card" onClick={() => navigate('/playlists')}>
                    <div className="profile-create-plus"><FaPlus /></div>
                    <div className="profile-create-text">
                      <strong>创建新歌单</strong>
                      <span>整理你喜欢的音乐</span>
                    </div>
                  </div>
                  {playlists.length > 0 ? (
                    <div className="profile-playlist-grid">
                      {playlists.map((playlist) => (
                        <PlaylistCard key={playlist.id} playlist={playlist} />
                      ))}
                    </div>
                  ) : (
                    <div className="profile-empty-state">
                      <div className="profile-empty-visual"><FaList /></div>
                      <h3>还没有歌单</h3>
                      <p>创建你的第一个歌单吧</p>
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'history' && (
                <div className="profile-section">
                  {history.length > 0 ? (
                    <>
                      <div className="profile-section-header">
                        {historySelectMode ? (
                          <div className="profile-select-actions">
                            <button className="profile-select-all-btn" onClick={() => {
                              if (selectedHistory.size === history.length) {
                                setSelectedHistory(new Set());
                              } else {
                                setSelectedHistory(new Set(history.map(s => s.id)));
                              }
                            }}>
                              <FaCheckSquare /> {selectedHistory.size === history.length ? '取消全选' : '全选'}
                            </button>
                            <button
                              className="profile-clear-btn"
                              disabled={selectedHistory.size === 0}
                              onClick={async () => {
                                if (!window.confirm(`确定要删除选中的 ${selectedHistory.size} 条记录吗？`)) return;
                                try {
                                  await usersApi.deleteHistoryBatch([...selectedHistory]);
                                  setHistory(prev => prev.filter(s => !selectedHistory.has(s.id)));
                                  setSelectedHistory(new Set());
                                  setHistorySelectMode(false);
                                } catch (error) {
                                  console.error('Failed to delete history:', error);
                                }
                              }}
                            >
                              <FaTrashAlt /> 删除选中 ({selectedHistory.size})
                            </button>
                            <button className="profile-cancel-btn" onClick={() => {
                              setHistorySelectMode(false);
                              setSelectedHistory(new Set());
                            }}>
                              取消
                            </button>
                          </div>
                        ) : (
                          <div className="profile-select-actions">
                            <button className="profile-select-mode-btn" onClick={() => setHistorySelectMode(true)}>
                              <FaCheckSquare /> 选择
                            </button>
                            <button className="profile-clear-btn" onClick={async () => {
                              if (!window.confirm('确定要清空所有播放记录吗？')) return;
                              try {
                                await usersApi.clearHistory();
                                setHistory([]);
                              } catch (error) {
                                console.error('Failed to clear history:', error);
                              }
                            }}>
                              <FaTrashAlt /> 清空全部
                            </button>
                          </div>
                        )}
                      </div>
                      <div className="profile-song-list">
                        {history.map((song, index) => (
                          <ProfileSongRow
                            key={`h-${song.id}-${index}`}
                            song={song}
                            playlist={history}
                            index={index}
                            selectable={historySelectMode}
                            selected={selectedHistory.has(song.id)}
                            onSelect={(id) => {
                              setSelectedHistory(prev => {
                                const next = new Set(prev);
                                if (next.has(id)) next.delete(id);
                                else next.add(id);
                                return next;
                              });
                            }}
                            onDelete={historySelectMode ? undefined : async (id) => {
                              try {
                                await usersApi.deleteHistoryItem(id);
                                setHistory(prev => prev.filter(s => s.id !== id));
                              } catch (error) {
                                console.error('Failed to delete history item:', error);
                              }
                            }}
                          />
                        ))}
                      </div>
                    </>
                  ) : (
                    <div className="profile-empty-state">
                      <div className="profile-empty-visual"><FaHistory /></div>
                      <h3>还没有播放记录</h3>
                      <p>快去听首歌吧</p>
                      <button className="profile-empty-action" onClick={() => navigate('/')}>
                        <FaMusic /> 去听歌
                      </button>
                    </div>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {showEditModal && (
        <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>编辑个人信息</h3>
            {editError && <div className="password-error">{editError}</div>}
            <form onSubmit={handleEditSubmit}>
              <div className="form-group">
                <label htmlFor="edit-email">邮箱</label>
                <input
                  id="edit-email"
                  type="email"
                  value={editForm.email}
                  onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                  placeholder="输入邮箱"
                />
              </div>
              <div className="form-group">
                <label htmlFor="edit-nickname">昵称</label>
                <input
                  id="edit-nickname"
                  type="text"
                  value={editForm.nickname}
                  onChange={(e) => setEditForm({ ...editForm, nickname: e.target.value })}
                  placeholder="输入昵称（最多5字）"
                  maxLength={5}
                />
              </div>
              <div className="form-group">
                <label htmlFor="edit-bio">个人简介</label>
                <textarea
                  id="edit-bio"
                  value={editForm.bio}
                  onChange={(e) => setEditForm({ ...editForm, bio: e.target.value })}
                  placeholder="介绍一下自己吧"
                  rows={3}
                  maxLength={100}
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowEditModal(false)}>
                  取消
                </button>
                <button type="submit" className="submit-btn" disabled={saving}>
                  {saving ? '保存中...' : '保存'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showPasswordModal && (
        <div className="modal-overlay" onClick={() => setShowPasswordModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>修改密码</h3>
            <form onSubmit={handlePasswordSubmit}>
              <div className="form-group">
                <label htmlFor="old-password">当前密码</label>
                <input
                  id="old-password"
                  type="password"
                  value={passwordForm.oldPassword}
                  onChange={(e) => setPasswordForm({ ...passwordForm, oldPassword: e.target.value })}
                  placeholder="输入当前密码"
                />
              </div>
              <div className="form-group">
                <label htmlFor="new-password">新密码</label>
                <input
                  id="new-password"
                  type="password"
                  value={passwordForm.newPassword}
                  onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                  placeholder="输入新密码（至少6位）"
                />
              </div>
              <div className="form-group">
                <label htmlFor="confirm-password">确认新密码</label>
                <input
                  id="confirm-password"
                  type="password"
                  value={passwordForm.confirmPassword}
                  onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                  placeholder="再次输入新密码"
                />
              </div>
              {passwordError && (
                <div className="password-error">{passwordError}</div>
              )}
              <div className="modal-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowPasswordModal(false)}>
                  取消
                </button>
                <button type="submit" className="submit-btn" disabled={changingPassword}>
                  {changingPassword ? '修改中...' : '确认修改'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Profile;
