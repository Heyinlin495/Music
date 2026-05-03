import { useState, useEffect } from 'react';
import { FaPlay, FaMusic, FaChevronLeft, FaChevronRight } from 'react-icons/fa';
import { songsApi } from '../api';
import { usePlayerStore } from '../store/playerStore';
import { formatDuration } from '../utils/formatters';
import './AllSongs.css';

function AllSongs() {
  const [songs, setSongs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const pageSize = 20;
  const { playSong, currentSong } = usePlayerStore();

  useEffect(() => {
    loadSongs(page);
  }, [page]);

  const loadSongs = async (p) => {
    setLoading(true);
    try {
      const res = await songsApi.getAll(p, pageSize);
      const data = res.data.data;
      setSongs(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (error) {
      console.error('Failed to load songs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handlePlay = (song) => {
    playSong(song, songs);
  };

  const pageNumbers = [];
  const maxVisible = 7;
  let start = Math.max(0, page - Math.floor(maxVisible / 2));
  let end = Math.min(totalPages, start + maxVisible);
  if (end - start < maxVisible) {
    start = Math.max(0, end - maxVisible);
  }
  for (let i = start; i < end; i++) {
    pageNumbers.push(i);
  }

  if (loading && songs.length === 0) {
    return (
      <div className="all-songs loading">
        <div className="spinner"></div>
        <p>加载中...</p>
      </div>
    );
  }

  return (
    <div className="all-songs">
      <div className="all-songs-header">
        <h1>所有歌曲</h1>
        <span className="all-songs-count">共 {totalElements} 首</span>
      </div>

      <div className="all-songs-list">
        <div className="all-songs-list-head">
          <span className="all-songs-col-index">#</span>
          <span className="all-songs-col-cover"></span>
          <span className="all-songs-col-title">歌曲</span>
          <span className="all-songs-col-artist">歌手</span>
          <span className="all-songs-col-duration">时长</span>
        </div>
        {songs.map((song, index) => {
          const isCurrent = currentSong?.id === song.id;
          const globalIndex = page * pageSize + index + 1;
          return (
            <div
              key={song.id}
              className={`all-songs-row ${isCurrent ? 'playing' : ''}`}
              onClick={() => handlePlay(song)}
            >
              <span className="all-songs-col-index">
                {isCurrent ? <FaPlay className="all-songs-playing-icon" /> : String(globalIndex).padStart(2, '0')}
              </span>
              <div className="all-songs-col-cover">
                {song.coverUrl ? (
                  <img src={song.coverUrl} alt={song.title} />
                ) : (
                  <div className="all-songs-cover-default"><FaMusic /></div>
                )}
              </div>
              <div className="all-songs-col-title">
                <div className="all-songs-title">{song.title}</div>
              </div>
              <span className="all-songs-col-artist">{song.artist}</span>
              <span className="all-songs-col-duration">
                {song.duration ? formatDuration(song.duration) : '-'}
              </span>
            </div>
          );
        })}
      </div>

      {totalPages > 1 && (
        <div className="all-songs-pagination">
          <button
            className="all-songs-page-btn"
            disabled={page === 0}
            onClick={() => setPage(p => p - 1)}
          >
            <FaChevronLeft />
          </button>
          {start > 0 && <span className="all-songs-page-ellipsis">...</span>}
          {pageNumbers.map(p => (
            <button
              key={p}
              className={`all-songs-page-btn ${p === page ? 'active' : ''}`}
              onClick={() => setPage(p)}
            >
              {p + 1}
            </button>
          ))}
          {end < totalPages && <span className="all-songs-page-ellipsis">...</span>}
          <button
            className="all-songs-page-btn"
            disabled={page === totalPages - 1}
            onClick={() => setPage(p => p + 1)}
          >
            <FaChevronRight />
          </button>
        </div>
      )}
    </div>
  );
}

export default AllSongs;
