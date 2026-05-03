import { FaPlay, FaHeart, FaRegHeart, FaEllipsisH } from 'react-icons/fa';
import { usePlayerStore } from '../store/playerStore';
import { useAuthStore } from '../store/authStore';
import { usersApi } from '../api';
import { useState, useEffect, useRef } from 'react';
import './SongCard.css';

function SongCard({ song, playlist, onFavoriteChange }) {
  const { playSong, currentSong, isPlaying } = usePlayerStore();
  const { isAuthenticated } = useAuthStore();
  const [isFavorite, setIsFavorite] = useState(song.isFavorite || false);
  const [showMenu, setShowMenu] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    setIsFavorite(song.isFavorite || false);
  }, [song.isFavorite]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setShowMenu(false);
      }
    };
    if (showMenu) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [showMenu]);

  const isCurrentSong = currentSong?.id === song.id;

  const handlePlay = () => {
    playSong(song, playlist);
  };

  const handleFavorite = async (e) => {
    e.stopPropagation();
    if (!isAuthenticated) return;

    try {
      if (isFavorite) {
        await usersApi.removeFavorite(song.id);
      } else {
        await usersApi.addFavorite(song.id);
      }
      setIsFavorite(!isFavorite);
      if (onFavoriteChange) {
        onFavoriteChange(song.id, !isFavorite);
      }
    } catch (error) {
      console.error('Failed to update favorite:', error);
    }
  };

  const handlePlayAction = (e) => {
    e.stopPropagation();
    playSong(song, playlist);
  };

  const formatDuration = (seconds) => {
    if (!seconds) return '--:--';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div
      className={`song-card ${isCurrentSong ? 'playing' : ''}`}
      onClick={handlePlay}
    >
      <div className="song-cover">
        {song.coverUrl ? (
          <img src={song.coverUrl} alt={song.title} />
        ) : (
          <div className="default-cover">
            <FaPlay />
          </div>
        )}
        <div className="play-overlay">
          {isCurrentSong && isPlaying ? (
            <div className="equalizer">
              <span></span><span></span><span></span>
            </div>
          ) : (
            <FaPlay />
          )}
        </div>
      </div>

      <div className="song-info">
        <h4 className="song-title">{song.title}</h4>
        <p className="song-artist">{song.artist}</p>
        {song.album && <p className="song-album">{song.album}</p>}
      </div>

      <div className="song-meta">
        <span className="song-duration">{formatDuration(song.duration)}</span>
        <span className="song-plays">{song.playCount?.toLocaleString() || 0} 次播放</span>
      </div>

      <div className="song-actions" onClick={(e) => e.stopPropagation()}>
        {isAuthenticated && (
          <button
            className={`action-btn favorite-btn ${isFavorite ? 'active' : ''}`}
            onClick={handleFavorite}
            title={isFavorite ? '取消收藏' : '收藏'}
          >
            {isFavorite ? <FaHeart /> : <FaRegHeart />}
          </button>
        )}
        <div className="menu-wrapper" ref={menuRef}>
          <button
            className="action-btn menu-btn"
            onClick={(e) => {
              e.stopPropagation();
              setShowMenu(!showMenu);
            }}
            title="更多"
          >
            <FaEllipsisH />
          </button>
          {showMenu && (
            <div className="song-menu">
              <button
                className="song-menu-item"
                onClick={(e) => {
                  e.stopPropagation();
                  setShowMenu(false);
                  handlePlayAction(e);
                }}
              >
                播放
              </button>
              <button
                className="song-menu-item"
                onClick={(e) => {
                  e.stopPropagation();
                  setShowMenu(false);
                  handleFavorite(e);
                }}
              >
                {isFavorite ? '取消收藏' : '收藏'}
              </button>
            </div>
          )}
        </div>
        <button
          className="action-btn play-btn"
          onClick={handlePlayAction}
          title="播放"
        >
          <FaPlay />
        </button>
      </div>
    </div>
  );
}

export default SongCard;
