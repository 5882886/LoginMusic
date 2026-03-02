package com.loginmusic.event;

import com.loginmusic.Config;
import com.loginmusic.LoginMusic;
import com.loginmusic.music.JavaFXMusicPlayer;
import com.loginmusic.music.MusicConfig;
import com.loginmusic.music.MusicEntry;
import com.loginmusic.music.MusicDownloadScreen;
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
    public static void PlayLoginMusic(String musicId) {
        isInitialPos = false;
        MusicEntry entry = MusicConfig.getMusic(musicId);

        if (entry == null) {
            LoginMusic.LOGGER.warn("未找到音乐 {}", musicId);
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
                    JavaFXMusicPlayer.playMusic(entry.getId(), entry.getName());
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
                // 显示下载信息
                downloadMusic(url, name, (downloaded, total, progress) -> {
                    Component status = Component.translatable(LoginMusic.MODID + ".gui.logindownload.progress",
                        String.format("%.1f", downloaded / 1024.0 / 1024.0),
                        String.format("%.1f", total / 1024.0 / 1024.0)
                    );
                    // 在主进程中更新进度
                    if (screen != null) { screen.updateProgress(progress, status); }
                });
                // 设置界面关闭状态
                mc.execute(screen::setCompleted);
            } catch (Exception e) {
                LoginMusic.LOGGER.error("下载音乐错误！");
                mc.execute(() -> screen.setError("下载失败" + e.getMessage()));
            }
        });
    }

    @FunctionalInterface
    public interface DownloadCallback {
        void onProgress(long downloadedBytes, long totalBytes, float progress);
    }

    // 下载方法
    public static void downloadMusic(String urlStr, String name, DownloadCallback callback) {
        try {
            Path cacheFile = LoginMusic.CACHE_DIR.resolve(name);
            // 检查缓存，命中直接返回
            if (Files.exists(cacheFile)) {
                LoginMusic.LOGGER.info("文件已下载");
                if (callback != null) {
                    long size = Files.size(cacheFile);
                    callback.onProgress(size, size, 1.0f);
                }
                return;
            }
            LoginMusic.LOGGER.info("开始下载音乐：{}", urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "LoginMusic");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // 获取文件大小
                long totalBytes = connection.getContentLengthLong();
                LoginMusic.LOGGER.info("文件大小：{}", totalBytes);
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
                    LoginMusic.LOGGER.info("下载完成，共 {} 字节", downloadedBytes);
                }
            } else {
                LoginMusic.LOGGER.warn("下载失败，HTTP状态码：{}", responseCode);
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("下载异常！{}", String.valueOf(e));
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
        if (JavaFXMusicPlayer.isStopped()) return;

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
            JavaFXMusicPlayer.StopCurrentMusic();
            mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(LoginMusic.MODID + ".message.out_of_range"),
                false
            );
        }
    }

    // 检测退出世界操作
    @SubscribeEvent
    public static void checkLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (JavaFXMusicPlayer.isStopped()) return;
        LoginMusic.LOGGER.info("玩家退出世界，停止音乐播放");
        // 解决玩家退出世界仍播放音乐的问题
        JavaFXMusicPlayer.StopCurrentMusic();
    }
}
