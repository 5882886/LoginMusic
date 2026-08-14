package com.github.rd806.loginmusic.media.lyric;

import com.github.rd806.loginmusic.LoginMusic;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 解析歌词文件
public class LyricParser {

    private static final Pattern TIME_TAG_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})]");

    // 解析LRC歌词文本
    public static List<LyricEntry> parseLRC(String lrcContent) {
        List<LyricEntry> lyrics = new ArrayList<>();

        if (lrcContent == null || lrcContent.isEmpty()) { return lyrics; }

        try (BufferedReader reader = new BufferedReader(new StringReader(lrcContent))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, lyrics);
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Error while parsing LRC content: ", e);
        }
        // 歌词按照时间升序排列
        Collections.sort(lyrics);
        return lyrics;
    }

    // 解析单行歌词
    private static void parseLine(String line, List<LyricEntry> lyrics) {
        Matcher matcher = TIME_TAG_PATTERN.matcher(line);
        boolean hasTimeTag = false;
        while (matcher.find()) {
            hasTimeTag = true;
            try {
                // 提取时间信息
                long time = getTime(matcher);
                // 提取歌词文本（去除所有时间标签）
                String text = matcher.replaceAll("").trim();
                if (!text.isEmpty()) {
                    lyrics.add(new LyricEntry(time, text));
                }
            } catch (NumberFormatException e) {
                LoginMusic.LOGGER.error("Error while parsing LRC content: {}", line);
            }
        }

        // 处理没有时间标签的行（通常不会出现，但为了健壮性）
        if (!hasTimeTag && !line.trim().isEmpty()) {
            // 可以忽略或作为元数据
            LoginMusic.LOGGER.debug("No time tag, skipping {}", line);
        }
    }

    // 提取时间戳
    private static long getTime(Matcher matcher) {
        int minutes = Integer.parseInt(matcher.group(1));
        int seconds = Integer.parseInt(matcher.group(2));
        String msStr = matcher.group(3);
        // 处理毫秒（可能是2位或3位）
        long milliseconds;
        if (msStr.length() == 2) {
            milliseconds = Long.parseLong(msStr) * 10;
        } else {
            milliseconds = Long.parseLong(msStr);
        }
        return (minutes * 60L + seconds) * 1000 + milliseconds;
    }

    // 获取当前时间对应的歌词
    public static LyricEntry getCurrentLyric(List<LyricEntry> lyrics, long currentTime) {
        if (lyrics == null || lyrics.isEmpty()) {
            return new LyricEntry(0, "Lyrics unavailable");
        }
        LyricEntry current = null;
        for (LyricEntry lyric : lyrics) {
            if (lyric.getTime() <= currentTime) {
                current = lyric;
            } else {
                break;
            }
        }
        return current;
    }
}
