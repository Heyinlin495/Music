import { useState, useEffect, useRef } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { FaPlus, FaCamera } from 'react-icons/fa';
import { playlistsApi } from '../api';
import { useAuthStore } from '../store/authStore';
import PlaylistCard from '../components/PlaylistCard';
import './Playlists.css';

function Playlists() {
  const { isAuthenticated, refreshPlaylists } = useAuthStore();
  const location = useLocation();
  const [myPlaylists, setMyPlaylists] = useState([]);
  const [publicPlaylists, setPublicPlaylists] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newPlaylist, setNewPlaylist] = useState({ name: '', description: '', isPublic: true });
  const [coverFile, setCoverFile] = useState(null);
  const [coverPreview, setCoverPreview] = useState(null);
  const coverInputRef = useRef(null);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState('');

  useEffect(() => {
    loadPlaylists();
  }, [isAuthenticated, location]);

  const loadPlaylists = async () => {
    try {
      const publicRes = await playlistsApi.getPublic(0);
      setPublicPlaylists(publicRes.data.data?.content || []);
      
      if (isAuthenticated) {
        const myRes = await playlistsApi.getMy();
        setMyPlaylists(myRes.data.data || []);
      }
    } catch (error) {
      console.error('Failed to load playlists:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCoverSelect = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setCoverFile(file);
    setCoverPreview(URL.createObjectURL(file));
  };

  const handleCreatePlaylist = async (e) => {
    e.preventDefault();
    setCreateError('');
    setCreating(true);
    try {
      const res = await playlistsApi.create(newPlaylist);
      const playlistId = res.data.data.id;
      if (coverFile) {
        try {
          const formData = new FormData();
          formData.append('file', coverFile);
          await playlistsApi.uploadCover(playlistId, formData);
        } catch (coverErr) {
          console.error('Cover upload failed:', coverErr.response?.data || coverErr);
        }
      }
      setShowCreateModal(false);
      setNewPlaylist({ name: '', description: '', isPublic: true });
      setCoverFile(null);
      setCoverPreview(null);
      loadPlaylists();
      refreshPlaylists();
    } catch (error) {
      const msg = error.response?.data?.message || error.message || '创建失败，请重试';
      setCreateError(msg);
      console.error('Create playlist error:', error.response?.data || error);
    } finally {
      setCreating(false);
    }
  };

  if (loading) {
    return (
      <div className="playlists-page loading">
        <div className="spinner"></div>
        <p>加载中...</p>
      </div>
    );
  }

  return (
    <div className="playlists-page">
      {isAuthenticated && (
        <section className="section">
          <div className="section-header">
            <h2>我的歌单</h2>
            <button className="create-btn" onClick={() => setShowCreateModal(true)}>
              <FaPlus /> 创建歌单
            </button>
          </div>
          {myPlaylists.length > 0 ? (
            <div className="playlists-grid">
              {myPlaylists.map((playlist) => (
                <PlaylistCard key={playlist.id} playlist={playlist} />
              ))}
            </div>
          ) : (
            <p className="empty-message">还没有创建歌单，点击上方按钮创建第一个吧！</p>
          )}
        </section>
      )}

      <section className="section">
        <h2>发现歌单</h2>
        {publicPlaylists.length > 0 ? (
          <div className="playlists-grid">
            {publicPlaylists.map((playlist) => (
              <PlaylistCard key={playlist.id} playlist={playlist} />
            ))}
          </div>
        ) : (
          <p className="empty-message">暂无公开歌单</p>
        )}
      </section>

      {showCreateModal && (
        <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>创建歌单</h3>
            {createError && <div className="create-error">{createError}</div>}
            <form onSubmit={handleCreatePlaylist}>
              <div className="form-group cover-upload-group">
                <div className="cover-upload-preview" onClick={() => coverInputRef.current?.click()}>
                  {coverPreview ? (
                    <img src={coverPreview} alt="封面预览" />
                  ) : (
                    <div className="cover-upload-placeholder">
                      <FaCamera />
                      <span>上传封面</span>
                    </div>
                  )}
                </div>
                <input
                  ref={coverInputRef}
                  type="file"
                  accept="image/*"
                  onChange={handleCoverSelect}
                  style={{ display: 'none' }}
                />
              </div>
              <div className="form-group">
                <label>歌单名称</label>
                <input
                  type="text"
                  value={newPlaylist.name}
                  onChange={(e) => setNewPlaylist({ ...newPlaylist, name: e.target.value })}
                  placeholder="请输入歌单名称"
                  required
                />
              </div>
              <div className="form-group">
                <label>描述</label>
                <textarea
                  value={newPlaylist.description}
                  onChange={(e) => setNewPlaylist({ ...newPlaylist, description: e.target.value })}
                  placeholder="歌单描述（可选）"
                  rows="3"
                />
              </div>
              <div className="form-group checkbox">
                <input
                  type="checkbox"
                  id="isPublic"
                  checked={newPlaylist.isPublic}
                  onChange={(e) => setNewPlaylist({ ...newPlaylist, isPublic: e.target.checked })}
                />
                <label htmlFor="isPublic">公开歌单</label>
              </div>
              <div className="modal-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowCreateModal(false)}>
                  取消
                </button>
                <button type="submit" className="submit-btn" disabled={creating}>
                  {creating ? '创建中...' : '创建'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Playlists;
