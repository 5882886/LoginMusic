package com.rd806.loginmusic.media.music;

import java.util.Objects;

public class MusicEntry {
    private String id;
    private String musicUrl;
    private String name;
    private String lyrics;
    private String lyricsUrl;

    // 默认构造函数
    // 防止因配置文件缺少部分字段而无法进入游戏
    public MusicEntry() {
        this.id = "Default";
        this.musicUrl = "Default";
        this.name = "Default.mp3";
        this.lyrics = "Default.lrc";
        this.lyricsUrl = "Default";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMusicUrl() { return musicUrl; }
    public void setMusicUrl(String musicUrl) { this.musicUrl = musicUrl; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }

    public String getLyricsUrl() { return lyricsUrl; }
    public void setLyricsUrl(String lyricsUrl) {  this.lyricsUrl = lyricsUrl; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (object == null || getClass() != object.getClass()) return false;

        MusicEntry that = (MusicEntry) object;
        return Objects.equals(id, that.id);
    }
}
