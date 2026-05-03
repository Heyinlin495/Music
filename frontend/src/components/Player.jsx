import { useState, useEffect, useRef, useCallback } from 'react';
import { FaPlay, FaPause, FaStepBackward, FaStepForward, FaVolumeUp, FaVolumeMute, FaRandom, FaRedo, FaSyncAlt, FaMusic, FaAlignLeft, FaHeart, FaRegHeart } from 'react-icons/fa';
import { usePlayerStore } from '../store/playerStore';
import { useAuthStore } from '../store/authStore';
import { usersApi } from '../api';
import LyricsPanel from './LyricsPanel';
import './Player.css';

function Player() {
  const audioRef = useRef(null);
  const [audioError, setAudioError] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [showLyrics, setShowLyrics] = useState(false);
  const [isFavorite, setIsFavorite] = useState(false);
  const { isAuthenticated } = useAuthStore();
  const {
    currentSong,
    isPlaying,
    volume,
    progress,
    duration,
    repeat,
    shuffle,
    hidePlayer,
    setAudioRef,
    togglePlay,
    setIsPlaying,
    playNext,
    playPrev,
    setVolume,
    setProgress,
    setDuration,
    seekTo,
    cyclePlayMode,
  } = usePlayerStore();

  useEffect(() => {
    if (audioRef.current) {
      setAudioRef(audioRef.current);
    }
  }, [setAudioRef]);

  useEffect(() => {
    if (audioRef.current && currentSong) {
      setAudioError(false);
      setErrorMessage('');
      if (currentSong.fileUrl) {
        audioRef.current.src = currentSong.fileUrl;
        if (isPlaying) {
          audioRef.current.play().catch(err => {
            console.error('播放失败:', err);
            setAudioError(true);
            setErrorMessage('音频文件加载失败');
            setIsPlaying(false);
          });
        }
      } else if (currentSong.songmid) {
        // Use QQ Music streaming proxy
        audioRef.current.src = `/api/music/stream/${currentSong.id}`;
        if (isPlaying) {
          audioRef.current.play().catch(err => {
            console.error('QQ音乐播放失败:', err);
            setAudioError(true);
            setErrorMessage('QQ音乐源不可用，可能需要更新Key');
            setIsPlaying(false);
          });
        }
      } else {
        setAudioError(true);
        setErrorMessage('该歌曲没有可用的音频源');
        setIsPlaying(false);
      }
    }
  }, [currentSong]);

  useEffect(() => {
    if (audioRef.current && currentSong && (currentSong.fileUrl || currentSong.songmid)) {
      if (isPlaying) {
        audioRef.current.play().catch(err => {
          console.error('播放失败:', err);
          setAudioError(true);
        });
      } else {
        audioRef.current.pause();
      }
    }
  }, [isPlaying]);

  // Check if current song is favorited
  useEffect(() => {
    setIsFavorite(currentSong?.isFavorite || false);
  }, [currentSong]);

  const toggleFavorite = async () => {
    if (!isAuthenticated || !currentSong) return;
    try {
      if (isFavorite) {
        await usersApi.removeFavorite(currentSong.id);
      } else {
        await usersApi.addFavorite(currentSong.id);
      }
      setIsFavorite(!isFavorite);
    } catch (error) {
      console.error('Failed to toggle favorite:', error);
    }
  };

  const handleTimeUpdate = useCallback(() => {
    if (audioRef.current && !audioError) {
      setProgress(audioRef.current.currentTime);
    }
  }, [audioError]);

  const handleLoadedMetadata = useCallback(() => {
    if (audioRef.current) {
      setDuration(audioRef.current.duration);
    }
  }, []);

  const handleEnded = useCallback(() => {
    if (repeat === 'one') {
      audioRef.current.currentTime = 0;
      audioRef.current.play();
    } else {
      playNext();
    }
  }, [repeat, playNext]);

  const handleProgressClick = (e) => {
    const bar = e.currentTarget;
    const rect = bar.getBoundingClientRect();
    const percent = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    const totalDur = duration || currentSong?.duration || 0;
    const newTime = percent * totalDur;
    if (audioRef.current && totalDur > 0) {
      audioRef.current.currentTime = newTime;
      setProgress(newTime);
    }
  };

  const handleVolumeChange = (e) => {
    const newVolume = parseFloat(e.target.value);
    setVolume(newVolume);
    if (audioRef.current) {
      audioRef.current.volume = newVolume;
    }
  };

  const formatTime = (time) => {
    if (isNaN(time) || time === null) return '0:00';
    const minutes = Math.floor(time / 60);
    const seconds = Math.floor(time % 60);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  const totalDuration = duration || currentSong?.duration || 0;
  const progressPercent = totalDuration > 0 ? (progress / totalDuration) * 100 : 0;

  const audioElement = (
    <audio
      ref={audioRef}
      onTimeUpdate={handleTimeUpdate}
      onLoadedMetadata={handleLoadedMetadata}
      onEnded={handleEnded}
      onPlay={() => setIsPlaying(true)}
      onPause={() => setIsPlaying(false)}
      onError={() => {
        setAudioError(true);
        setErrorMessage('音频加载失败，请检查网络或音频源');
        setIsPlaying(false);
      }}
    />
  );

  if (hidePlayer) {
    return (
      <div style={{ display: 'none' }}>
        {audioElement}
      </div>
    );
  }

  if (!currentSong) {
    return (
      <div className="player player-empty">
        {audioElement}
        <FaMusic style={{ marginRight: '0.5rem', opacity: 0.5 }} />
        <p>选择一首歌曲开始播放</p>
      </div>
    );
  }

  return (
    <div className="player">
      {audioElement}

      <div className="player-song-info">
        <div className="player-cover">
          {currentSong.coverUrl ? (
            <img src={currentSong.coverUrl} alt={currentSong.title} />
          ) : (
            <div className="default-cover">
              <FaMusic />
            </div>
          )}
        </div>
        <div className="player-details">
          <h4>{currentSong.title}</h4>
          <p>{currentSong.artist}</p>
        </div>
      </div>

      <div className="player-controls">
        <div className="control-buttons">
          <button
            className={`control-btn mode-cycle-btn ${shuffle || repeat !== 'off' ? 'active' : ''} ${shuffle ? 'mode-shuffle' : repeat === 'one' ? 'mode-one' : ''}`}
            onClick={cyclePlayMode}
            title={shuffle ? '随机播放' : repeat === 'one' ? '单曲循环' : '顺序播放'}
          >
            {shuffle ? <FaRandom /> : repeat === 'one' ? <><FaRedo /><span className="repeat-one">1</span></> : <FaSyncAlt />}
          </button>
          <button className="control-btn" onClick={playPrev} title="上一首">
            <FaStepBackward />
          </button>
          <button className="control-btn play-btn" onClick={togglePlay}>
            {isPlaying ? <FaPause /> : <FaPlay />}
          </button>
          <button className="control-btn" onClick={playNext} title="下一首">
            <FaStepForward />
          </button>
        </div>

        {audioError ? (
          <div className="progress-container error-state">
            <span className="error-message">{errorMessage || '播放失败'}</span>
          </div>
        ) : (
          <div className="progress-container">
            <span className="time">{formatTime(progress)}</span>
            <div className="progress-bar-wrapper">
              <div className="progress-bar" onClick={handleProgressClick}>
                <div
                  className="progress-fill"
                  style={{ width: `${progressPercent}%` }}
                />
              </div>
            </div>
            <span className="time">{formatTime(totalDuration)}</span>
          </div>
        )}
      </div>

      <div className="player-volume">
        {isAuthenticated && (
          <button
            className={`favorite-btn ${isFavorite ? 'active' : ''}`}
            onClick={toggleFavorite}
            title={isFavorite ? '取消喜欢' : '喜欢'}
          >
            {isFavorite ? <FaHeart /> : <FaRegHeart />}
          </button>
        )}
        <button
          className={`lyrics-toggle-btn ${showLyrics ? 'active' : ''}`}
          onClick={() => setShowLyrics(!showLyrics)}
          title="歌词"
        >
          <FaAlignLeft />
        </button>
        {volume === 0 ? <FaVolumeMute /> : <FaVolumeUp />}
        <input
          type="range"
          min="0"
          max="1"
          step="0.01"
          value={volume}
          onChange={handleVolumeChange}
          className="volume-slider"
        />
      </div>

      {showLyrics && <LyricsPanel onClose={() => setShowLyrics(false)} />}
    </div>
  );
}

export default Player;
