import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { FaSearch } from 'react-icons/fa';
import { songsApi } from '../api';
import SongCard from '../components/SongCard';
import './Search.css';

function Search() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get('q') || '');
  const [songs, setSongs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [page, setPage] = useState(0);

  useEffect(() => {
    const q = searchParams.get('q');
    if (q) {
      setKeyword(q);
      searchSongs(q, 0);
    }
  }, [searchParams]);

  const searchSongs = async (query, pageNum) => {
    if (!query.trim()) return;
    
    setLoading(true);
    try {
      const response = await songsApi.search(query, pageNum);
      const data = response.data.data;
      
      if (pageNum === 0) {
        setSongs(data.content || []);
      } else {
        setSongs((prev) => [...prev, ...(data.content || [])]);
      }
      
      setHasMore(!data.last);
      setPage(pageNum);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (keyword.trim()) {
      setSearchParams({ q: keyword });
    }
  };

  const loadMore = () => {
    if (!loading && hasMore) {
      searchSongs(keyword, page + 1);
    }
  };

  return (
    <div className="search-page">
      <div className="search-header">
        <h1>搜索音乐</h1>
        <form className="search-form" onSubmit={handleSearch}>
          <FaSearch className="search-icon" />
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="搜索歌曲、歌手..."
            className="search-input"
            autoComplete="off"
          />
        </form>
      </div>

      <div className="search-results">
        {loading && songs.length === 0 ? (
          <div className="loading">
            <div className="spinner"></div>
            <p>搜索中...</p>
          </div>
        ) : songs.length > 0 ? (
          <>
            <div className="results-header">
              <h2>搜索结果</h2>
            </div>
            <div className="songs-list">
              {songs.map((song) => (
                <SongCard key={song.id} song={song} playlist={songs} />
              ))}
            </div>
            {hasMore && (
              <button 
                className="load-more-btn" 
                onClick={loadMore}
                disabled={loading}
              >
                {loading ? '加载中...' : '加载更多'}
              </button>
            )}
          </>
        ) : keyword && !loading ? (
          <div className="no-results">
            <p>没有找到相关歌曲</p>
            <span>尝试搜索其他关键词</span>
          </div>
        ) : (
          <div className="search-hint">
            <FaSearch className="hint-icon" />
            <p>输入关键词搜索你喜欢的音乐</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default Search;
