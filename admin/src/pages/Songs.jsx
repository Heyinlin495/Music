import { useState, useEffect, useRef } from 'react';
import { FiSearch, FiUpload, FiEdit2, FiTrash2, FiMusic, FiDownload } from 'react-icons/fi';
import { adminApi } from '../api';
import './Songs.css';

function Songs() {
    const [songs, setSongs] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [keyword, setKeyword] = useState('');
    const [loading, setLoading] = useState(true);
    const [showUploadModal, setShowUploadModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [editSong, setEditSong] = useState(null);
    const [uploadData, setUploadData] = useState({
        title: '', artist: '', album: '', genre: '', duration: 0
    });
    const [uploadFile, setUploadFile] = useState(null);
    const [coverFile, setCoverFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [importing, setImporting] = useState(false);
    const fileInputRef = useRef();

    useEffect(() => {
        loadSongs();
    }, [page]);

    const loadSongs = async () => {
        try {
            setLoading(true);
            const response = await adminApi.getSongs(page, 10, keyword);
            setSongs(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error('Failed to load songs:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        setPage(0);
        loadSongs();
    };

    const handleImport = async () => {
        if (!confirm('确定要导入 qqyinyue 目录下的本地音乐文件吗？')) return;
        setImporting(true);
        try {
            const response = await adminApi.importLocalMusic();
            const { imported, skipped, errors } = response.data;
            let msg = `导入完成！成功导入 ${imported} 首歌曲`;
            if (skipped > 0) msg += `，跳过 ${skipped} 首（已存在）`;
            if (errors && errors.length > 0) msg += `\n错误: ${errors.join(', ')}`;
            alert(msg);
            loadSongs();
        } catch (error) {
            alert(error.response?.data?.error || '导入失败');
        } finally {
            setImporting(false);
        }
    };

    const handleUpload = async (e) => {
        e.preventDefault();
        if (!uploadFile) {
            alert('请选择音乐文件');
            return;
        }

        setUploading(true);
        const formData = new FormData();
        formData.append('file', uploadFile);
        formData.append('title', uploadData.title);
        formData.append('artist', uploadData.artist);
        formData.append('album', uploadData.album || '');
        formData.append('genre', uploadData.genre || '');
        formData.append('duration', uploadData.duration || 0);
        if (coverFile) {
            formData.append('cover', coverFile);
        }

        try {
            await adminApi.uploadSong(formData);
            setShowUploadModal(false);
            setUploadData({ title: '', artist: '', album: '', genre: '', duration: 0 });
            setUploadFile(null);
            setCoverFile(null);
            loadSongs();
        } catch (error) {
            alert(error.response?.data?.message || '上传歌曲失败');
        } finally {
            setUploading(false);
        }
    };

    const handleEdit = async (e) => {
        e.preventDefault();
        try {
            await adminApi.updateSong(editSong.id, {
                title: editSong.title,
                artist: editSong.artist,
                album: editSong.album,
                genre: editSong.genre
            });
            setShowEditModal(false);
            loadSongs();
        } catch (error) {
            alert('更新歌曲失败');
        }
    };

    const handleDelete = async (songId) => {
        if (!confirm('确定要删除该歌曲吗？')) return;
        try {
            await adminApi.deleteSong(songId);
            loadSongs();
        } catch (error) {
            alert('删除歌曲失败');
        }
    };

    const formatDuration = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    return (
        <div className="songs-page">
            <div className="page-header">
                <h1>歌曲管理</h1>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button className="btn-primary" onClick={handleImport} disabled={importing}>
                        <FiDownload /> {importing ? '导入中...' : '导入本地音乐'}
                    </button>
                    <button className="btn-primary" onClick={() => setShowUploadModal(true)}>
                        <FiUpload /> 上传歌曲
                    </button>
                </div>
            </div>

            <form className="search-bar" onSubmit={handleSearch}>
                <input
                    type="text"
                    placeholder="搜索歌曲..."
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
                                <th>歌曲名</th>
                                <th>歌手</th>
                                <th>专辑</th>
                                <th>流派</th>
                                <th>时长</th>
                                <th>播放数</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            {songs.map(song => (
                                <tr key={song.id}>
                                    <td>{song.id}</td>
                                    <td>
                                        <div className="song-title">
                                            <FiMusic />
                                            {song.title}
                                        </div>
                                    </td>
                                    <td>{song.artist}</td>
                                    <td>{song.album || '-'}</td>
                                    <td>{song.genre || '-'}</td>
                                    <td>{formatDuration(song.duration || 0)}</td>
                                    <td>{(song.playCount || 0).toLocaleString()}</td>
                                    <td>
                                        <div className="action-buttons">
                                            <button 
                                                className="action-btn edit" 
                                                onClick={() => { setEditSong(song); setShowEditModal(true); }}
                                            >
                                                <FiEdit2 />
                                            </button>
                                            <button 
                                                className="action-btn delete" 
                                                onClick={() => handleDelete(song.id)}
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

            {showUploadModal && (
                <div className="modal-overlay">
                    <div className="modal">
                        <h2>上传新歌曲</h2>
                        <form onSubmit={handleUpload}>
                            <div className="file-upload" onClick={() => fileInputRef.current?.click()}>
                                <FiUpload size={32} />
                                <span>{uploadFile ? uploadFile.name : '点击选择音乐文件'}</span>
                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    accept="audio/*"
                                    onChange={(e) => setUploadFile(e.target.files[0])}
                                    style={{ display: 'none' }}
                                />
                            </div>
                            <input
                                type="text"
                                placeholder="歌曲名 *"
                                value={uploadData.title}
                                onChange={(e) => setUploadData({ ...uploadData, title: e.target.value })}
                                required
                            />
                            <input
                                type="text"
                                placeholder="歌手 *"
                                value={uploadData.artist}
                                onChange={(e) => setUploadData({ ...uploadData, artist: e.target.value })}
                                required
                            />
                            <input
                                type="text"
                                placeholder="专辑"
                                value={uploadData.album}
                                onChange={(e) => setUploadData({ ...uploadData, album: e.target.value })}
                            />
                            <input
                                type="text"
                                placeholder="流派"
                                value={uploadData.genre}
                                onChange={(e) => setUploadData({ ...uploadData, genre: e.target.value })}
                            />
                            <input
                                type="number"
                                placeholder="时长（秒）"
                                value={uploadData.duration}
                                onChange={(e) => setUploadData({ ...uploadData, duration: parseInt(e.target.value) || 0 })}
                            />
                            <div className="modal-actions">
                                <button type="button" onClick={() => setShowUploadModal(false)}>取消</button>
                                <button type="submit" className="btn-primary" disabled={uploading}>
                                    {uploading ? '上传中...' : '上传'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {showEditModal && editSong && (
                <div className="modal-overlay">
                    <div className="modal">
                        <h2>编辑歌曲</h2>
                        <form onSubmit={handleEdit}>
                            <input
                                type="text"
                                placeholder="歌曲名"
                                value={editSong.title}
                                onChange={(e) => setEditSong({ ...editSong, title: e.target.value })}
                                required
                            />
                            <input
                                type="text"
                                placeholder="歌手"
                                value={editSong.artist}
                                onChange={(e) => setEditSong({ ...editSong, artist: e.target.value })}
                                required
                            />
                            <input
                                type="text"
                                placeholder="专辑"
                                value={editSong.album || ''}
                                onChange={(e) => setEditSong({ ...editSong, album: e.target.value })}
                            />
                            <input
                                type="text"
                                placeholder="流派"
                                value={editSong.genre || ''}
                                onChange={(e) => setEditSong({ ...editSong, genre: e.target.value })}
                            />
                            <div className="modal-actions">
                                <button type="button" onClick={() => setShowEditModal(false)}>取消</button>
                                <button type="submit" className="btn-primary">保存</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Songs;
