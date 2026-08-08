package com.github.rd806.loginmusic.media.download;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.media.PreparedAudio;
import com.github.rd806.loginmusic.media.SimpleMusicPlayer;
import com.github.rd806.loginmusic.media.lyric.LyricParser;
import com.github.rd806.loginmusic.media.music.MusicCache;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadMethod {

    private static final Minecraft mc = Minecraft.getInstance();
    private static DownloadScreen downloadScreen;

    private static volatile PreparedAudio preparedAudio;
    private static volatile String lyric;

    // 音频加载线程
    public static final ExecutorService AUDIO_LOADER = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "LoginMusic AudioLoader");
        t.setDaemon(true);
        return t;
    });
    // 歌词加载线程
    public static final ExecutorService LYRIC_LOADER = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "LoginMusic LyricLoader");
        t.setDaemon(true);
        return t;
    });

    // 开始下载
    public static void startDownload(MusicEntry music, DownloadScreen screen) {
        downloadScreen = screen;
        // 创建两个 CompletableFuture 来跟踪下载任务
        CompletableFuture<Void> audioFuture = CompletableFuture.runAsync(() -> {
            preparedAudio = downloadMusic(music, (downloaded, total, progress) -> {
                Component status = Component.translatable(LoginMusic.MODID + ".gui.download.progress.music",
                        String.format("%.1f", downloaded / 1024.0 / 1024.0),
                        String.format("%.1f", total / 1024.0 / 1024.0));
                if (screen != null) {
                    mc.execute(() -> screen.updateAudioProgress(progress, status));
                }
            });
            mc.execute(screen::setAudioCompleted);
        }, AUDIO_LOADER);  // 使用 AUDIO_LOADER 作为执行器

        CompletableFuture<Void> lyricFuture = CompletableFuture.runAsync(() -> {
            lyric = downloadLyrics(music, (downloaded, total, progress) -> {
                Component status = Component.translatable(LoginMusic.MODID + ".gui.download.progress.lyric",
                        String.format("%.1f", downloaded / 1024.0 / 1024.0),
                        String.format("%.1f", total / 1024.0 / 1024.0));
                if (screen != null) {
                    mc.execute(() -> screen.updateLyricProgress(progress, status));
                }
            });
            mc.execute(screen::setLyricCompleted);
        }, LYRIC_LOADER);

        // 等待两个任务都完成
        CompletableFuture.allOf(audioFuture, lyricFuture)
                .thenAccept(ignored -> {
                    // 确保在 Minecraft 主线程中执行
                    mc.execute(() -> {
                        // 检查是否都完成了
                        if (preparedAudio != null && lyric != null) {
                            SimpleMusicPlayer.playMusic(music, preparedAudio, lyric);
                        } else {
                            LoginMusic.LOGGER.error("Could not load music or lyrics!");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    // 错误处理
                    LoginMusic.LOGGER.error("Failed to download music or lyrics", throwable);
                    return null;
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
    private static PreparedAudio downloadMusic(MusicEntry music, DownloadCallback callback) {
        String name = music.getMusicName();
        String path = music.getMusicPath();
        try {
            PreparedAudio audio = MusicCache.getMusic(path);

            if (audio != null) {
                if (downloadScreen != null) {
                    mc.execute(downloadScreen::setAudioCompleted);
                }
                return audio;
            }

            // 检查缓存，命中直接返回
            Path cacheFile = LoginMusic.MUSICS_DIR.resolve(name);
            if (isDownloaded(cacheFile, callback)) {
                LoginMusic.LOGGER.info("Music has been downloaded!");
                // 读取文件所有字节到 byte[]
                byte[] data = Files.readAllBytes(cacheFile);
                // 包装为 ByteArrayInputStream
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                audio = PrepareMusic.prepareAudio(bais);
                MusicCache.putMusic(path, audio);
                if (downloadScreen != null) {
                    mc.execute(downloadScreen::setAudioCompleted);
                }
                return audio;
            }

            // 从网络加载
            LoginMusic.LOGGER.info("Start downloading music from {}", path);
            URLConnection connection = openConnection(path);
            // 获取文件大小
            if (connection == null) {
                LoginMusic.LOGGER.error("Audio network connection error!");
                if (downloadScreen != null) {
                    mc.execute(() -> downloadScreen.setError("Downloading error!"));
                }
                return null;
            }
            long totalBytes = connection.getContentLengthLong();
            LoginMusic.LOGGER.info("Music size: {}", totalBytes);

            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long downloadedBytes = 0;

            // 加载文件
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                downloadedBytes += bytesRead;
                // 回调进度
                if (callback != null) {
                    float progress = totalBytes > 0 ? downloadedBytes / (float) totalBytes : 0;
                    callback.onProgress(downloadedBytes, totalBytes, progress);
                }
            }
            // 转换为输入流
            byte[] data = baos.toByteArray();
            if (ClientConfig.ALLOW_DOWNLOAD.get()) {
                Files.write(cacheFile, data);
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            LoginMusic.LOGGER.info("Downloading music completed, total {} bytes", downloadedBytes);
            audio = PrepareMusic.prepareAudio(bais);
            MusicCache.putMusic(path, audio);
            if (downloadScreen != null) {
                mc.execute(downloadScreen::setAudioCompleted);
            }
            return audio;
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("Downloading music error!", e);
            if (downloadScreen != null) {
                mc.execute(() -> downloadScreen.setError("Downloading error!"));
            }
            return null;
        }
    }

    // 歌词下载方法
    private static String downloadLyrics(MusicEntry music, DownloadCallback callback) {
        String name = music.getLyricName();
        String path = music.getLyricPath();
        try {
            String lyric = MusicCache.getLyric(path);
            if (lyric != null) {
                if (downloadScreen != null) {
                    mc.execute(downloadScreen::setLyricCompleted);
                }
                return lyric;
            }

            // 检查缓存，命中直接返回
            Path cacheFile = LoginMusic.LYRICS_DIR.resolve(name);
            if (isDownloaded(cacheFile, callback)) {
                try {
                    LoginMusic.LOGGER.info("Lyric file loaded");
                    lyric = Files.readString(cacheFile);
                    MusicCache.putLyric(path, lyric);
                    if (downloadScreen != null) {
                        mc.execute(downloadScreen::setLyricCompleted);
                    }
                    return lyric;
                } catch (Exception e) {
                    LoginMusic.LOGGER.error("Error while loading Lyric from {}", path, e);
                    if (downloadScreen != null) {
                        mc.execute(() -> downloadScreen.setError("Downloading error!"));
                    }
                    return null;
                }
            }

            // 从网络加载
            LoginMusic.LOGGER.info("Start downloading lyrics from {}", path);
            URLConnection connection = openConnection(path);
            if (connection == null) {
                LoginMusic.LOGGER.error("Lyric network connection error!");
                if (downloadScreen != null) {
                    mc.execute(() -> downloadScreen.setError("Downloading error!"));
                }
                return null;
            }

            long totalBytes = connection.getContentLengthLong();
            LoginMusic.LOGGER.info("Lyrics size: {}", totalBytes);

            // 下载文件
            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long downloadedBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                downloadedBytes += bytesRead;
                // 回调进度
                if (callback != null) {
                    float progress = totalBytes > 0 ? downloadedBytes / (float) totalBytes : 0;
                    callback.onProgress(downloadedBytes, totalBytes, progress);
                }
            }
            byte[] data = baos.toByteArray();
            // 检测编码
            String charset = LyricParser.detectCharset(data);
            // 转换为字符串
            lyric = new String(data, charset);
            // 保存到文件
            if (ClientConfig.ALLOW_DOWNLOAD.get()) {
                Files.write(cacheFile, data);
            }
            MusicCache.putLyric(path, lyric);
            LoginMusic.LOGGER.info("Downloading lyric completed, total {} bytes", downloadedBytes);
            if (downloadScreen != null) {
                mc.execute(downloadScreen::setLyricCompleted);
            }
            return lyric;
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("Downloading lyric error!", e);
            if (downloadScreen != null) {
                mc.execute(() -> downloadScreen.setError("Downloading error!"));
            }
            return null;
        }
    }

    // 检查是否有本地缓存
    private static boolean isDownloaded(Path cacheFile, DownloadCallback callback) {
        try {
            if (Files.exists(cacheFile) && callback != null) {
                long size = Files.size(cacheFile);
                callback.onProgress(size, size, 1.0f);
                return true;
            }
            return false;
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("Failed to load from local file!", e);
            return false;
        }
    }
}
