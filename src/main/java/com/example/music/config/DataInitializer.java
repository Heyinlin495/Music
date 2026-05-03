package com.example.music.config;

import com.example.music.entity.*;
import com.example.music.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("开始初始化数据...");

        // 创建管理员用户
        User adminUser = createAdminUser();

        // 创建演示用户
        User demoUser = createDemoUser();

        // 创建测试用户
        User testUser = createTestUser();

        // 检查是否需要初始化歌曲数据
        if (songRepository.count() == 0 && demoUser != null) {
            List<Song> songs = createDemoSongs(demoUser);
            createDemoPlaylists(demoUser, songs);
            createDemoComments(demoUser, testUser, songs);
        }

        log.info("数据初始化完成!");
    }

    private User createAdminUser() {
        if (userRepository.existsByUsername("admin")) {
            log.info("管理员用户已存在");
            return userRepository.findByUsername("admin").orElse(null);
        }

        User adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@music.com")
                .nickname("系统管理员")
                .bio("系统管理员账号")
                .role(Role.ADMIN)
                .build();
        userRepository.save(adminUser);
        log.info("创建管理员用户: admin / admin123");
        return adminUser;
    }

    private User createDemoUser() {
        if (userRepository.existsByUsername("demo")) {
            log.info("演示用户已存在");
            return userRepository.findByUsername("demo").orElse(null);
        }

        User demoUser = User.builder()
                .username("demo")
                .password(passwordEncoder.encode("demo123"))
                .email("demo@music.com")
                .nickname("音乐爱好者")
                .bio("热爱音乐，分享快乐")
                .role(Role.USER)
                .build();
        userRepository.save(demoUser);
        log.info("创建演示用户: demo / demo123");
        return demoUser;
    }

    private User createTestUser() {
        if (userRepository.existsByUsername("test")) {
            log.info("测试用户已存在");
            return userRepository.findByUsername("test").orElse(null);
        }

        User testUser = User.builder()
                .username("test")
                .password(passwordEncoder.encode("test123"))
                .email("test@music.com")
                .nickname("测试用户")
                .bio("测试账号")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);
        log.info("创建测试用户: test / test123");
        return testUser;
    }

    private List<Song> createDemoSongs(User uploader) {
        List<Song> songs = new ArrayList<>();

        // 经典摇滚
        songs.add(createSong("Bohemian Rhapsody", "Queen", "A Night at the Opera", "摇滚", 354, uploader));
        songs.add(createSong("Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", "摇滚", 482, uploader));
        songs.add(createSong("Hotel California", "Eagles", "Hotel California", "摇滚", 391, uploader));
        songs.add(createSong("Sweet Child O Mine", "Guns N Roses", "Appetite for Destruction", "摇滚", 356, uploader));
        songs.add(createSong("Smells Like Teen Spirit", "Nirvana", "Nevermind", "摇滚", 301, uploader));
        songs.add(createSong("Back In Black", "AC/DC", "Back In Black", "摇滚", 255, uploader));
        songs.add(createSong("November Rain", "Guns N Roses", "Use Your Illusion I", "摇滚", 537, uploader));

        // 欧美流行
        songs.add(createSong("Shape of You", "Ed Sheeran", "Divide", "流行", 234, uploader));
        songs.add(createSong("Blinding Lights", "The Weeknd", "After Hours", "流行", 200, uploader));
        songs.add(createSong("Billie Jean", "Michael Jackson", "Thriller", "流行", 294, uploader));
        songs.add(createSong("Bad Guy", "Billie Eilish", "When We All Fall Asleep", "流行", 194, uploader));
        songs.add(createSong("Uptown Funk", "Bruno Mars", "Uptown Special", "流行", 270, uploader));
        songs.add(createSong("Rolling in the Deep", "Adele", "21", "流行", 228, uploader));
        songs.add(createSong("Someone Like You", "Adele", "21", "流行", 285, uploader));
        songs.add(createSong("Hello", "Adele", "25", "流行", 295, uploader));
        songs.add(createSong("Thinking Out Loud", "Ed Sheeran", "X", "流行", 281, uploader));
        songs.add(createSong("Perfect", "Ed Sheeran", "Divide", "流行", 263, uploader));
        songs.add(createSong("Stay With Me", "Sam Smith", "In The Lonely Hour", "流行", 172, uploader));
        songs.add(createSong("Havana", "Camila Cabello", "Camila", "流行", 217, uploader));
        songs.add(createSong("Shallow", "Lady Gaga & Bradley Cooper", "A Star Is Born", "流行", 216, uploader));
        songs.add(createSong("Dance Monkey", "Tones and I", "The Kids Are Coming", "流行", 210, uploader));
        songs.add(createSong("Watermelon Sugar", "Harry Styles", "Fine Line", "流行", 174, uploader));
        songs.add(createSong("Levitating", "Dua Lipa", "Future Nostalgia", "流行", 203, uploader));
        songs.add(createSong("drivers license", "Olivia Rodrigo", "SOUR", "流行", 242, uploader));
        songs.add(createSong("As It Was", "Harry Styles", "Harry's House", "流行", 167, uploader));
        songs.add(createSong("Anti-Hero", "Taylor Swift", "Midnights", "流行", 200, uploader));

        // 古典音乐
        songs.add(createSong("月光奏鸣曲", "贝多芬", "古典精选", "古典", 360, uploader));
        songs.add(createSong("致爱丽丝", "贝多芬", "古典精选", "古典", 180, uploader));
        songs.add(createSong("四季·春", "维瓦尔第", "四季", "古典", 320, uploader));
        songs.add(createSong("卡农序曲", "帕赫贝尔", "古典精选", "古典", 330, uploader));
        songs.add(createSong("蓝色多瑙河", "施特劳斯", "圆舞曲精选", "古典", 285, uploader));

        // 华语流行 - 周杰伦
        songs.add(createSong("晴天", "周杰伦", "叶惠美", "华语流行", 269, uploader));
        songs.add(createSong("稻香", "周杰伦", "魔杰座", "华语流行", 223, uploader));
        songs.add(createSong("七里香", "周杰伦", "七里香", "华语流行", 299, uploader));
        songs.add(createSong("告白气球", "周杰伦", "Jay", "华语流行", 215, uploader));
        songs.add(createSong("简单爱", "周杰伦", "依然范特西", "华语流行", 270, uploader));
        songs.add(createSong("夜曲", "周杰伦", "叶惠美", "华语流行", 226, uploader));
        songs.add(createSong("青花瓷", "周杰伦", "我很忙", "华语流行", 239, uploader));
        songs.add(createSong("安静", "周杰伦", "范特西", "华语流行", 305, uploader));
        songs.add(createSong("反方向的钟", "周杰伦", "依然范特西", "华语流行", 236, uploader));

        // 华语流行 - 其他歌手
        songs.add(createSong("平凡之路", "朴树", "猎户星座", "华语流行", 295, uploader));
        songs.add(createSong("起风了", "买辣椒也用券", "起风了", "华语流行", 325, uploader));
        songs.add(createSong("小幸运", "田馥甄", "小幸运", "华语流行", 294, uploader));
        songs.add(createSong("大鱼", "周深", "大鱼海棠", "华语流行", 316, uploader));
        songs.add(createSong("光年之外", "G.E.M.邓紫棋", "光年之外", "华语流行", 235, uploader));
        songs.add(createSong("爱情转移", "陈奕迅", "认了吧", "华语流行", 220, uploader));
        songs.add(createSong("后来", "刘若英", "我等你", "华语流行", 341, uploader));
        songs.add(createSong("成都", "赵雷", "无法长大", "华语流行", 291, uploader));

        // 电子音乐
        songs.add(createSong("Faded", "Alan Walker", "Different World", "电子", 212, uploader));
        songs.add(createSong("Alone", "Alan Walker", "Different World", "电子", 161, uploader));
        songs.add(createSong("Wake Me Up", "Avicii", "True", "电子", 247, uploader));
        songs.add(createSong("Titanium", "David Guetta ft. Sia", "Nothing But The Beat", "电子", 245, uploader));
        songs.add(createSong("Animals", "Martin Garrix", "Animals", "电子", 187, uploader));

        // 韩流
        songs.add(createSong("Dynamite", "BTS", "BE", "韩流", 199, uploader));
        songs.add(createSong("Butter", "BTS", "Butter", "韩流", 164, uploader));
        songs.add(createSong("How You Like That", "BLACKPINK", "THE ALBUM", "韩流", 190, uploader));
        songs.add(createSong("Kill This Love", "BLACKPINK", "KILL THIS LOVE", "韩流", 190, uploader));

        // 民谣
        songs.add(createSong("想见你", "朴树", "生如夏花", "民谣", 283, uploader));
        songs.add(createSong("南山南", "马頔", "南山南", "民谣", 320, uploader));
        songs.add(createSong("安河桥", "宋冬野", "安河桥北", "民谣", 295, uploader));

        // 华语流行 - 新增热门
        songs.add(createSong("漠河舞厅", "柳爽", "漠河舞厅", "华语流行", 264, uploader));
        songs.add(createSong("孤勇者", "陈奕迅", "孤勇者", "华语流行", 262, uploader));
        songs.add(createSong("错位时空", "艾辰", "错位时空", "华语流行", 213, uploader));
        songs.add(createSong("白月光与朱砂痣", "大籽", "白月光与朱砂痣", "华语流行", 228, uploader));
        songs.add(createSong("踏山河", "是七叔呢", "踏山河", "华语流行", 195, uploader));
        songs.add(createSong("星辰大海", "黄霄雲", "星辰大海", "华语流行", 252, uploader));
        songs.add(createSong("下山", "要不要买菜", "下山", "华语流行", 203, uploader));
        songs.add(createSong("少年", "梦然", "少年", "华语流行", 225, uploader));
        songs.add(createSong("可能", "程响", "可能", "华语流行", 290, uploader));
        songs.add(createSong("半生雪", "是七叔呢", "半生雪", "华语流行", 208, uploader));
        songs.add(createSong("送你一朵小红花", "赵英俊", "送你一朵小红花", "华语流行", 261, uploader));
        songs.add(createSong("这世界那么多人", "莫文蔚", "这世界那么多人", "华语流行", 277, uploader));
        songs.add(createSong("如愿", "王菲", "如愿", "华语流行", 312, uploader));
        songs.add(createSong("世间美好与你环环相扣", "柏松", "世间美好与你环环相扣", "华语流行", 254, uploader));
        songs.add(createSong("飞鸟和蝉", "任然", "飞鸟和蝉", "华语流行", 280, uploader));
        songs.add(createSong("你的答案", "阿冗", "你的答案", "华语流行", 264, uploader));

        // 华语流行 - 经典补充
        songs.add(createSong("红豆", "王菲", "唱游", "华语流行", 296, uploader));
        songs.add(createSong("漂洋过海来看你", "李宗盛", "作品李宗盛", "华语流行", 306, uploader));
        songs.add(createSong("突然好想你", "五月天", "后青春期的诗", "华语流行", 330, uploader));
        songs.add(createSong("倔强", "五月天", "神的孩子都在跳舞", "华语流行", 264, uploader));
        songs.add(createSong("光辉岁月", "Beyond", "命运派对", "华语流行", 283, uploader));
        songs.add(createSong("真的爱你", "Beyond", "Beyond IV", "华语流行", 281, uploader));
        songs.add(createSong("吻别", "张学友", "吻别", "华语流行", 306, uploader));
        songs.add(createSong("一路上有你", "张学友", "吻别", "华语流行", 296, uploader));
        songs.add(createSong("童话", "光良", "童话", "华语流行", 311, uploader));
        songs.add(createSong("遇见", "孙燕姿", "遇见", "华语流行", 258, uploader));
        songs.add(createSong("绿光", "孙燕姿", "Start", "华语流行", 224, uploader));

        // 欧美流行 - 新增热门
        songs.add(createSong("Flowers", "Miley Cyrus", "Endless Summer Vacation", "流行", 200, uploader));
        songs.add(createSong("Cruel Summer", "Taylor Swift", "Lover", "流行", 178, uploader));
        songs.add(createSong("Vampire", "Olivia Rodrigo", "GUTS", "流行", 219, uploader));
        songs.add(createSong("Espresso", "Sabrina Carpenter", "Espresso", "流行", 175, uploader));
        songs.add(createSong("Paint The Town Red", "Doja Cat", "Scarlet", "流行", 212, uploader));
        songs.add(createSong("Snooze", "SZA", "SOS", "流行", 202, uploader));
        songs.add(createSong("Kill Bill", "SZA", "SOS", "流行", 153, uploader));

        // 嘻哈/说唱
        songs.add(createSong("Lose Yourself", "Eminem", "8 Mile", "嘻哈", 326, uploader));
        songs.add(createSong("Rap God", "Eminem", "The Marshall Mathers LP 2", "嘻哈", 364, uploader));
        songs.add(createSong("HUMBLE.", "Kendrick Lamar", "DAMN.", "嘻哈", 177, uploader));
        songs.add(createSong("God's Plan", "Drake", "Scorpion", "嘻哈", 198, uploader));

        log.info("创建了 {} 首示例歌曲", songs.size());
        return songs;
    }

    private Song createSong(String title, String artist, String album, String genre, int duration, User uploader) {
        Song song = Song.builder()
                .title(title)
                .artist(artist)
                .album(album)
                .genre(genre)
                .duration(duration)
                .uploader(uploader)
                .playCount((long) (Math.random() * 50000))
                .build();
        return songRepository.save(song);
    }

    private void createDemoPlaylists(User user, List<Song> songs) {
        // 摇滚精选歌单
        Playlist rockPlaylist = Playlist.builder()
                .name("经典摇滚精选")
                .description("收录经典摇滚金曲，带你感受摇滚的魅力")
                .user(user)
                .isPublic(true)
                .songs(songs.stream().filter(s -> "摇滚".equals(s.getGenre())).toList())
                .build();
        playlistRepository.save(rockPlaylist);

        // 流行热门歌单
        Playlist popPlaylist = Playlist.builder()
                .name("流行热门榜")
                .description("最新最热的流行音乐")
                .user(user)
                .isPublic(true)
                .songs(songs.stream().filter(s -> "流行".equals(s.getGenre())).toList())
                .build();
        playlistRepository.save(popPlaylist);

        // 华语精选歌单
        Playlist chinesePlaylist = Playlist.builder()
                .name("华语金曲")
                .description("精选华语经典歌曲")
                .user(user)
                .isPublic(true)
                .songs(songs.stream().filter(s -> "华语流行".equals(s.getGenre())).toList())
                .build();
        playlistRepository.save(chinesePlaylist);

        // 电子音乐歌单
        Playlist electronicPlaylist = Playlist.builder()
                .name("电子舞曲")
                .description("最燃的电子音乐合集")
                .user(user)
                .isPublic(true)
                .songs(songs.stream().filter(s -> "电子".equals(s.getGenre())).toList())
                .build();
        playlistRepository.save(electronicPlaylist);

        // 韩流精选歌单
        Playlist kpopPlaylist = Playlist.builder()
                .name("韩流精选")
                .description("热门韩国流行音乐")
                .user(user)
                .isPublic(true)
                .songs(songs.stream().filter(s -> "韩流".equals(s.getGenre())).toList())
                .build();
        playlistRepository.save(kpopPlaylist);

        // 古典音乐歌单
        Playlist classicalPlaylist = Playlist.builder()
                .name("古典乐章")
                .description("经典古典音乐，陶冶情操")
                .user(user)
                .isPublic(true)
                .songs(songs.stream().filter(s -> "古典".equals(s.getGenre())).toList())
                .build();
        playlistRepository.save(classicalPlaylist);

        // 嘻哈说唱歌单
        Playlist hiphopPlaylist = Playlist.builder()
                .name("嘻哈说唱")
                .description("最火的嘻哈说唱合集")
                .user(user)
                .isPublic(true)
                .songs(songs.stream().filter(s -> "嘻哈".equals(s.getGenre())).toList())
                .build();
        playlistRepository.save(hiphopPlaylist);

        log.info("创建了 7 个示例歌单");
    }

    private void createDemoComments(User user1, User user2, List<Song> songs) {
        if (songs.isEmpty()) return;

        String[] comments = {
                "太好听了，单曲循环中！",
                "经典永流传",
                "每次听都有不同的感觉",
                "强烈推荐！",
                "这首歌陪伴了我的青春",
                "旋律太美了",
                "百听不厌的神曲"
        };

        int commentCount = 0;
        for (int i = 0; i < Math.min(10, songs.size()); i++) {
            Song song = songs.get(i);

            // 用户1评论
            Comment comment1 = Comment.builder()
                    .content(comments[i % comments.length])
                    .user(user1)
                    .song(song)
                    .build();
            commentRepository.save(comment1);
            commentCount++;

            // 用户2评论（如果存在）
            if (user2 != null && i % 2 == 0) {
                Comment comment2 = Comment.builder()
                        .content(comments[(i + 1) % comments.length])
                        .user(user2)
                        .song(song)
                        .build();
                commentRepository.save(comment2);
                commentCount++;
            }
        }

        log.info("创建了 {} 条示例评论", commentCount);
    }
}
