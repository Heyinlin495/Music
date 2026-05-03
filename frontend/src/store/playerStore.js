import { create } from 'zustand';
import { songsApi } from '../api';

export const usePlayerStore = create((set, get) => ({
  currentSong: null,
  playlist: [],
  currentIndex: 0,
  isPlaying: false,
  volume: 0.7,
  progress: 0,
  duration: 0,
  repeat: 'off', // off, one, all
  shuffle: false,
  audioRef: null,
  shuffleHistory: [], // 随机播放历史栈，用于上一首回退
  shufflePlayed: new Set(), // 已播放的歌曲id，避免重复
  hidePlayer: false, // 是否隐藏播放器（Profile页面用）

  setAudioRef: (ref) => set({ audioRef: ref }),
  setHidePlayer: (hide) => set({ hidePlayer: hide }),

  // 内部方法：获取下一首随机索引（不重复播放同一首）
  _getNextShuffleIndex: () => {
    const { playlist, currentIndex, shufflePlayed } = get();
    if (playlist.length <= 1) return 0;

    // 收集还没播放过的索引
    const available = [];
    for (let i = 0; i < playlist.length; i++) {
      if (i !== currentIndex && !shufflePlayed.has(playlist[i].id)) {
        available.push(i);
      }
    }

    // 如果所有歌都播过了，重置记录，排除当前歌曲
    if (available.length === 0) {
      const resetAvailable = [];
      for (let i = 0; i < playlist.length; i++) {
        if (i !== currentIndex) {
          resetAvailable.push(i);
        }
      }
      set({ shufflePlayed: new Set([playlist[currentIndex]?.id]) });
      return resetAvailable[Math.floor(Math.random() * resetAvailable.length)];
    }

    return available[Math.floor(Math.random() * available.length)];
  },

  playSong: async (song, playlist = null) => {
    const state = get();

    // 如果点击的是当前正在播放的歌曲，切换播放/暂停
    if (state.currentSong?.id === song.id) {
      get().togglePlay();
      return;
    }

    if (playlist) {
      const index = playlist.findIndex(s => s.id === song.id);
      set({
        playlist,
        currentIndex: index >= 0 ? index : 0,
        // 切换播放列表时重置随机状态
        shuffleHistory: [],
        shufflePlayed: new Set(),
      });
    }

    // 记录到随机历史
    const newHistory = [...state.shuffleHistory, state.currentSong?.id].filter(Boolean);

    set({
      currentSong: song,
      isPlaying: true,
      progress: 0,
      duration: song.duration || 0,
      shuffleHistory: newHistory,
      shufflePlayed: new Set([...state.shufflePlayed, song.id]),
    });

    // 记录播放
    try {
      await songsApi.play(song.id);
    } catch (error) {
      console.error('记录播放失败:', error);
    }
  },

  togglePlay: () => {
    const { audioRef, isPlaying, currentSong } = get();
    if (!currentSong) return;

    if (audioRef && (currentSong.fileUrl || currentSong.songmid)) {
      if (isPlaying) {
        audioRef.pause();
      } else {
        audioRef.play().catch(console.error);
      }
    }
    set({ isPlaying: !isPlaying });
  },

  setIsPlaying: (playing) => set({ isPlaying: playing }),

  playNext: () => {
    const { playlist, currentIndex, shuffle, repeat } = get();
    if (playlist.length === 0) return;

    let nextIndex;
    if (shuffle) {
      nextIndex = get()._getNextShuffleIndex();
    } else {
      nextIndex = currentIndex + 1;
      if (nextIndex >= playlist.length) {
        if (repeat === 'all') {
          nextIndex = 0;
        } else {
          set({ isPlaying: false });
          return;
        }
      }
    }

    const nextSong = playlist[nextIndex];
    if (!nextSong) return;

    // 记录到随机历史
    const newHistory = [...get().shuffleHistory, playlist[currentIndex]?.id].filter(Boolean);

    set({
      currentIndex: nextIndex,
      currentSong: nextSong,
      isPlaying: true,
      progress: 0,
      duration: nextSong?.duration || 0,
      shuffleHistory: newHistory,
      shufflePlayed: new Set([...get().shufflePlayed, nextSong.id]),
    });

    songsApi.play(nextSong.id).catch(console.error);
  },

  playPrev: () => {
    const { playlist, currentIndex, shuffle, shuffleHistory } = get();
    if (playlist.length === 0) return;

    let prevIndex;
    if (shuffle && shuffleHistory.length > 0) {
      // 随机模式：从历史栈中弹出上一首
      const prevSongId = shuffleHistory[shuffleHistory.length - 1];
      const historyIndex = playlist.findIndex(s => s.id === prevSongId);
      prevIndex = historyIndex >= 0 ? historyIndex : 0;
      set({ shuffleHistory: shuffleHistory.slice(0, -1) });
    } else {
      // 顺序模式：上一首
      prevIndex = currentIndex - 1;
      if (prevIndex < 0) {
        prevIndex = playlist.length - 1;
      }
    }

    const prevSong = playlist[prevIndex];
    if (!prevSong) return;

    set({
      currentIndex: prevIndex,
      currentSong: prevSong,
      isPlaying: true,
      progress: 0,
      duration: prevSong?.duration || 0,
    });

    songsApi.play(prevSong.id).catch(console.error);
  },

  setVolume: (volume) => {
    const { audioRef } = get();
    if (audioRef) {
      audioRef.volume = volume;
    }
    set({ volume });
  },

  setProgress: (progress) => set({ progress }),
  setDuration: (duration) => set({ duration }),

  seekTo: (time) => {
    const { audioRef } = get();
    if (audioRef) {
      audioRef.currentTime = time;
    }
    set({ progress: time });
  },

  toggleRepeat: () => {
    const { repeat } = get();
    const modes = ['off', 'all', 'one'];
    const nextIndex = (modes.indexOf(repeat) + 1) % modes.length;
    set({ repeat: modes[nextIndex] });
  },

  toggleShuffle: () => {
    const { shuffle } = get();
    if (shuffle) {
      set({ shuffle: false, shuffleHistory: [], shufflePlayed: new Set() });
    } else {
      set({ shuffle: true, shuffleHistory: [], shufflePlayed: new Set() });
    }
  },

  // 循环切换播放模式：随机播放 → 顺序播放 → 单曲循环
  cyclePlayMode: () => {
    const { shuffle, repeat } = get();
    if (shuffle) {
      // 随机 → 顺序
      set({ shuffle: false, repeat: 'off', shuffleHistory: [], shufflePlayed: new Set() });
    } else if (repeat === 'off') {
      // 顺序 → 单曲循环
      set({ repeat: 'one' });
    } else {
      // 单曲循环 → 随机
      set({ repeat: 'off', shuffle: true, shuffleHistory: [], shufflePlayed: new Set() });
    }
  },

  // 获取当前播放模式
  getPlayMode: () => {
    const { shuffle, repeat } = get();
    if (shuffle) return 'shuffle';
    if (repeat === 'one') return 'one';
    return 'sequential';
  },

  clearPlaylist: () => set({
    playlist: [],
    currentSong: null,
    currentIndex: 0,
    isPlaying: false,
    progress: 0,
    duration: 0,
    shuffleHistory: [],
    shufflePlayed: new Set(),
  }),
}));
