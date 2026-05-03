import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaUpload, FaMusic } from 'react-icons/fa';
import { songsApi } from '../api';
import { useAuthStore } from '../store/authStore';
import './Upload.css';

function Upload() {
  const { isAuthenticated } = useAuthStore();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    artist: '',
    album: '',
    genre: '',
    duration: '',
    lyrics: '',
  });
  const [musicFile, setMusicFile] = useState(null);
  const [coverFile, setCoverFile] = useState(null);
  const [coverPreview, setCoverPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    if (coverFile) {
      const url = URL.createObjectURL(coverFile);
      setCoverPreview(url);
      return () => URL.revokeObjectURL(url);
    } else {
      setCoverPreview(null);
    }
  }, [coverFile]);

  if (!isAuthenticated) {
    return null;
  }

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleMusicChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setMusicFile(file);
    }
  };

  const handleCoverChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setCoverFile(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!musicFile) {
      setError('请选择音乐文件');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const data = new FormData();
      data.append('file', musicFile);
      if (coverFile) {
        data.append('cover', coverFile);
      }
      data.append('title', formData.title);
      data.append('artist', formData.artist);
      if (formData.album) data.append('album', formData.album);
      if (formData.genre) data.append('genre', formData.genre);
      if (formData.duration) data.append('duration', formData.duration);
      if (formData.lyrics) data.append('lyrics', formData.lyrics);

      await songsApi.upload(data);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || '上传失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="upload-page">
      <div className="upload-card">
        <h1><FaUpload /> 上传音乐</h1>
        <p className="subtitle">与大家分享你的音乐</p>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="file-upload-section">
            <div className="file-upload">
              <input
                type="file"
                id="musicFile"
                accept="audio/*"
                onChange={handleMusicChange}
                hidden
              />
              <label htmlFor="musicFile" className="file-label">
                <FaMusic />
                <span>{musicFile ? musicFile.name : '选择音乐文件'}</span>
              </label>
            </div>
            <div className="file-upload cover">
              <input
                type="file"
                id="coverFile"
                accept="image/*"
                onChange={handleCoverChange}
                hidden
              />
              <label htmlFor="coverFile" className="file-label">
                {coverPreview ? (
                  <img src={coverPreview} alt="Cover" />
                ) : (
                  <>
                    <FaUpload />
                    <span>封面图片</span>
                  </>
                )}
              </label>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>歌曲名称 *</label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="请输入歌曲名称"
                required
              />
            </div>
            <div className="form-group">
              <label>歌手/艺术家 *</label>
              <input
                type="text"
                name="artist"
                value={formData.artist}
                onChange={handleChange}
                placeholder="请输入歌手名称"
                required
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>专辑</label>
              <input
                type="text"
                name="album"
                value={formData.album}
                onChange={handleChange}
                placeholder="专辑名称（可选）"
              />
            </div>
            <div className="form-group">
              <label>风格</label>
              <input
                type="text"
                name="genre"
                value={formData.genre}
                onChange={handleChange}
                placeholder="音乐风格（可选）"
              />
            </div>
          </div>

          <div className="form-group">
            <label>歌词</label>
            <textarea
              name="lyrics"
              value={formData.lyrics}
              onChange={handleChange}
              placeholder="歌词（可选）"
              rows="4"
            />
          </div>

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? '上传中...' : '上传音乐'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Upload;
