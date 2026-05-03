import { useState, useEffect, useRef, useMemo } from 'react';
import { FaTimes, FaMusic } from 'react-icons/fa';
import { musicApi } from '../api';
import { usePlayerStore } from '../store/playerStore';
import './LyricsPanel.css';

function isPinyinLine(text) {
  // Check if the line is pure pinyin/romanization (no Chinese characters)
  // Contains only ASCII letters, spaces, digits, and common punctuation
  if (!text) return false;
  return /^[a-zA-Z0-9\s,.:;!?'"()\-À-ɏ]+$/.test(text) && /[a-zA-Z]/.test(text);
}

function parseLRC(lrcText) {
  if (!lrcText) return [];
  const lines = lrcText.split('\n');
  const result = [];
  const timeRegex = /\[(\d{2}):(\d{2})\.(\d{2,3})\]/g;

  for (const line of lines) {
    const times = [];
    let match;
    while ((match = timeRegex.exec(line)) !== null) {
      const min = parseInt(match[1], 10);
      const sec = parseInt(match[2], 10);
      const ms = parseInt(match[3].padEnd(3, '0'), 10);
      times.push(min * 60 + sec + ms / 1000);
    }
    const text = line.replace(/\[\d{2}:\d{2}\.\d{2,3}\]/g, '').trim();
    if (times.length > 0 && text && !isPinyinLine(text)) {
      for (const time of times) {
        result.push({ time, text });
      }
    }
  }

  result.sort((a, b) => a.time - b.time);
  return result;
}

function LyricsPanel({ onClose }) {
  const { currentSong, progress } = usePlayerStore();
  const [lyrics, setLyrics] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const lyricsListRef = useRef(null);
  const activeLineRef = useRef(null);

  useEffect(() => {
    if (!currentSong) return;

    setLoading(true);
    setError('');
    setLyrics([]);

    // Try fetching from QQ Music API first, fall back to song's own lyrics field
    musicApi.getLyrics(currentSong.id)
      .then(res => {
        const lrcText = res.data?.lyric;
        if (lrcText) {
          setLyrics(parseLRC(lrcText));
        } else if (currentSong.lyrics) {
          // Plain text lyrics (from upload)
          setLyrics([{ time: 0, text: currentSong.lyrics }]);
        } else {
          setError('暂无歌词');
        }
      })
      .catch(() => {
        // Fall back to song's stored lyrics
        if (currentSong.lyrics) {
          setLyrics([{ time: 0, text: currentSong.lyrics }]);
        } else {
          setError('暂无歌词');
        }
      })
      .finally(() => setLoading(false));
  }, [currentSong?.id]);

  const activeIndex = useMemo(() => {
    if (lyrics.length === 0) return -1;
    let idx = -1;
    for (let i = 0; i < lyrics.length; i++) {
      if (progress >= lyrics[i].time) {
        idx = i;
      } else {
        break;
      }
    }
    return idx;
  }, [lyrics, progress]);

  useEffect(() => {
    if (activeLineRef.current && lyricsListRef.current) {
      activeLineRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'center',
      });
    }
  }, [activeIndex]);

  return (
    <div className="lyrics-panel">
      <div className="lyrics-panel-header">
        <button className="lyrics-close-btn" onClick={onClose}>
          <FaTimes />
        </button>
      </div>

      <div className="lyrics-panel-content">
        <div className="lyrics-song-info">
          <div className="lyrics-cover">
            {currentSong?.coverUrl ? (
              <img src={currentSong.coverUrl} alt={currentSong.title} />
            ) : (
              <div className="lyrics-default-cover">
                <FaMusic />
              </div>
            )}
          </div>
          <h3>{currentSong?.title}</h3>
          <p>{currentSong?.artist}</p>
        </div>

        <div className="lyrics-list" ref={lyricsListRef}>
          {loading && <div className="lyrics-status">加载歌词中...</div>}
          {error && !loading && <div className="lyrics-status">{error}</div>}
          {!loading && !error && lyrics.length > 0 && (
            lyrics.map((line, index) => (
              <p
                key={index}
                ref={index === activeIndex ? activeLineRef : null}
                className={`lyrics-line ${index === activeIndex ? 'active' : ''}`}
              >
                {line.text}
              </p>
            ))
          )}
          {!loading && !error && lyrics.length === 0 && (
            <div className="lyrics-status">暂无歌词</div>
          )}
        </div>
      </div>
    </div>
  );
}

export default LyricsPanel;
