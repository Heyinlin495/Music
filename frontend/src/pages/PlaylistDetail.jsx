import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FaPlay, FaTrash, FaEdit, FaPlus, FaSearch, FaMusic, FaTimes, FaCamera } from 'react-icons/fa';
import { playlistsApi, songsApi } from '../api';
import { useAuthStore } from '../store/authStore';
import { usePlayerStore } from '../store/playerStore';
import SongCard from '../components/SongCard';
import './PlaylistDetail.css';

function PlaylistDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, refreshPlaylists } = useAuthStore();
  const { playSong } = usePlayerStore();
  const [playlist, setPlaylist] = useState(null);
  const [loading, setLoading] = useState(true);

  // Edit modal state
  const [showEditModal, setShowEditModal] = useState(false);
  const [editForm, setEditForm] = useState({ name: '', description: '', isPublic: true });
  const [coverFile, setCoverFile] = useState(null);
  const [coverPreview, setCoverPreview] = useState(null);
  const coverInputRef = useRef(null);

  // Add songs modal state
  const [showAddSongs, setShowAddSongs] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [allSongs, setAllSongs] = useState([]);
  const [searchPage, setSearchPage] = useState(0);
  const [searchTotal, setSearchTotal] = useState(0);

  useEffect(() => {
    loadPlaylist();
  }, [id]);

  const loadPlaylist = async () => {
    try {
      const response = await playlistsApi.getById(id);
      setPlaylist(response.data.data);
    } catch (error) {
      console.error('Failed to load playlist:', error);
      navigate('/playlists');
    } finally {
      setLoading(false);
    }
  };

  const handlePlayAll = () => {
    if (playlist?.songs?.length > 0) {
      playSong(playlist.songs[0], playlist.songs);
    }
  };

  const handleDeletePlaylist = async () => {
    if (window.confirm('确定要删除这个歌单吗？')) {
      try {
        await playlistsApi.delete(id);
        refreshPlaylists();
        navigate('/playlists');
      } catch (error) {
        console.error('Failed to delete playlist:', error);
      }
    }
  };

  const handleRemoveSong = async (songId) => {
    try {
      await playlistsApi.removeSong(id, songId);
      loadPlaylist();
    } catch (error) {
      console.error('Failed to remove song:', error);
    }
  };

  // Edit playlist
  const openEditModal = () => {
    setEditForm({
      name: playlist.name,
      description: playlist.description || '',
      isPublic: playlist.isPublic,
    });
    setCoverFile(null);
    setCoverPreview(null);
    setShowEditModal(true);
  };

  const handleCoverSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      setCoverFile(file);
      setCoverPreview(URL.createObjectURL(file));
    }
  };

  const handleSaveEdit = async (e) => {
    e.preventDefault();
    try {
      await playlistsApi.update(id, editForm);
      if (coverFile) {
        const formData = new FormData();
        formData.append('file', coverFile);
        await playlistsApi.uploadCover(id, formData);
      }
      setShowEditModal(false);
      loadPlaylist();
    } catch (error) {
      console.error('Failed to update playlist:', error);
    }
  };

  // Add songs
  const openAddSongs = async () => {
    setShowAddSongs(true);
    setSearchKeyword('');
    setSearchResults([]);
    setSearchPage(0);
    try {
      const res = await songsApi.getAll(0, 50);
      setAllSongs(res.data.data?.content || []);
      setSearchTotal(res.data.data?.totalElements || 0);
    } catch (error) {
      console.error('Failed to load songs:', error);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchKeyword.trim()) {
      setSearchResults([]);
      return;
    }
    try {
      const res = await songsApi.search(searchKeyword.trim(), 0);
      setSearchResults(res.data.data?.content || []);
    } catch (error) {
      console.error('Failed to search songs:', error);
    }
  };

  const handleAddSong = async (songId) => {
    try {
      await playlistsApi.addSong(id, songId);
      loadPlaylist();
    } catch (error) {
      console.error('Failed to add song:', error);
    }
  };

  const isSongInPlaylist = (songId) => {
    return playlist?.songs?.some(s => s.id === songId);
  };

  const songsToShow = searchKeyword.trim() ? searchResults : allSongs;

  if (loading) {
    return (
      <div className="playlist-detail loading">
        <div className="spinner"></div>
        <p>加载中...</p>
      </div>
    );
  }

  if (!playlist) {
    return (
      <div className="playlist-detail">
        <p>歌单不存在</p>
      </div>
    );
  }

  const isOwner = user?.id === playlist.userId;

  return (
    <div className="playlist-detail">
      <div className="playlist-header">
        <div className="playlist-cover" onClick={isOwner ? openEditModal : undefined}>
          {playlist.coverUrl ? (
            <img src={playlist.coverUrl} alt={playlist.name} />
          ) : (
            <div className="default-cover">
              <FaPlay />
            </div>
          )}
          {isOwner && (
            <div className="cover-overlay">
              <FaCamera />
              <span>更换封面</span>
            </div>
          )}
        </div>
        <div className="playlist-info">
          <span className="playlist-type">歌单</span>
          <h1>{playlist.name}</h1>
          {playlist.description && <p className="description">{playlist.description}</p>}
          <div className="playlist-meta">
            <span>{playlist.userName}</span>
            <span>·</span>
            <span>{playlist.songCount || 0} 首歌曲</span>
          </div>
          <div className="playlist-actions">
            <button className="play-all-btn" onClick={handlePlayAll} disabled={!playlist.songs?.length}>
              <FaPlay /> 播放全部
            </button>
            {isOwner && (
              <>
                <button className="edit-btn" onClick={openEditModal}>
                  <FaEdit /> 编辑
                </button>
                <button className="add-songs-btn" onClick={openAddSongs}>
                  <FaPlus /> 添加歌曲
                </button>
                <button className="delete-btn" onClick={handleDeletePlaylist}>
                  <FaTrash /> 删除
                </button>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="playlist-songs">
        <h2>歌曲列表</h2>
        {playlist.songs?.length > 0 ? (
          <div className="songs-list">
            {playlist.songs.map((song) => (
              <div key={song.id} className="song-item">
                <SongCard song={song} playlist={playlist.songs} />
                {isOwner && (
                  <button
                    className="remove-song-btn"
                    onClick={() => handleRemoveSong(song.id)}
                    title="从歌单中移除"
                  >
                    <FaTrash />
                  </button>
                )}
              </div>
            ))}
          </div>
        ) : (
          <p className="empty-message">歌单还没有歌曲</p>
        )}
      </div>

      {/* Edit Modal */}
      {showEditModal && (
        <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>编辑歌单</h3>
            <form onSubmit={handleSaveEdit}>
              <div className="form-group cover-upload-group">
                <div
                  className="cover-upload-preview"
                  onClick={() => coverInputRef.current?.click()}
                >
                  {coverPreview ? (
                    <img src={coverPreview} alt="preview" />
                  ) : playlist.coverUrl ? (
                    <img src={playlist.coverUrl} alt="cover" />
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
                  value={editForm.name}
                  onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                  placeholder="请输入歌单名称"
                  required
                />
              </div>
              <div className="form-group">
                <label>描述</label>
                <textarea
                  value={editForm.description}
                  onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
                  placeholder="歌单描述（可选）"
                  rows="3"
                />
              </div>
              <div className="form-group checkbox">
                <input
                  type="checkbox"
                  id="editIsPublic"
                  checked={editForm.isPublic}
                  onChange={(e) => setEditForm({ ...editForm, isPublic: e.target.checked })}
                />
                <label htmlFor="editIsPublic">公开歌单</label>
              </div>
              <div className="modal-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowEditModal(false)}>
                  取消
                </button>
                <button type="submit" className="submit-btn">保存</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Songs Modal */}
      {showAddSongs && (
        <div className="modal-overlay" onClick={() => setShowAddSongs(false)}>
          <div className="modal modal-wide" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>添加歌曲到歌单</h3>
              <button className="modal-close" onClick={() => setShowAddSongs(false)}>
                <FaTimes />
              </button>
            </div>
            <form className="add-songs-search" onSubmit={handleSearch}>
              <FaSearch className="search-icon" />
              <input
                type="text"
                placeholder="搜索歌曲..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
              />
            </form>
            <div className="add-songs-list">
              {songsToShow.map((song) => {
                const inPlaylist = isSongInPlaylist(song.id);
                return (
                  <div key={song.id} className="add-song-item">
                    <div className="add-song-info">
                      <div className="add-song-cover">
                        {song.coverUrl ? (
                          <img src={song.coverUrl} alt={song.title} />
                        ) : (
                          <div className="add-song-cover-default"><FaMusic /></div>
                        )}
                      </div>
                      <div className="add-song-text">
                        <div className="add-song-title">{song.title}</div>
                        <div className="add-song-artist">{song.artist}</div>
                      </div>
                    </div>
                    <button
                      className={`add-song-btn ${inPlaylist ? 'added' : ''}`}
                      onClick={() => !inPlaylist && handleAddSong(song.id)}
                      disabled={inPlaylist}
                    >
                      {inPlaylist ? '已添加' : <><FaPlus /> 添加</>}
                    </button>
                  </div>
                );
              })}
              {songsToShow.length === 0 && (
                <p className="empty-message">
                  {searchKeyword.trim() ? '没有找到匹配的歌曲' : '暂无歌曲'}
                </p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default PlaylistDetail;
