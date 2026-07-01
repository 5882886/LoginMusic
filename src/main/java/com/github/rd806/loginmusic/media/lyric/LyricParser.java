package com.github.rd806.loginmusic.media.lyric;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.media.music.MusicEntry;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    // 从音乐条目加载歌词
    public static String loadLyrics(MusicEntry entry) {
        if (entry == null || entry.getLyric() == null || entry.getLyric().isEmpty()) {
            return null;
        }

        String lyrics = loadFromFile(entry.getLyric());
        if (lyrics == null) {
            lyrics = loadFromUrl(entry.getLyricUrl());
        }
        return lyrics;
    }

    // 从本地文件加载歌词
    private static String loadFromFile(String filePath) {
        try {
            LoginMusic.LOGGER.info("Loading Lyric from {}", filePath);

            Path path = Paths.get(filePath);

            // 如果是相对路径，尝试从配置目录查找
            if (!path.isAbsolute()) {
                Path configPath = LoginMusic.LYRICS_DIR.resolve(filePath);
                if (Files.exists(configPath)) {
                    path = configPath;
                }
            }

            if (!Files.exists(path)) {
                LoginMusic.LOGGER.info("No lyrics file found: {}, try url.", path);
                return null;
            }

            String lyricContent = Files.readString(path);
            LoginMusic.LOGGER.info("Lyric file loaded");
            return lyricContent;

        } catch (Exception e) {
            LoginMusic.LOGGER.error("Error while loading Lyric from {}", filePath, e);
            return null;
        }
    }

    // 从url加载歌词
    private static String loadFromUrl(String urlStr) {
        try {
            HttpURLConnection connection;

            URI uri = new URI(urlStr);
            URL url = uri.toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "LoginMusic");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) { return null; }

            InputStream inputStream = connection.getInputStream();
            // 使用 ByteArrayOutputStream 一次性读取所有数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];  // 使用更大的缓冲区
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            byte[] allBytes = baos.toByteArray();
            // 检测编码
            String charset = detectCharset(allBytes);
            LoginMusic.LOGGER.info("Lyric file loaded from {}, using charset {}", urlStr,  charset);
            // 转换为字符串
            return new String(allBytes, charset);
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Error while loading Lyric from {}", urlStr, e);
            return null;
        }
    }


    // 检测字符编码（增强版）
    private static String detectCharset(byte[] data) {
        if (data == null || data.length == 0) { return "UTF-8"; }
        // 检测 UTF-8 BOM
        if (data.length >= 3 && data[0] == (byte) 0xEF && data[1] == (byte) 0xBB && data[2] == (byte) 0xBF) { return "UTF-8"; }
        // 检测 UTF-16 BE BOM
        if (data.length >= 2 && data[0] == (byte) 0xFE && data[1] == (byte) 0xFF) { return "UTF-16BE"; }
        // 检测 UTF-16 LE BOM
        if (data.length >= 2 && data[0] == (byte) 0xFF && data[1] == (byte) 0xFE) { return "UTF-16LE"; }
        // 尝试判断是否为 GBK/GB2312
        // 简单检测：如果存在非 UTF-8 序列的字节，则认为是 GBK
        boolean isAscii = true;
        boolean hasChineseByte = false;
        // 只检测前1KB
        for (int i = 0; i < data.length && i < 1024; i++) {
            byte b = data[i];
            if (b < 0) {
                isAscii = false;
                // 检测是否可能是 GBK 编码（GBK 首字节范围 0x81-0xFE）
                if ((b & 0xFF) >= 0x81 && (b & 0xFF) <= 0xFE) { hasChineseByte = true; }
            }
        }

        if (isAscii) { return "US-ASCII"; }
        // 尝试 UTF-8 解码，检查是否有无效序列
        if (isValidUtf8(data)) { return "UTF-8"; }
        // 默认返回 GBK（中文环境下常见）
        return hasChineseByte ? "GBK" : "UTF-8";
    }

    // 检查是否为有效的 UTF-8 编码
    private static boolean isValidUtf8(byte[] data) {
        int i = 0;
        while (i < data.length) {
            byte b = data[i];
            if ((b & 0x80) == 0) {
                // ASCII 字符，1字节
                i++;
            } else if ((b & 0xE0) == 0xC0) {
                // 2字节 UTF-8
                if (i + 1 >= data.length) return false;
                if ((data[i+1] & 0xC0) != 0x80) return false;
                i += 2;
            } else if ((b & 0xF0) == 0xE0) {
                // 3字节 UTF-8
                if (i + 2 >= data.length) return false;
                if ((data[i+1] & 0xC0) != 0x80) return false;
                if ((data[i+2] & 0xC0) != 0x80) return false;
                i += 3;
            } else if ((b & 0xF8) == 0xF0) {
                // 4字节 UTF-8
                if (i + 3 >= data.length) return false;
                if ((data[i+1] & 0xC0) != 0x80) return false;
                if ((data[i+2] & 0xC0) != 0x80) return false;
                if ((data[i+3] & 0xC0) != 0x80) return false;
                i += 4;
            } else {
                // 无效的 UTF-8 序列
                return false;
            }
        }
        return true;
    }

    // 异步加载歌词
    public static CompletableFuture<String> loadLyricAsync(MusicEntry entry) {
        return CompletableFuture.supplyAsync(() -> loadLyrics(entry));
    }
}