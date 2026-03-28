package com.rd806.loginmusic.media;

import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.config.ClientConfig;
import com.rd806.loginmusic.media.lyric.LyricEntry;
import com.rd806.loginmusic.media.lyric.LyricParser;
import com.rd806.loginmusic.media.lyric.LyricPlayer;
import com.rd806.loginmusic.media.music.MusicDownloadScreen;
import com.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class SimpleMusicPlayer {
    private static final Minecraft mc = Minecraft.getInstance();

    private static Clip currentClip;
    private static String currentMusicId;
    private static boolean isPlaying = false;
    private static long startTimeMillis;

    private static List<LyricEntry> currentLyrics;
    private static boolean lyricStarted = false;

    private SimpleMusicPlayer() {}

    static {
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        LoginMusic.LOGGER.info("Supported music file:");
        for (AudioFileFormat.Type type : types) {
            LoginMusic.LOGGER.info("  - {}", type.getExtension());
        }

        // 检查MP3 SPI是否加载
        try {
            Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader");
            LoginMusic.LOGGER.info("MP3 support loaded!");
        } catch (ClassNotFoundException e) {
            LoginMusic.LOGGER.warn("MP3 support not found!");
        }
    }

    // 播放音乐
    public static void playMusic(MusicEntry entry) {
        try {
            LoginMusic.LOGGER.info("Playing: {}", entry.getId());
            stopCurrentMusic();

            currentMusicId = entry.getId();
            isPlaying = true;

            File localFile = LoginMusic.CACHE_DIR.resolve(entry.getName()).toFile();

            AudioInputStream audioStream;

            if (localFile.exists()) {
                // 获取音频输入流
                audioStream = AudioSystem.getAudioInputStream(localFile);
            } else {
                // 获取在线音频输入流
                URI uri = new URI(entry.getUrl());
                URL url = uri.toURL();

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedInputStream bufferedInputStream = new BufferedInputStream(connection.getInputStream());
                audioStream = AudioSystem.getAudioInputStream(bufferedInputStream);
            }

            AudioFormat sourceFormat = audioStream.getFormat();

            // 转换为PCM格式（如果需要）
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false
            );

            // 如果格式不匹配，进行转换
            if (!sourceFormat.matches(targetFormat)) {
                LoginMusic.LOGGER.info("Changing format...");
                audioStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
            }

            DataLine.Info info = new DataLine.Info(Clip.class, targetFormat);

            if (!AudioSystem.isLineSupported(info)) {
                LoginMusic.LOGGER.error("Unsupported music file: {}", entry.getName());
                return;
            }

            Clip clip = (Clip) AudioSystem.getLine(info);

            // 添加播放完成监听
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    if (currentClip == clip) {
                        currentClip = null;
                        currentMusicId = null;
                        isPlaying = false;
                        // 播放结束通知
                        mc.execute(() -> {
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.displayClientMessage(
                                        Component.translatable( LoginMusic.MODID + ".message.play_ended", entry.getName()),
                                        false
                                );
                            }
                        });
                    }
                }
            });

            clip.open(audioStream);

            // 在MC线程中执行
            mc.execute(() -> {
                currentClip = clip;
                clip.start();
                startTimeMillis = System.currentTimeMillis();
                playLyric(entry);
                isPlaying = true;
                // 通知玩家
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(
                            Component.translatable(LoginMusic.MODID + ".message.play_music", entry.getName()),
                            false
                    );
                }
            });

            LoginMusic.LOGGER.info("Playing music: {}", entry.getName());

        } catch (UnsupportedAudioFileException e) {
            LoginMusic.LOGGER.error("Unsupported type: {}", e.getMessage());
        } catch (LineUnavailableException e) {
            LoginMusic.LOGGER.error("Not available: {}", e.getMessage());
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to play music: {} ", entry.getName());
        }
    }

    // 播放歌词
    private static void playLyric(MusicEntry entry) {
        if (entry.getLyrics() != null && ClientConfig.getAllowLyrics()) {
            LoginMusic.LOGGER.info("Lyrics prepared!");
            // 异步播放歌词
            LyricParser.loadLyricAsync(entry).thenAccept(lyricContent  -> {
                if (lyricContent != null && !lyricContent.isEmpty()) {
                    currentLyrics = LyricParser.parseLRC(lyricContent);
                    lyricStarted = true;

                    if (isPlaying && startTimeMillis > 0) {
                        LoginMusic.LOGGER.info("Lyrics playing!");
                        // 计算展示歌词与播放开始的间隔时间
                        // 即已播放的时间
                        long elapsedTime = System.currentTimeMillis() - startTimeMillis;
                        LyricPlayer.startLyricDisplay(currentLyrics, elapsedTime);
                    } else  {
                        LoginMusic.LOGGER.warn("No lyrics found!");
                    }
                }
            }).exceptionally(throwable -> {
                LoginMusic.LOGGER.warn("Error loading lyrics!", throwable);
                return null;
            });
        } else if (!ClientConfig.getAllowLyrics()) {
            LoginMusic.LOGGER.warn("Lyrics are disabled!");
        }
    }

    // 停止播放
    public static void stopCurrentMusic() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
            currentMusicId = null;
            isPlaying = false;
            LyricPlayer.stopLyricDisplay();
        }
    }

    /* ----- 下载逻辑 ----- */

    public static void startDownload(String url, String name, MusicDownloadScreen screen) {
        // 加载下载界面
        CompletableFuture.runAsync(() -> {
            try {
                boolean[] typeMismatch = {false};
                String[] mismatch = {""};
                // 显示下载信息
                downloadMusic(url, name, typeMismatch, mismatch, (downloaded, total, progress) -> {
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

    // 下载方法
    private static void downloadMusic(String urlStr, String name, boolean[] typeMismatch, String[] mismatchType, DownloadCallback callback) {
        try {
            Path cacheFile = LoginMusic.CACHE_DIR.resolve(name);
            // 检查缓存，命中直接返回
            if (Files.exists(cacheFile)) {
                LoginMusic.LOGGER.info("File has been downloaded!");
                if (callback != null) {
                    long size = Files.size(cacheFile);
                    callback.onProgress(size, size, 1.0f);
                }
                return;
            }

            LoginMusic.LOGGER.info("Start downloading from {}", urlStr);

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

                LoginMusic.LOGGER.info("File size: {}; File type: {}", totalBytes, mimeType);

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
                    LoginMusic.LOGGER.info("Downloading completed, total {} bytes", downloadedBytes);
                }
            } else {
                LoginMusic.LOGGER.warn("Download failed, error code: {}", responseCode);
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("Downloading error!", e);
        }
    }

    // 获取播放状态
    public static boolean isStopped() { return !isPlaying; }

    // 获取播放的音乐id
    public static String getCurrentMusicId() { return currentMusicId; }
}
