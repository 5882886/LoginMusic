package com.rd806.loginmusic.event;

import com.rd806.loginmusic.Config;
import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.music.MusicConfig;
import com.rd806.loginmusic.music.MusicDownloadScreen;
import com.rd806.loginmusic.music.MusicEntry;
import com.rd806.loginmusic.music.SimpleMusicPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class ClientLoginEvent {

    private static final Minecraft mc = Minecraft.getInstance();

    // 是否初始化位置
    private static boolean isInitialPos = false;
    // 是否已注册活动
    private static boolean listenerRegistered = false;
    // 记录玩家位置
    private static double lastX, lastY, lastZ;


    // 登录事件
    public static void playLoginMusic(String musicId) {
        isInitialPos = false;
        MusicEntry entry = MusicConfig.getMusic(musicId);

        if (entry == null) {
            LoginMusic.LOGGER.warn("Music not found {}", musicId);
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(LoginMusic.MODID + ".message.music_not_found", musicId),
                    false
                );
            }
            return;
        }

        mc.execute(() -> {
            // 创建并显示下载界面
            MusicDownloadScreen screen = new MusicDownloadScreen(musicId, () -> {
                // 下载完成后播放音乐
                mc.execute(() -> {
                    mc.setScreen(null);
                    // 关闭自定义界面，回到游戏
                    // 启动播放事件
                    SimpleMusicPlayer.playMusic(entry.getId(), entry.getName());
                });
            });

            mc.setScreen(screen);
            startDownload(entry.getUrl(), entry.getName(), screen);
        });

        // 注册监听方法
        registerListener();
    }

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
                    if (screen != null) { screen.updateProgress(progress, status); }
                });
                // 设置界面关闭状态
                mc.execute(screen::setCompleted);
            } catch (Exception e) {
                LoginMusic.LOGGER.error("Downloading Music failed!");
                mc.execute(() -> screen.setError("Downloading failed" + e.getMessage()));
            }
        });
    }

    @FunctionalInterface
    public interface DownloadCallback {
        void onProgress(long downloadedBytes, long totalBytes, float progress);
    }

    // 下载方法
    public static void downloadMusic(String urlStr, String name, boolean[] typeMismatch, String[] mismatchType, DownloadCallback callback) {
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
            // 不允许下载则直接返回
            if (!Config.getAllowDownload()) {
                if (mc.player != null) {
                    mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(LoginMusic.MODID + ".message.download_forbidden"),
                        false
                    );
                }
                return;
            }

            LoginMusic.LOGGER.info("Start downloading from {}", urlStr);

            URL url = new URL(urlStr);
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
                if (!mimeType.equals("audio/mpeg")) {
                    LoginMusic.LOGGER.warn("The downloading file {} may not be an audio file!", mimeType);
                    // 设置类型不匹配标志
                    if (typeMismatch != null && typeMismatch.length > 0) {
                        typeMismatch[0] = true;
                    }
                    if (mismatchType != null && mismatchType.length > 0) {
                        mismatchType[0] = mimeType;
                    }
                    callback.onProgress(0, totalBytes, 0.0f);
                }

                // 下载文件
                try (InputStream in = connection.getInputStream()) {
                    OutputStream out = Files.newOutputStream(cacheFile);
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long downloadedBytes = 0;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;
                        // 回调进度
                        if (callback != null) {
                            // (float) 必须在分母上，写在前面直接将转为 0.0f
                            float progress = downloadedBytes / (float) totalBytes;
                            callback.onProgress(downloadedBytes, totalBytes, progress);
                        }
                    }
                    LoginMusic.LOGGER.info("Downloading completed, total {} bytes", downloadedBytes);
                }
            } else {
                LoginMusic.LOGGER.warn("Download failed, error code: {}", responseCode);
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("Downloading error! {}", String.valueOf(e));
        }
    }

    // 注册监听器
    private static void registerListener() {
        if (!listenerRegistered) {
            MinecraftForge.EVENT_BUS.register(ClientLoginEvent.class);
            listenerRegistered = true;
        }
    }

    // 检测玩家移动
    @SubscribeEvent
    public static void checkMove(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        // 已经停止则不再检测
        if (SimpleMusicPlayer.isStopped()) return;

        LocalPlayer player = mc.player;
        if (player == null) return;
        // 获取玩家坐标
        if (!isInitialPos) {
            lastX = player.getX();
            lastY = player.getY();
            lastZ = player.getZ();
            isInitialPos = true;
            return;
        }

        boolean outOfRange = (Math.abs(player.getX() - lastX) > Config.getRange())
                || Math.abs(player.getY() - lastY) > Config.getRange()
                || Math.abs(player.getZ() - lastZ) > Config.getRange();

        // 检测移动范围
        if (outOfRange) {
            SimpleMusicPlayer.stopCurrentMusic();
            mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(LoginMusic.MODID + ".message.out_of_range"),
                false
            );
        }
    }

    // 检测退出世界操作
    @SubscribeEvent
    public static void checkLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (SimpleMusicPlayer.isStopped()) return;
        LoginMusic.LOGGER.info("Player exits the world, stop playing!");
        // 解决玩家退出世界仍播放音乐的问题
        SimpleMusicPlayer.stopCurrentMusic();
    }
}
