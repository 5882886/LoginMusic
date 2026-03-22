package com.rd806.loginmusic.music;

import java.util.Objects;

public class MusicEntry {
    private String id;
    private String url;
    private String name;
    private String lyrics;

    public MusicEntry() {}

    public MusicEntry(String id, String url, String name, String lyrics) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.lyrics = lyrics;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (object == null || getClass() != object.getClass()) return false;

        MusicEntry that = (MusicEntry) object;
        return Objects.equals(id, that.id);
    }

    public int HashCode() {
        return Objects.hash(id);
    }
}
