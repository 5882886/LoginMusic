package com.github.rd806.loginmusic.media.music;

import java.util.Objects;

public class MusicEntry {
    private String id;
    private String musicName;
    private String musicPath;
    private String lyricName;
    private String lyricPath;

    // 默认构造函数
    // 防止因配置文件缺少部分字段而无法进入游戏
    public MusicEntry() {
        this.id = "Default";
        this.musicName = "Default.mp3";
        this.musicPath = "Default.mp3";
        this.lyricName = "Default.lrc";
        this.lyricPath = "Default.lrc";
    }

    public MusicEntry(String id, String musicName, String musicPath, String lyricName, String lyricPath) {
        this.id = id;
        this.musicName = musicName;
        this.musicPath = musicPath;
        this.lyricName = lyricName;
        this.lyricPath = lyricPath;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMusicName() { return musicName; }
    public void setMusicName(String music) { this.musicName = music; }

    public String getMusicPath() { return musicPath; }
    public void setMusicPath(String musicPath) { this.musicPath = musicPath; }

    public String getLyricName() { return lyricName; }
    public void setLyricName(String lyrics) { this.lyricName = lyrics; }

    public String getLyricPath() { return lyricPath; }
    public void setLyricPath(String lyricsUrl) {  this.lyricPath = lyricsUrl; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (object == null || getClass() != object.getClass()) return false;

        MusicEntry that = (MusicEntry) object;
        return Objects.equals(id, that.id);
    }
}
