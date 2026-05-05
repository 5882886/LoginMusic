package com.rd806.loginmusic.media.download;

import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class DownloadMethod {

    private static final Minecraft mc = Minecraft.getInstance();

    // 开始下载
    public static void startDownload(MusicEntry entry, DownloadScreen screen) {
        // 加载下载界面
        CompletableFuture.runAsync(() -> {
            try {
                boolean[] typeMismatch = {false};
                String[] mismatch = {""};

                // 下载音乐
                downloadMusic(entry.getMusicUrl(), entry.getMusic(), typeMismatch, mismatch, (downloaded, total, progress) -> {
                    Component status;
                    // 设置不同的提示信息
                    if (typeMismatch[0]) {
                        status = Component.translatable(LoginMusic.MODID + ".gui.logindownload.warn");
                    } else {
                        status = Component.translatable(LoginMusic.MODID + ".gui.logindownload.progress",
                                String.format("%.1f", downloaded / 1024.0 / 1024.0),
                                String.format("%.1f", total / 1024.0 / 1024.0)
                        );
                    }
                    // 在主进程中更新进度
                    if (screen != null) {
                        mc.execute(() -> screen.updateProgress(progress, status));
                    }
                });
                // 下载歌词
                downloadLyrics(entry.getLyricUrl(), entry.getLyric(), (downloaded, total, progress) -> {
                    Component status;
                    // 设置不同的提示信息
                    status = Component.translatable(LoginMusic.MODID + ".gui.logindownload.progress",
                            String.format("%.1f", downloaded / 1024.0 / 1024.0),
                            String.format("%.1f", total / 1024.0 / 1024.0)
                    );
                    // 在主进程中更新进度
                    if (screen != null) {
                        mc.execute(() -> screen.updateProgress(progress, status));
                    }
                });

                // 设置界面关闭状态
                mc.execute(screen::setCompleted);
            } catch (Exception e) {
                LoginMusic.LOGGER.error("Downloading Music failed!");
                mc.execute(() -> screen.setError("Downloading failed" + e.getMessage()));
            }
        });
    }

    // 进度回调
    @FunctionalInterface
    private interface DownloadCallback {
        void onProgress(long downloadedBytes, long totalBytes, float progress);
    }

    // 音乐下载方法
    private static void downloadMusic(String urlStr, String name, boolean[] typeMismatch, String[] mismatchType, DownloadCallback callback) {
        try {
            Path cacheFile = LoginMusic.CACHE_DIR.resolve(name);
            // 检查缓存，命中直接返回
            if (Files.exists(cacheFile)) {
                LoginMusic.LOGGER.info("Music has been downloaded!");
                if (callback != null) {
                    long size = Files.size(cacheFile);
                    callback.onProgress(size, size, 1.0f);
                }
                return;
            }
            LoginMusic.LOGGER.info("Start downloading music from {}", urlStr);
            // Java20 之后不再使用 URL() 方法
            // - URL url = new URL(urlStr);
            URI uri = new URI(urlStr);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "LoginMusic");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // 获取文件大小和类型
                long totalBytes = connection.getContentLengthLong();
                String mimeType = connection.getContentType();

                LoginMusic.LOGGER.info("Music size: {}; Music type: {}", totalBytes, mimeType);

                // 检查文件类型
                if (mimeType != null && !mimeType.equals("audio/mpeg")) {
                    LoginMusic.LOGGER.warn("The downloading file {} may not be an audio file!", mimeType);
                    // 设置类型不匹配标志
                    if (typeMismatch != null && typeMismatch.length > 0) {
                        typeMismatch[0] = true;
                    }
                    if (mismatchType != null && mismatchType.length > 0) {
                        mismatchType[0] = mimeType;
                    }
                }
                // 下载文件
                try (InputStream in = connection.getInputStream();
                     OutputStream out = Files.newOutputStream(cacheFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long downloadedBytes = 0;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;
                        // 回调进度
                        if (callback != null) {
                            float progress = totalBytes > 0 ? downloadedBytes / (float) totalBytes : 0;
                            callback.onProgress(downloadedBytes, totalBytes, progress);
                        }
                    }
                    LoginMusic.LOGGER.info("Downloading music completed, total {} bytes", downloadedBytes);
                }
            } else {
                LoginMusic.LOGGER.warn("Download music failed, error code: {}", responseCode);
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("Downloading music error!", e);
        }
    }

    // 歌词下载方法
    private static void downloadLyrics(String urlStr, String name, DownloadCallback callback) {
        try {
            Path cacheFile = LoginMusic.LYRICS_DIR.resolve(name);
            // 检查缓存，命中直接返回
            if (Files.exists(cacheFile)) {
                LoginMusic.LOGGER.info("Lyrics has been downloaded!");
                if (callback != null) {
                    long size = Files.size(cacheFile);
                    callback.onProgress(size, size, 1.0f);
                }
                return;
            }
            LoginMusic.LOGGER.info("Start downloading lyrics from {}", urlStr);
            // Java20 之后不再使用 URL() 方法
            // - URL url = new URL(urlStr);
            URI uri = new URI(urlStr);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "LoginMusic");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // 获取文件大小和类型
                long totalBytes = connection.getContentLengthLong();
                String mimeType = connection.getContentType();

                LoginMusic.LOGGER.info("Lyrics size: {}; File type: {}", totalBytes, mimeType);

                // 下载文件
                try (InputStream in = connection.getInputStream();
                     OutputStream out = Files.newOutputStream(cacheFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long downloadedBytes = 0;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;
                        // 回调进度
                        if (callback != null) {
                            float progress = totalBytes > 0 ? downloadedBytes / (float) totalBytes : 0;
                            callback.onProgress(downloadedBytes, totalBytes, progress);
                        }
                    }
                    LoginMusic.LOGGER.info("Downloading lyrics completed, total {} bytes", downloadedBytes);
                }
            } else {
                LoginMusic.LOGGER.warn("Download lyrics failed, error code: {}", responseCode);
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("Downloading lyrics error!", e);
        }
    }
}
