package com.rd806.loginmusic.media.music;

import java.util.Objects;

public class MusicEntry {
    private String id;
    private String musicUrl;
    private String music;
    private String lyric;
    private String lyricUrl;

    // 默认构造函数
    // 防止因配置文件缺少部分字段而无法进入游戏
    public MusicEntry() {
        this.id = "Default";
        this.music = "Default.mp3";
        this.musicUrl = "Default";
        this.lyric = "Default.lrc";
        this.lyricUrl = "Default";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMusicUrl() { return musicUrl; }
    public void setMusicUrl(String musicUrl) { this.musicUrl = musicUrl; }

    public String getMusic() { return music; }
    public void setMusic(String music) { this.music = music; }

    public String getLyric() { return lyric; }
    public void setLyric(String lyrics) { this.lyric = lyrics; }

    public String getLyricUrl() { return lyricUrl; }
    public void setLyricUrl(String lyricsUrl) {  this.lyricUrl = lyricsUrl; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (object == null || getClass() != object.getClass()) return false;

        MusicEntry that = (MusicEntry) object;
        return Objects.equals(id, that.id);
    }
}
