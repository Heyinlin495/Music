-- 初始化数据库脚本
-- 此脚本在 Docker 容器首次启动时自动执行

USE musicdb;

-- 创建索引（如果使用 ddl-auto=validate，需要手动创建索引）
-- 注意：如果使用 ddl-auto=update，JPA 会自动创建索引，此部分可选

-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_role ON users(role);

-- 歌曲表索引
CREATE INDEX IF NOT EXISTS idx_artist ON songs(artist);
CREATE INDEX IF NOT EXISTS idx_genre ON songs(genre);
CREATE INDEX IF NOT EXISTS idx_play_count ON songs(play_count);
CREATE INDEX IF NOT EXISTS idx_created_at ON songs(created_at);
CREATE INDEX IF NOT EXISTS idx_uploader_id ON songs(uploader_id);
CREATE INDEX IF NOT EXISTS idx_title_artist ON songs(title, artist);

-- 用户收藏表索引
CREATE INDEX IF NOT EXISTS idx_user_favorites_user_id ON user_favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_user_favorites_song_id ON user_favorites(song_id);
