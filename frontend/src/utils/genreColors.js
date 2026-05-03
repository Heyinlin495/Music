export const genreColors = {
  '摇滚': ['#e74c3c', '#c0392b'],
  '流行': ['#3498db', '#2980b9'],
  '华语流行': ['#e91e63', '#ad1457'],
  '古典': ['#8e44ad', '#6c3483'],
  '电子': ['#00b894', '#00cec9'],
  '嘻哈': ['#f39c12', '#e67e22'],
  '民谣': ['#27ae60', '#1e8449'],
  '韩流': ['#fd79a8', '#e84393'],
};

export function getGenreGradient(genre) {
  const colors = genreColors[genre];
  if (colors) {
    return `linear-gradient(135deg, ${colors[0]}, ${colors[1]})`;
  }
  return 'linear-gradient(135deg, #94a3b8, #64748b)';
}
