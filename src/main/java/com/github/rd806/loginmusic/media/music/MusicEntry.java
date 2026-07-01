package com.github.rd806.loginmusic.media.music;

import java.util.Objects;

public class MusicEntry {
    private String id;
    private String music;
    private String musicUrl;
    private String lyric;
    private String lyricUrl;

    // 默认构造函数
    // 防止因配置文件缺少部分字段而无法进入游戏
    public MusicEntry() {
        this.id = "Default";
        this.music = "Default.mp3";
        this.musicUrl = "Default";
        this.lyric = "Default.lrc";
        this.lyricUrl = "Default.lrc";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMusicUrl() { return musicUrl; }
    public void setMusicUrl(String musicUrl) { this.musicUrl = musicUrl; }

    public String getMusic() { return music; }
    public void setMusic(String name) { this.music = name; }

    public String getLyric() { return lyric; }
    public void setLyric(String lyric) { this.lyric = lyric; }

    public String getLyricUrl() { return lyricUrl; }
    public void setLyricUrl(String lyricUrl) { this.lyricUrl = lyricUrl; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (object == null || getClass() != object.getClass()) return false;

        MusicEntry that = (MusicEntry) object;
        return Objects.equals(id, that.id);
    }
}
