import { Link } from 'react-router-dom';
import { FaMusic, FaPlay } from 'react-icons/fa';
import './PlaylistCard.css';

function PlaylistCard({ playlist }) {
  return (
    <Link to={`/playlist/${playlist.id}`} className="playlist-card">
      <div className="playlist-cover">
        {playlist.coverUrl ? (
          <img src={playlist.coverUrl} alt={playlist.name} />
        ) : (
          <div className="default-cover">
            <FaMusic />
          </div>
        )}
        <div className="play-overlay">
          <FaPlay />
        </div>
      </div>
      <div className="playlist-info">
        <h4 className="playlist-name">{playlist.name}</h4>
        <p className="playlist-meta">
          {playlist.songCount || 0} 首歌曲 · {playlist.userName}
        </p>
      </div>
    </Link>
  );
}

export default PlaylistCard;
