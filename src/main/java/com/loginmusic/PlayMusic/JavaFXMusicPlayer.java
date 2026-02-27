package com.loginmusic.PlayMusic;

import com.loginmusic.LoginMusic;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// JavaFXMusicPlayer播放器
@OnlyIn(Dist.CLIENT)
public class JavaFXMusicPlayer {

    private static final Path CACHE_DIR = Paths.get("LoginMusic");

    private static MediaPlayer currentMediaPlayer;
    private static String currentMusicId;
    private static boolean isDownloaded;

    private static final AtomicBoolean isPlaying = new AtomicBoolean(false);
    private static final AtomicBoolean javafxInitialized = new AtomicBoolean(false);

    // 初始化播放器
    private static void InitialJavaFx() {
        if (javafxInitialized.get()) return;
        // 在一个新线程中初始化 JavaFX 运行时
        // 这个 start 方法会阻塞，直到 JavaFX 退出
        // 但因为我们没有传递任何参数，它会启动一个无窗口的 JavaFX 应用
        // 一个更优雅的方式是用 com.sun.javafx.application.PlatformImpl 来启动，但这里是简易方案
        // 警告：这种启动方式可能在某些环境下不是最优的，但对于一个独立 Mod 来说足够简单。

        try {
            CountDownLatch latch = new CountDownLatch(1);

            new Thread(() -> {
                try {
                    Platform.startup(() -> {
                        LoginMusic.LOGGER.info("JavaFX 运行时启动成功");
                        javafxInitialized.set(true);
                        latch.countDown();
                    });
                } catch (IllegalStateException e) {
                    // 已经启动过
                    javafxInitialized.set(true);
                    latch.countDown();
                } catch (Exception e) {
                    LoginMusic.LOGGER.error("JavaFX 启动失败", e);
                    latch.countDown();
                }
            }).start();

            latch.await(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            LoginMusic.LOGGER.error("JavaFX 初始化异常", e);
        }
    }

    // 播放音乐
    public static void PlayMusic(String musicId, String musicName, String url) {
        InitialJavaFx();

        if (!javafxInitialized.get()) {
            LoginMusic.LOGGER.warn("JavaFX 未初始化，无法播放");
            showErrorToPlayer("JavaFX 初始化失败", musicName);
            return;
        }

        // 停止当前音乐
        StopCurrentMusic();
        currentMusicId = musicId;

        // 在 JavaFX 应用线程中执行播放操作
        Platform.runLater(() -> {
            try {
                // 下载或获取本地文件（这部分可以复用你之前 NetworkMusicPlayer 里的下载逻辑）
                // 为了简单，这里假设我们已经有了一个本地文件，或者直接播放网络流？
                // 更稳定的方式是先下载到本地再播放
                isDownloaded = false;
                File localFile = DownloadMusic(url, musicName); // 复用之前的下载方法

                // 只播放原来就有的文件
                if (localFile != null && localFile.exists() && !isDownloaded) {
                    // 创建 Media 对象
                    Media media = new Media(localFile.toURI().toString());

                    // 创建 MediaPlayer
                    currentMediaPlayer = new MediaPlayer(media);

                    // 更新播放状态
                    currentMediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                        isPlaying.set(newStatus == MediaPlayer.Status.PLAYING);
                    });

                    // 设置播放完成监听
                    currentMediaPlayer.setOnEndOfMedia(() -> {
                        StopCurrentMusic();
                        Minecraft.getInstance().execute(() -> {
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.displayClientMessage(
                                    Component.literal("§7[音乐] 播放结束: " + musicName),
                                    false
                                );
                            }
                        });
                    });

                    // 设置错误监听
                    currentMediaPlayer.setOnError(() -> {
                        LoginMusic.LOGGER.warn("JavaFX 播放错误");
                        Platform.runLater(() -> {
                            StopCurrentMusic();
                            LoginMusic.LOGGER.error("{} 播放出错", musicId);
                        });
                    });

                    // 开始播放
                    currentMediaPlayer.play();
                    isPlaying.set(true);

                    // 通知玩家
                    Minecraft.getInstance().execute(() -> {
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.displayClientMessage(
                                Component.literal("§a 正在播放: " + musicName),
                                true
                            );
                        }
                    });

                } else {
                    if (!isDownloaded) {
                        showErrorToPlayer("找不到音频文件", musicName);
                    }
                }
            } catch (Exception e) {
                LoginMusic.LOGGER.error("JavaFX 播放过程中发生异常", e);
                showErrorToPlayer("播放异常", musicName);
            }
        });
    }

    public static void StopCurrentMusic() {
        if (currentMediaPlayer != null) {
            Platform.runLater(() -> {
                currentMediaPlayer.stop();
                currentMediaPlayer.dispose();
                currentMediaPlayer = null;
                currentMusicId = null;
                isPlaying.set(false);
            });
        } else {
            currentMusicId = null;
            isPlaying.set(false);
        }
    }

    // 检查是否正在播放
    public static boolean isPlaying() {
        return isPlaying.get() && currentMediaPlayer != null;
    }

    // 获取当前播放的音乐ID
    public static String getCurrentMusicId() {
        return currentMusicId;
    }

    // 复用你之前 NetworkMusicPlayer 中的下载方法
    private static File DownloadMusic(String urlStr, String name) {
        try {
            Path cacheFile = CACHE_DIR.resolve(name);

            // 检查缓存
            if (Files.exists(cacheFile)) {
                LoginMusic.LOGGER.info("文件已下载");
                return cacheFile.toFile();
            }

            LoginMusic.LOGGER.info("下载音乐：{}", urlStr);
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("§a 正在下载您的登录音乐: " + currentMusicId),
                        false
                    );
                }
            });

            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "LoginMusic");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStream in = connection.getInputStream()) {
                    OutputStream out = Files.newOutputStream(cacheFile);
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalBytes = 0;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalBytes += bytesRead;
                    }

                    Minecraft.getInstance().execute(() -> {
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.displayClientMessage(
                                    Component.literal("§a 下载完成，重进世界后生效！"),
                                    false
                            );
                        }
                    });
                    LoginMusic.LOGGER.info("下载完成，共 {} 字节", totalBytes);
                }
                isDownloaded = true;
                return cacheFile.toFile();
            } else {
                LoginMusic.LOGGER.warn("下载失败，HTTP状态码：{}", responseCode);
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("下载异常！{}", String.valueOf(e));
        }
        return null; // 替换为实际的返回值
    }

    private static void showErrorToPlayer(String error, String displayName) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("§c[LoginMusic] " + error + ": " + displayName),
                        false
                );
            }
        });
    }
}
