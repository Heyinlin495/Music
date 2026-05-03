import { Link } from 'react-router-dom';
import { getGenreGradient } from '../utils/genreColors';
import './GenreCard.css';

function GenreCard({ genre, songCount, coverUrl }) {
  return (
    <Link
      to={`/genre/${encodeURIComponent(genre)}`}
      className="genre-card"
      style={{ background: getGenreGradient(genre) }}
    >
      {coverUrl && <img className="genre-card-bg" src={coverUrl} alt="" />}
      <div className="genre-card-overlay">
        <div className="genre-card-info">
          <h3 className="genre-card-name">{genre}</h3>
          <span className="genre-card-count">{songCount} 首歌曲</span>
        </div>
      </div>
    </Link>
  );
}

export default GenreCard;
