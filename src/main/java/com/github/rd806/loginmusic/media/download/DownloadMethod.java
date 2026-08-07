package com.github.rd806.loginmusic.media.download;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.media.SimpleMusicPlayer;
import com.github.rd806.loginmusic.media.lyric.LyricParser;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public class DownloadMethod {

    private static final Minecraft mc = Minecraft.getInstance();
    private static DownloadScreen downloadScreen;

    /* ----- 音乐下载方法 ----- */
    // 开始下载
    public static void startDownload(MusicEntry music, DownloadScreen screen) {
        downloadScreen = screen;
        // 加载音乐
        SimpleMusicPlayer.AUDIO_LOADER.submit(() -> {
            downloadMusic(music, (downloaded, total, progress) -> {
                Component status = Component.translatable(LoginMusic.MODID + ".gui.download.progress",
                            String.format("%.1f", downloaded / 1024.0 / 1024.0),
                            String.format("%.1f", total / 1024.0 / 1024.0));
                // 在主进程中更新进度
                if (screen != null) {
                    mc.execute(() -> screen.updateAudioProgress(progress, status));
                }
            });
            mc.execute(screen::setAudioCompleted);
        });
        // 加载歌词
        SimpleMusicPlayer.LYRIC_LOADER.submit(() -> {
            downloadLyrics(music, (downloaded, total, progress) -> {
                Component status;
                // 设置不同的提示信息
                status = Component.translatable(LoginMusic.MODID + ".gui.download.progress",
                        String.format("%.1f", downloaded / 1024.0 / 1024.0),
                        String.format("%.1f", total / 1024.0 / 1024.0));
                // 在主进程中更新进度
                if (screen != null) {
                    mc.execute(() -> screen.updateLyricProgress(progress, status));
                }
            });
            mc.execute(screen::setLyricCompleted);
        });
    }

    // 进度回调
    @FunctionalInterface
    private interface DownloadCallback {
        void onProgress(long downloadedBytes, long totalBytes, float progress);
    }

    private static URLConnection openConnection(String path) {
        try {
            URI uri = new URI(path);
            URL url = uri.toURL();
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "LoginMusic");
            return connection;
        } catch (Exception ex) {
            LoginMusic.LOGGER.error("Failed to open URL", ex);
        }
        return null;
    }

    // 音乐下载方法
    private static void downloadMusic(MusicEntry music, DownloadCallback callback) {
        String name = music.getMusicName();
        String urlStr = music.getMusicPath();
        try {
            Path cacheFile = LoginMusic.LYRICS_DIR.resolve(name);
            // 检查缓存，命中直接返回
            if (isDownloaded(cacheFile, callback)) {
                LoginMusic.LOGGER.info("The file has been downloaded!");
                PrepareMusic.audioInputStream = AudioSystem.getAudioInputStream(cacheFile.toFile());
                mc.execute(downloadScreen::setAudioCompleted);
                return;
            }
            LoginMusic.LOGGER.info("Start downloading music from {}", urlStr);
            // Java20 之后不再使用 URL() 方法
            URLConnection connection = openConnection(urlStr);
            // 获取文件大小
            if (connection == null) { return; }
            long totalBytes = connection.getContentLengthLong();
            LoginMusic.LOGGER.info("Music size: {}", totalBytes);

            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long downloadedBytes = 0;
            // 加载文件
            if (ClientConfig.ALLOW_DOWNLOAD.get()) {
                try (OutputStream out = Files.newOutputStream(cacheFile)) {
                    // 下载文件
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;
                        // 回调进度
                        if (callback != null) {
                            float progress = totalBytes > 0 ? downloadedBytes / (float) totalBytes : 0;
                            callback.onProgress(downloadedBytes, totalBytes, progress);
                        }
                    }
                    PrepareMusic.audioInputStream = AudioSystem.getAudioInputStream(cacheFile.toFile());
                    LoginMusic.LOGGER.info("Downloading music completed, total {} bytes", downloadedBytes);
                }
            } else {
                ByteArrayOutputStream baos = MusicConfig.get(urlStr);
                if (baos.size() == 0) {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        // 写入到输出流，保存所有数据
                        baos.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;
                        // 回调进度
                        if (callback != null) {
                            float progress = totalBytes > 0 ? downloadedBytes / (float) totalBytes : 0;
                            callback.onProgress(downloadedBytes, totalBytes, progress);
                        }
                    }
                    MusicConfig.put(urlStr, baos);
                }
                // 从 ByteArrayOutputStream 获取完整的字节数组
                byte[] allData = baos.toByteArray();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(allData);
                PrepareMusic.audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream);
                mc.execute(downloadScreen::setAudioCompleted);
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("Downloading music error!", e);
            mc.execute(() -> downloadScreen.setError("Downloading error!"));
        }
    }

    // 歌词下载方法
    private static void downloadLyrics(MusicEntry music, DownloadCallback callback) {
        String name = music.getLyricName();
        String urlStr = music.getLyricPath();
        try {
            Path cacheFile = LoginMusic.LYRICS_DIR.resolve(name);
            // 检查缓存，命中直接返回
            if (isDownloaded(cacheFile, callback)) {
                try {
                    LyricParser.lyricContent = Files.readString(cacheFile);
                    LoginMusic.LOGGER.info("Lyric file loaded");
                    mc.execute(downloadScreen::setLyricCompleted);
                    return;
                } catch (Exception e) {
                    LoginMusic.LOGGER.error("Error while loading Lyric from {}", urlStr, e);
                    return;
                }
            }
            LoginMusic.LOGGER.info("Start downloading lyrics from {}", urlStr);
            URLConnection connection = openConnection(urlStr);
            if (connection == null) { return; }
            // 获取文件大小和类型
            long totalBytes = connection.getContentLengthLong();
            LoginMusic.LOGGER.info("Lyrics size: {}; File type: ", totalBytes);

            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long downloadedBytes = 0;
            if (ClientConfig.ALLOW_DOWNLOAD.get()) {
                // 下载文件
                try (OutputStream out = Files.newOutputStream(cacheFile)) {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;
                        // 回调进度
                        if (callback != null) {
                            float progress = totalBytes > 0 ? downloadedBytes / (float) totalBytes : 0;
                            callback.onProgress(downloadedBytes, totalBytes, progress);
                        }
                        LyricParser.lyricContent = Files.readString(cacheFile);
                        mc.execute(downloadScreen::setLyricCompleted);
                    }
                    LoginMusic.LOGGER.info("Downloading lyrics completed, total {} bytes", downloadedBytes);
                }
            } else {
                ByteArrayOutputStream baos = MusicConfig.get(urlStr);
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                    downloadedBytes += bytesRead;
                    // 回调进度
                    if (callback != null) {
                        float progress = totalBytes > 0 ? downloadedBytes / (float) totalBytes : 0;
                        callback.onProgress(downloadedBytes, totalBytes, progress);
                    }
                }
                byte[] allBytes = baos.toByteArray();
                // 检测编码
                String charset = LyricParser.detectCharset(allBytes);
                LoginMusic.LOGGER.info("Lyric file loaded from {}, using charset {}", urlStr,  charset);
                // 转换为字符串
                LyricParser.lyricContent = new String(allBytes, charset);
                mc.execute(downloadScreen::setLyricCompleted);
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("Downloading lyrics error!", e);
            mc.execute(() -> downloadScreen.setError("Downloading error!"));
        }
    }

    // 检查是否有本地缓存
    private static boolean isDownloaded(Path cacheFile, DownloadCallback callback) {
        try {
            if (Files.exists(cacheFile)) {
                if (callback != null) {
                    long size = Files.size(cacheFile);
                    callback.onProgress(size, size, 1.0f);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("Downloading method failed!", e);
            return false;
        }
    }
}
