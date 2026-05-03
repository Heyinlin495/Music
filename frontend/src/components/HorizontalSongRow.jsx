import { Link } from 'react-router-dom';
import { FaPlay, FaChevronRight } from 'react-icons/fa';
import { usePlayerStore } from '../store/playerStore';
import './HorizontalSongRow.css';

function HorizontalSongRow({ title, songs, seeAllLink }) {
  const { playSong, currentSong, isPlaying } = usePlayerStore();

  if (!songs || songs.length === 0) return null;

  const handlePlay = (song) => {
    playSong(song, songs);
  };

  return (
    <section className="hsr-section">
      <div className="hsr-header">
        <h2>{title}</h2>
        {seeAllLink && (
          <Link to={seeAllLink} className="hsr-see-all">
            查看全部 <FaChevronRight />
          </Link>
        )}
      </div>
      <div className="hsr-scroll">
        {songs.map((song) => {
          const isCurrent = currentSong?.id === song.id;
          return (
            <div
              key={song.id}
              className={`hsr-item ${isCurrent ? 'playing' : ''}`}
              onClick={() => handlePlay(song)}
            >
              <div className="hsr-item-cover">
                {song.coverUrl ? (
                  <img src={song.coverUrl} alt={song.title} />
                ) : (
                  <div className="hsr-item-cover-default">
                    <FaPlay />
                  </div>
                )}
                <div className="hsr-item-play-overlay">
                  <FaPlay />
                </div>
                {isCurrent && isPlaying && (
                  <div className="hsr-equalizer">
                    <span></span><span></span><span></span>
                  </div>
                )}
              </div>
              <div className="hsr-item-title">{song.title}</div>
              <div className="hsr-item-artist">{song.artist}</div>
            </div>
          );
        })}
      </div>
    </section>
  );
}

export default HorizontalSongRow;
