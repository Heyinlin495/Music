import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { FaPlay } from 'react-icons/fa';
import { songsApi } from '../api';
import { usePlayerStore } from '../store/playerStore';
import { getGenreGradient } from '../utils/genreColors';
import SongCard from '../components/SongCard';
import './GenrePage.css';

function GenrePage() {
  const { genre } = useParams();
  const decodedGenre = decodeURIComponent(genre);
  const { playSong } = usePlayerStore();

  const [songs, setSongs] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  useEffect(() => {
    setSongs([]);
    setPage(0);
    setHasMore(true);
    setLoading(true);
    loadSongs(0, true);
  }, [decodedGenre]);

  const loadSongs = async (pageNum, reset = false) => {
    try {
      if (pageNum > 0) setLoadingMore(true);
      const res = await songsApi.getByGenre(decodedGenre, pageNum);
      const data = res.data.data;
      const content = data?.content || [];
      setSongs(prev => reset ? content : [...prev, ...content]);
      setHasMore(!data?.last);
      setPage(pageNum);
    } catch (error) {
      console.error('Failed to load genre songs:', error);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  const handlePlayAll = () => {
    if (songs.length > 0) {
      playSong(songs[0], songs);
    }
  };

  if (loading) {
    return (
      <div className="genre-page loading">
        <div className="spinner"></div>
        <p>加载中...</p>
      </div>
    );
  }

  return (
    <div className="genre-page">
      <div className="genre-header" style={{ background: getGenreGradient(decodedGenre) }}>
        <div className="genre-header-content">
          <span className="genre-header-label">分类歌单</span>
          <h1>{decodedGenre}</h1>
          <p className="genre-header-meta">{songs.length}+ 首歌曲</p>
          <button className="play-all-btn" onClick={handlePlayAll} disabled={songs.length === 0}>
            <FaPlay /> 播放全部
          </button>
        </div>
      </div>

      <div className="genre-songs">
        {songs.length > 0 ? (
          <div className="songs-list">
            {songs.map((song) => (
              <SongCard key={song.id} song={song} playlist={songs} />
            ))}
          </div>
        ) : (
          <p className="empty-message">该分类暂无歌曲</p>
        )}

        {hasMore && (
          <button
            className="load-more-btn"
            onClick={() => loadSongs(page + 1)}
            disabled={loadingMore}
          >
            {loadingMore ? '加载中...' : '加载更多'}
          </button>
        )}
      </div>
    </div>
  );
}

export default GenrePage;
