import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { FaPlay, FaSearch, FaHeart, FaMusic, FaChartLine, FaCalendarAlt, FaChevronDown, FaChevronUp } from 'react-icons/fa';
import { songsApi, playlistsApi } from '../api';
import { usePlayerStore } from '../store/playerStore';
import { useAuthStore } from '../store/authStore';
import { formatDuration, formatPlayCount } from '../utils/formatters';
import './Home.css';

function Home() {
  const navigate = useNavigate();
  const [searchKeyword, setSearchKeyword] = useState('');
  const [featuredPlaylists, setFeaturedPlaylists] = useState([]);
  const [randomSongs, setRandomSongs] = useState([]);
  const [dailySongs, setDailySongs] = useState([]);
  const [topSongs, setTopSongs] = useState([]);
  const [latestSongs, setLatestSongs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedSection, setExpandedSection] = useState(null);
  const { playSong, currentSong } = usePlayerStore();
  const { playlistVersion } = useAuthStore();
  const location = useLocation();

  useEffect(() => {
    loadData();
  }, [location, playlistVersion]);

  const loadData = async () => {
    try {
      const [featuredRes, dailyRes, randomRes, topRes, latestRes] = await Promise.all([
        playlistsApi.getFeatured(6).catch(() => ({ data: { data: [] } })),
        songsApi.getDaily(10).catch(() => ({ data: { data: [] } })),
        songsApi.getRandom(10).catch(() => ({ data: { data: [] } })),
        songsApi.getTop(10).catch(() => ({ data: { data: [] } })),
        songsApi.getLatest(10).catch(() => ({ data: { data: [] } })),
      ]);

      setFeaturedPlaylists(featuredRes.data.data || []);
      setDailySongs(dailyRes.data.data || []);
      setRandomSongs(randomRes.data.data || []);
      setTopSongs(topRes.data.data || []);
      setLatestSongs(latestRes.data.data || []);
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchKeyword.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchKeyword.trim())}`);
    }
  };

  const toggleSection = (section) => {
    setExpandedSection(expandedSection === section ? null : section);
  };

  const coverGradients = [
    'linear-gradient(135deg, #1a3a5c, #2d6a9f)',
    'linear-gradient(135deg, #2d1b4e, #6b3fa0)',
    'linear-gradient(135deg, #1b3d2f, #2d8a5e)',
    'linear-gradient(135deg, #4a1942, #c2185b)',
    'linear-gradient(135deg, #1a2744, #3d6cb9)',
    'linear-gradient(135deg, #3e2723, #bf360c)',
  ];

  const renderSongItem = (song, index, playlist, prefix = '') => {
    const isCurrent = currentSong?.id === song.id;
    return (
      <div
        key={`${prefix}${song.id}`}
        className={`home-song-item ${isCurrent ? 'playing' : ''}`}
        onClick={() => playSong(song, playlist)}
      >
        <span className="home-song-item-index">
          {String(index + 1).padStart(2, '0')}
        </span>
        <div className="home-song-item-cover">
          {song.coverUrl ? (
            <img src={song.coverUrl} alt={song.title} />
          ) : (
            <div className="home-song-item-cover-default">
              <FaMusic />
            </div>
          )}
          <button className="home-song-item-play">
            <FaPlay />
          </button>
        </div>
        <div className="home-song-item-info">
          <div className="home-song-item-title">{song.title}</div>
          <div className="home-song-item-artist">{song.artist}</div>
        </div>
        <div className="home-song-item-duration">
          {song.duration ? formatDuration(song.duration) : '-'}
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="home loading">
        <div className="spinner"></div>
        <p>加载中...</p>
      </div>
    );
  }

  return (
    <div className="home">
      <div className="home-hero-text" aria-hidden="true">
        <div className="hero-char-wrap">
          {['He', 'yin', 'lin', '的', '快', '乐', '星', '球'].map((ch, i) => (
            <span
              key={i}
              className="hero-char"
              style={{
                animationDelay: `${i * 0.12}s, ${i * 0.12}s, ${i * 0.25}s`,
              }}
            >{ch}</span>
          ))}
        </div>
      </div>

      {/* Search Bar */}
      <form className="home-search" onSubmit={handleSearch}>
        <FaSearch className="home-search-icon" />
        <input
          type="text"
          placeholder="搜索歌曲、歌手、专辑..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          autoComplete="off"
        />
      </form>

      {/* Banner Quick Links */}
      <div className="home-banner">
        {/* 每日推荐 - dailySongs */}
        <div className="home-banner-group">
          <div
            className={`home-banner-card ${expandedSection === 'daily' ? 'expanded' : ''}`}
            onClick={() => toggleSection('daily')}
          >
            <div className="home-banner-icon">
              <FaCalendarAlt />
            </div>
            <div className="home-banner-text">
              <div className="home-banner-title">每日推荐</div>
              <div className="home-banner-desc">每日精选好歌</div>
            </div>
            <div className="home-banner-arrow">
              {expandedSection === 'daily' ? <FaChevronUp /> : <FaChevronDown />}
            </div>
          </div>
          {expandedSection === 'daily' && dailySongs.length > 0 && (
            <div className="home-banner-expanded">
              {dailySongs.map((song, index) => renderSongItem(song, index, dailySongs, 'daily-'))}
            </div>
          )}
        </div>

        {/* 猜你喜欢 - randomSongs */}
        <div className="home-banner-group">
          <div
            className={`home-banner-card ${expandedSection === 'guess' ? 'expanded' : ''}`}
            onClick={() => toggleSection('guess')}
          >
            <div className="home-banner-icon">
              <FaHeart />
            </div>
            <div className="home-banner-text">
              <div className="home-banner-title">猜你喜欢</div>
              <div className="home-banner-desc">发现更多好歌</div>
            </div>
            <div className="home-banner-arrow">
              {expandedSection === 'guess' ? <FaChevronUp /> : <FaChevronDown />}
            </div>
          </div>
          {expandedSection === 'guess' && randomSongs.length > 0 && (
            <div className="home-banner-expanded">
              {randomSongs.map((song, index) => renderSongItem(song, index, randomSongs, 'guess-'))}
            </div>
          )}
        </div>

        {/* 新歌 - latestSongs */}
        <div className="home-banner-group">
          <div
            className={`home-banner-card ${expandedSection === 'new' ? 'expanded' : ''}`}
            onClick={() => toggleSection('new')}
          >
            <div className="home-banner-icon">
              <FaMusic />
            </div>
            <div className="home-banner-text">
              <div className="home-banner-title">新歌</div>
              <div className="home-banner-desc">最新音乐首发</div>
            </div>
            <div className="home-banner-arrow">
              {expandedSection === 'new' ? <FaChevronUp /> : <FaChevronDown />}
            </div>
          </div>
          {expandedSection === 'new' && latestSongs.length > 0 && (
            <div className="home-banner-expanded">
              {latestSongs.map((song, index) => renderSongItem(song, index, latestSongs, 'new-'))}
            </div>
          )}
        </div>

        {/* 排行 */}
        <div className="home-banner-group">
          <div
            className={`home-banner-card ${expandedSection === 'rank' ? 'expanded' : ''}`}
            onClick={() => toggleSection('rank')}
          >
            <div className="home-banner-icon">
              <FaChartLine />
            </div>
            <div className="home-banner-text">
              <div className="home-banner-title">排行</div>
              <div className="home-banner-desc">热门歌曲榜单</div>
            </div>
            <div className="home-banner-arrow">
              {expandedSection === 'rank' ? <FaChevronUp /> : <FaChevronDown />}
            </div>
          </div>
          {expandedSection === 'rank' && topSongs.length > 0 && (
            <div className="home-banner-expanded">
              {topSongs.map((song, index) => renderSongItem(song, index, topSongs, 'rank-'))}
            </div>
          )}
        </div>
      </div>

      {/* 推荐歌单 - Only Playlists */}
      <section className="home-section">
        <div className="home-section-header">
          <h2 className="home-section-title">推荐歌单</h2>
          <button className="home-section-more" onClick={() => navigate('/playlists')}>
            更多 &gt;
          </button>
        </div>
        <div className="home-playlist-grid">
          {featuredPlaylists.slice(0, 6).map((playlist, index) => (
            <div
              key={playlist.id}
              className="home-playlist-card"
              onClick={() => navigate(`/playlist/${playlist.id}`)}
            >
              <div
                className="home-playlist-cover"
                style={{
                  background: playlist.coverUrl
                    ? undefined
                    : coverGradients[index % coverGradients.length],
                }}
              >
                {playlist.coverUrl ? (
                  <img src={playlist.coverUrl} alt={playlist.name} />
                ) : (
                  <div className="home-playlist-cover-icon">
                    <FaMusic />
                  </div>
                )}
                <div className="home-playlist-play-count">
                  <FaPlay />
                  {playlist.playCount != null ? formatPlayCount(playlist.playCount) : '0'}
                </div>
                <button
                  className="home-playlist-play"
                  onClick={(e) => {
                    e.stopPropagation();
                    navigate(`/playlist/${playlist.id}`);
                  }}
                >
                  <FaPlay />
                </button>
              </div>
              <div className="home-playlist-info">
                <h3 className="home-playlist-name">{playlist.name}</h3>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

export default Home;
