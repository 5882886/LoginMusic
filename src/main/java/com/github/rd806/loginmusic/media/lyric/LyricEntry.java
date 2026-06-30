package com.github.rd806.loginmusic.media.lyric;

import org.jetbrains.annotations.NotNull;

public record LyricEntry(long time, String text) implements Comparable<LyricEntry> {

    // 比较LRC对象，按照时间戳升序排列
    @Override
    public int compareTo(@NotNull LyricEntry o) {
        return Long.compare(this.time, o.time);
    }

    // 只比较时间戳
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LyricEntry entry = (LyricEntry) o;
        return time == entry.time;
    }

    public long getTime() { return time; }
    public String getText() { return text; }
}
