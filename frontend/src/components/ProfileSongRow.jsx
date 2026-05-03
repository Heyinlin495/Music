import { FaPlay, FaTrashAlt, FaHeart } from 'react-icons/fa';
import { usePlayerStore } from '../store/playerStore';
import './ProfileSongRow.css';

function ProfileSongRow({ song, playlist, index, selectable, selected, onSelect, onDelete, deleteIcon, deleteBtnClassName }) {
  const { playSong, currentSong, isPlaying } = usePlayerStore();
  const isCurrentSong = currentSong?.id === song.id;

  const formatDuration = (seconds) => {
    if (!seconds) return '--:--';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handlePlay = () => {
    playSong(song, playlist);
  };

  return (
    <div
      className={`profile-song-row ${isCurrentSong ? 'playing' : ''} ${selected ? 'selected' : ''}`}
      onClick={() => !selectable && handlePlay()}
    >
      {selectable && (
        <div className="profile-song-checkbox" onClick={(e) => e.stopPropagation()}>
          <input
            type="checkbox"
            checked={selected}
            onChange={() => onSelect?.(song.id)}
          />
        </div>
      )}

      <div className="profile-song-cover" onClick={selectable ? () => onSelect?.(song.id) : handlePlay}>
        {song.coverUrl ? (
          <img src={song.coverUrl} alt={song.title} />
        ) : (
          <div className="profile-song-default-cover">
            <FaPlay />
          </div>
        )}
        {isCurrentSong && isPlaying && (
          <div className="profile-song-playing-badge">
            <div className="mini-equalizer">
              <span></span><span></span><span></span>
            </div>
          </div>
        )}
      </div>

      <div className="profile-song-info">
        <span className="profile-song-title">{song.title}</span>
        <span className="profile-song-artist">{song.artist}</span>
      </div>

      <span className="profile-song-duration">{formatDuration(song.duration)}</span>

      {onDelete ? (
        <button
          className={`profile-song-delete-btn${deleteBtnClassName ? ` ${deleteBtnClassName}` : ''}`}
          onClick={(e) => {
            e.stopPropagation();
            onDelete(song.id);
          }}
        >
          {deleteIcon || <FaTrashAlt />}
        </button>
      ) : (
        !selectable && (
          <button
            className="profile-song-select-btn"
            onClick={(e) => {
              e.stopPropagation();
              handlePlay();
            }}
          >
            选择
          </button>
        )
      )}
    </div>
  );
}

export default ProfileSongRow;
