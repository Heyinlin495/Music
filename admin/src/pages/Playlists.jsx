import { useState, useEffect, useRef } from 'react';
import { FiSearch, FiPlus, FiEdit2, FiTrash2, FiList, FiImage, FiMusic, FiX, FiDownload } from 'react-icons/fi';
import { adminApi } from '../api';
import './Playlists.css';

function Playlists() {
    const [playlists, setPlaylists] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [keyword, setKeyword] = useState('');
    const [loading, setLoading] = useState(true);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showSongsModal, setShowSongsModal] = useState(false);
    const [editPlaylist, setEditPlaylist] = useState(null);
    const [formData, setFormData] = useState({
        name: '', description: '', isPublic: true
    });
    const [coverFile, setCoverFile] = useState(null);
    const [coverPreview, setCoverPreview] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const coverInputRef = useRef();

    // Songs management state
    const [selectedPlaylist, setSelectedPlaylist] = useState(null);
    const [playlistDetail, setPlaylistDetail] = useState(null);
    const [songSearchKeyword, setSongSearchKeyword] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [songSearchLoading, setSongSearchLoading] = useState(false);
    const [songsLoading, setSongsLoading] = useState(false);
    const [importing, setImporting] = useState(false);

    useEffect(() => {
        loadPlaylists();
    }, [page]);

    const loadPlaylists = async () => {
        try {
            setLoading(true);
            const response = await adminApi.getPlaylists(page, 10, keyword);
            setPlaylists(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error('Failed to load playlists:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        setPage(0);
        loadPlaylists();
    };

    const resetForm = () => {
        setFormData({ name: '', description: '', isPublic: true });
        setCoverFile(null);
        setCoverPreview(null);
    };

    const handleCoverSelect = (e) => {
        const file = e.target.files[0];
        if (file) {
            setCoverFile(file);
            const reader = new FileReader();
            reader.onloadend = () => setCoverPreview(reader.result);
            reader.readAsDataURL(file);
        }
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        if (!formData.name.trim()) {
            alert('请输入歌单名称');
            return;
        }

        setSubmitting(true);
        try {
            const response = await adminApi.createPlaylist({
                name: formData.name,
                description: formData.description,
                isPublic: formData.isPublic
            });

            if (coverFile) {
                const coverFormData = new FormData();
                coverFormData.append('file', coverFile);
                await adminApi.uploadPlaylistCover(response.data.id, coverFormData);
            }

            setShowCreateModal(false);
            resetForm();
            loadPlaylists();
        } catch (error) {
            const msg = error.response?.data?.error || error.response?.data?.message || '创建歌单失败';
            alert(msg);
        } finally {
            setSubmitting(false);
        }
    };

    const openEditModal = (playlist) => {
        setEditPlaylist(playlist);
        setFormData({
            name: playlist.name,
            description: playlist.description || '',
            isPublic: playlist.isPublic
        });
        setCoverPreview(playlist.coverUrl || null);
        setCoverFile(null);
        setShowEditModal(true);
    };

    const handleEdit = async (e) => {
        e.preventDefault();
        if (!formData.name.trim()) {
            alert('请输入歌单名称');
            return;
        }

        setSubmitting(true);
        try {
            await adminApi.updatePlaylist(editPlaylist.id, {
                name: formData.name,
                description: formData.description,
                isPublic: formData.isPublic
            });

            if (coverFile) {
                const coverFormData = new FormData();
                coverFormData.append('file', coverFile);
                await adminApi.uploadPlaylistCover(editPlaylist.id, coverFormData);
            }

            setShowEditModal(false);
            resetForm();
            loadPlaylists();
        } catch (error) {
            alert(error.response?.data?.error || '更新歌单失败');
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (playlistId) => {
        if (!confirm('确定要删除该歌单吗？')) return;
        try {
            await adminApi.deletePlaylist(playlistId);
            loadPlaylists();
        } catch (error) {
            alert(error.response?.data?.error || '删除歌单失败');
        }
    };

    // Songs management
    const openSongsModal = async (playlist) => {
        setSelectedPlaylist(playlist);
        setShowSongsModal(true);
        setSongSearchKeyword('');
        await Promise.all([
            loadPlaylistDetail(playlist.id),
            loadAllSongs()
        ]);
    };

    const loadAllSongs = async () => {
        try {
            setSongSearchLoading(true);
            const response = await adminApi.getSongs(0, 100);
            setSearchResults(response.data.content || []);
        } catch (error) {
            console.error('Failed to load songs:', error);
        } finally {
            setSongSearchLoading(false);
        }
    };

    const handleImportMusic = async () => {
        if (!confirm('确定要导入 qqyinyue 目录下的本地音乐文件吗？')) return;
        setImporting(true);
        try {
            const response = await adminApi.importLocalMusic();
            const { imported, skipped } = response.data;
            let msg = `导入完成！成功导入 ${imported} 首歌曲`;
            if (skipped > 0) msg += `，跳过 ${skipped} 首`;
            alert(msg);
            await loadAllSongs();
        } catch (error) {
            alert(error.response?.data?.error || '导入失败');
        } finally {
            setImporting(false);
        }
    };

    const loadPlaylistDetail = async (playlistId) => {
        try {
            setSongsLoading(true);
            const response = await adminApi.getPlaylistDetail(playlistId);
            setPlaylistDetail(response.data);
        } catch (error) {
            console.error('Failed to load playlist detail:', error);
            alert('加载歌单详情失败');
        } finally {
            setSongsLoading(false);
        }
    };

    const handleSongSearch = async () => {
        try {
            setSongSearchLoading(true);
            const response = await adminApi.getSongs(0, 100, songSearchKeyword);
            setSearchResults(response.data.content || []);
        } catch (error) {
            console.error('Failed to search songs:', error);
        } finally {
            setSongSearchLoading(false);
        }
    };

    const handleAddSong = async (songId) => {
        if (!selectedPlaylist) return;
        try {
            const response = await adminApi.addSongToPlaylist(selectedPlaylist.id, songId);
            setPlaylistDetail(response.data);
            loadPlaylists();
        } catch (error) {
            alert(error.response?.data?.error || '添加歌曲失败');
        }
    };

    const handleRemoveSong = async (songId) => {
        if (!selectedPlaylist) return;
        try {
            const response = await adminApi.removeSongFromPlaylist(selectedPlaylist.id, songId);
            setPlaylistDetail(response.data);
            loadPlaylists();
        } catch (error) {
            alert(error.response?.data?.error || '移除歌曲失败');
        }
    };

    const isSongInPlaylist = (songId) => {
        return playlistDetail?.songs?.some(s => s.id === songId) || false;
    };

    const formatDate = (dateStr) => {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('zh-CN');
    };

    const renderCoverUpload = () => (
        <div className="cover-upload-area">
            <div className="cover-preview" onClick={() => coverInputRef.current?.click()}>
                {coverPreview ? (
                    <img src={coverPreview} alt="封面预览" />
                ) : (
                    <div className="placeholder">
                        <FiImage size={28} />
                        <span>点击上传封面</span>
                    </div>
                )}
            </div>
            <input
                ref={coverInputRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                onChange={handleCoverSelect}
                style={{ display: 'none' }}
            />
            {coverPreview && (
                <button
                    type="button"
                    className="cover-upload-btn"
                    onClick={() => coverInputRef.current?.click()}
                >
                    更换封面
                </button>
            )}
        </div>
    );

    return (
        <div className="playlists-page">
            <div className="page-header">
                <h1>歌单管理</h1>
                <button className="btn-primary" onClick={() => { resetForm(); setShowCreateModal(true); }}>
                    <FiPlus /> 创建歌单
                </button>
            </div>

            <form className="search-bar" onSubmit={handleSearch}>
                <input
                    type="text"
                    placeholder="搜索歌单..."
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
                                <th>封面</th>
                                <th>歌单名称</th>
                                <th>创建者</th>
                                <th>歌曲数</th>
                                <th>可见性</th>
                                <th>创建时间</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            {playlists.map(playlist => (
                                <tr key={playlist.id}>
                                    <td>{playlist.id}</td>
                                    <td>
                                        <div className="playlist-cover-cell">
                                            {playlist.coverUrl ? (
                                                <img
                                                    className="playlist-cover-thumb"
                                                    src={playlist.coverUrl}
                                                    alt={playlist.name}
                                                />
                                            ) : (
                                                <div className="playlist-cover-placeholder">
                                                    <FiList size={20} />
                                                </div>
                                            )}
                                        </div>
                                    </td>
                                    <td>
                                        <div className="playlist-name-cell">
                                            <span className="name">{playlist.name}</span>
                                            {playlist.description && (
                                                <span className="desc">{playlist.description}</span>
                                            )}
                                        </div>
                                    </td>
                                    <td>{playlist.userName || '-'}</td>
                                    <td>{playlist.songCount || 0}</td>
                                    <td>
                                        <span className={`visibility-tag ${playlist.isPublic ? 'public' : 'private'}`}>
                                            {playlist.isPublic ? '公开' : '私密'}
                                        </span>
                                    </td>
                                    <td>{formatDate(playlist.createdAt)}</td>
                                    <td>
                                        <div className="action-buttons">
                                            <button
                                                className="action-btn songs"
                                                onClick={() => openSongsModal(playlist)}
                                                title="管理歌曲"
                                            >
                                                <FiMusic />
                                            </button>
                                            <button
                                                className="action-btn edit"
                                                onClick={() => openEditModal(playlist)}
                                            >
                                                <FiEdit2 />
                                            </button>
                                            <button
                                                className="action-btn delete"
                                                onClick={() => handleDelete(playlist.id)}
                                            >
                                                <FiTrash2 />
                                            </button>
                                        </div>
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

            {/* Create Modal */}
            {showCreateModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <h2>创建歌单</h2>
                        <form onSubmit={handleCreate}>
                            {renderCoverUpload()}
                            <input
                                type="text"
                                placeholder="歌单名称 *"
                                value={formData.name}
                                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                required
                            />
                            <textarea
                                placeholder="歌单描述"
                                value={formData.description}
                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            />
                            <div className="modal-checkbox">
                                <input
                                    type="checkbox"
                                    id="create-public"
                                    checked={formData.isPublic}
                                    onChange={(e) => setFormData({ ...formData, isPublic: e.target.checked })}
                                />
                                <label htmlFor="create-public">公开歌单</label>
                            </div>
                            <div className="modal-actions">
                                <button type="button" onClick={() => { setShowCreateModal(false); resetForm(); }}>
                                    取消
                                </button>
                                <button type="submit" className="btn-primary" disabled={submitting}>
                                    {submitting ? '创建中...' : '创建'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Edit Modal */}
            {showEditModal && editPlaylist && (
                <div className="modal-overlay">
                    <div className="modal">
                        <h2>编辑歌单</h2>
                        <form onSubmit={handleEdit}>
                            {renderCoverUpload()}
                            <input
                                type="text"
                                placeholder="歌单名称 *"
                                value={formData.name}
                                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                required
                            />
                            <textarea
                                placeholder="歌单描述"
                                value={formData.description}
                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            />
                            <div className="modal-checkbox">
                                <input
                                    type="checkbox"
                                    id="edit-public"
                                    checked={formData.isPublic}
                                    onChange={(e) => setFormData({ ...formData, isPublic: e.target.checked })}
                                />
                                <label htmlFor="edit-public">公开歌单</label>
                            </div>
                            <div className="modal-actions">
                                <button type="button" onClick={() => { setShowEditModal(false); resetForm(); }}>
                                    取消
                                </button>
                                <button type="submit" className="btn-primary" disabled={submitting}>
                                    {submitting ? '保存中...' : '保存'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Songs Management Modal */}
            {showSongsModal && selectedPlaylist && (
                <div className="modal-overlay">
                    <div className="modal songs-modal">
                        <div className="songs-modal-header">
                            <h2>管理歌曲 - {selectedPlaylist.name}</h2>
                            <button className="close-btn" onClick={() => { setShowSongsModal(false); setSelectedPlaylist(null); setPlaylistDetail(null); }}>
                                <FiX />
                            </button>
                        </div>

                        <div className="songs-modal-body">
                            {/* Current songs */}
                            <div className="songs-section">
                                <h3>当前歌曲 ({playlistDetail?.songs?.length || 0})</h3>
                                {songsLoading ? (
                                    <div className="loading">加载中...</div>
                                ) : (
                                    <div className="songs-list">
                                        {playlistDetail?.songs?.length > 0 ? (
                                            playlistDetail.songs.map(song => (
                                                <div key={song.id} className="song-item">
                                                    <div className="song-info">
                                                        <span className="song-title">{song.title}</span>
                                                        <span className="song-artist">{song.artist}</span>
                                                    </div>
                                                    <button
                                                        className="action-btn delete small"
                                                        onClick={() => handleRemoveSong(song.id)}
                                                        title="移除"
                                                    >
                                                        <FiX />
                                                    </button>
                                                </div>
                                            ))
                                        ) : (
                                            <div className="empty-hint">暂无歌曲</div>
                                        )}
                                    </div>
                                )}
                            </div>

                            {/* Search and add songs */}
                            <div className="songs-section">
                                <h3>添加歌曲</h3>
                                <button
                                    className="btn-primary"
                                    onClick={handleImportMusic}
                                    disabled={importing}
                                    style={{ marginBottom: '10px', fontSize: '12px', padding: '6px 12px' }}
                                >
                                    <FiDownload /> {importing ? '导入中...' : '导入本地音乐到曲库'}
                                </button>
                                <div className="song-search-bar">
                                    <input
                                        type="text"
                                        placeholder="搜索歌曲名称或歌手..."
                                        value={songSearchKeyword}
                                        onChange={(e) => setSongSearchKeyword(e.target.value)}
                                        onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleSongSearch())}
                                    />
                                    <button onClick={handleSongSearch} disabled={songSearchLoading}>
                                        <FiSearch />
                                    </button>
                                </div>
                                <div className="songs-list search-results">
                                    {songSearchLoading ? (
                                        <div className="loading">搜索中...</div>
                                    ) : searchResults.length > 0 ? (
                                        searchResults.map(song => (
                                            <div key={song.id} className="song-item">
                                                <div className="song-info">
                                                    <span className="song-title">{song.title}</span>
                                                    <span className="song-artist">{song.artist}</span>
                                                </div>
                                                {isSongInPlaylist(song.id) ? (
                                                    <span className="added-tag">已添加</span>
                                                ) : (
                                                    <button
                                                        className="action-btn add small"
                                                        onClick={() => handleAddSong(song.id)}
                                                        title="添加"
                                                    >
                                                        <FiPlus />
                                                    </button>
                                                )}
                                            </div>
                                        ))
                                    ) : songSearchKeyword ? (
                                        <div className="empty-hint">未找到相关歌曲</div>
                                    ) : null}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Playlists;
