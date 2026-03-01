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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// JavaFXMusicPlayer播放器
@OnlyIn(Dist.CLIENT)
public class JavaFXMusicPlayer {

    private static MediaPlayer currentMediaPlayer;
    private static String currentMusicId;

    private static final AtomicBoolean isPlaying = new AtomicBoolean(false);
    private static final AtomicBoolean javafxInitialized = new AtomicBoolean(false);

    // 初始化播放器
    private static void initialJavaFx() {
        if (javafxInitialized.get()) return;
        // 在一个新线程中初始化 JavaFX 运行时
        // 这个 start 方法会阻塞，直到 JavaFX 退出
        // 但因为我们没有传递任何参数，它会启动一个无窗口的 JavaFX 应用
        // 一个更优雅的方式是用 com.sun.javafx.application.PlatformImpl 来启动，但这里是简易方案
        // 警告：这种启动方式可能在某些环境下不是最优的，但对于一个独立 Mod 来说足够简单。
        try {
            CountDownLatch latch = new CountDownLatch(1);
            // 在新线程中启动JavaFX
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
    public static void playMusic(String musicId, String musicName) {
        initialJavaFx();

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
                File localFile = LoginMusic.CACHE_DIR.resolve(musicName).toFile();
                // 只播放原来就有的文件
                if (localFile.exists()) {
                    // 创建 Media 对象
                    Media media = new Media(localFile.toURI().toString());

                    // 创建 MediaPlayer
                    currentMediaPlayer = new MediaPlayer(media);

                    // 更新播放状态
                    currentMediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus)
                            -> isPlaying.set(newStatus == MediaPlayer.Status.PLAYING));

                    // 设置播放完成监听
                    currentMediaPlayer.setOnEndOfMedia(() -> {
                        StopCurrentMusic();
                        Minecraft.getInstance().execute(() -> {
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.displayClientMessage(
                                    Component.translatable( LoginMusic.MODID + ".message.play_ended", musicName),
                                    false
                                );
                            }
                        });
                    });

                    // 设置错误监听
                    currentMediaPlayer.setOnError(() -> {
                        LoginMusic.LOGGER.warn("JavaFX 播放错误");
                        StopCurrentMusic();
                        LoginMusic.LOGGER.error("{} 播放出错", musicId);
                    });

                    // 开始播放
                    currentMediaPlayer.play();
                    isPlaying.set(true);

                    // 通知玩家
                    Minecraft.getInstance().execute(() -> {
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.displayClientMessage(
                                Component.translatable(LoginMusic.MODID + ".message.play_music", musicName),
                                true
                            );
                        }
                    });

                } else {
                    showErrorToPlayer("找不到音频文件", musicName);
                }
            } catch (Exception e) {
                LoginMusic.LOGGER.error("JavaFX 播放过程中发生异常", e);
                showErrorToPlayer("播放异常", musicName);
            }
        });
    }

    // 停止当前音乐
    public static void StopCurrentMusic() {
        Platform.runLater(() -> {
            if (currentMediaPlayer != null) {
                currentMediaPlayer.stop();
                currentMediaPlayer.dispose();
                currentMediaPlayer = null;
            }
            currentMusicId = null;
            isPlaying.set(false);
        });
    }

    // 检查是否已经停止
    public static boolean isStopped() { return !isPlaying.get() || currentMediaPlayer == null; }

    // 获取当前播放的音乐ID
    public static String getCurrentMusicId() { return currentMusicId; }

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
