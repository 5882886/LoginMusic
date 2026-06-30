package com.github.rd806.loginmusic.event;

import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.media.download.DownloadMethod;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import com.github.rd806.loginmusic.media.download.DownloadScreen;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import com.github.rd806.loginmusic.media.SimpleMusicPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEvent {

    private static final Minecraft mc = Minecraft.getInstance();
    // 是否初始化位置
    private static boolean isInitialPos = false;
    // 是否已注册活动
    private static boolean listenerRegistered = false;
    // 记录玩家位置
    private static double lastX, lastY, lastZ;

    // 登录事件
    public static void playLoginMusic(String musicId) {
        if (mc.player == null) return;
        // 判断是否为来自其他玩家的音乐
        boolean isOwnMusic = musicId.equalsIgnoreCase(mc.player.getGameProfile().getName())
                            || musicId.equalsIgnoreCase(mc.player.getStringUUID());
        // 不是则进入判断
        if (!isOwnMusic) {
            // 设置为不允许则跳过
            if (!ClientConfig.ALLOW_OTHERS_MUSIC.get()) {
                mc.player.displayClientMessage(
                        Component.translatable(LoginMusic.MODID + ".message.not_allow_others_music", musicId), false);
                return;
            }
            // 当前有正在播放的音乐也跳过
            if (!SimpleMusicPlayer.isStopped()) { return; }
            // 播放来自其他玩家的音乐
            mc.player.displayClientMessage(
                    Component.translatable(LoginMusic.MODID + ".message.play_others_music", musicId), false);
        }

        isInitialPos = false;
        MusicEntry entry = MusicConfig.getMusic(musicId);
        if (entry == null) {
            LoginMusic.LOGGER.warn("Music not found {}", musicId);
            mc.player.displayClientMessage(
                    Component.translatable(LoginMusic.MODID + ".message.music_not_found", musicId), false);
            return;
        }

        // 启用下载模式
        if (ClientConfig.ALLOW_OTHERS_MUSIC.get()) {
            mc.execute(() -> {
                // 创建并显示下载界面
                DownloadScreen screen = new DownloadScreen(musicId, () -> {
                    // 下载完成后播放音乐
                    mc.execute(() -> {
                        mc.setScreen(null);
                        // 关闭自定义界面，回到游戏
                        // 启动播放事件
                        SimpleMusicPlayer.playMusic(entry);
                    });
                });
                mc.setScreen(screen);
                DownloadMethod.startDownload(entry, screen);
            });
        } else {
            // 不启用下载，直接读取音频流
            mc.player.displayClientMessage(Component.translatable(LoginMusic.MODID + ".message.download_forbidden"), false);
            mc.execute(() -> SimpleMusicPlayer.playMusic(entry));
        }
        // 注册监听方法
        registerListener();
    }

    // 注册监听器
    private static void registerListener() {
        if (!listenerRegistered) {
            MinecraftForge.EVENT_BUS.register(ClientEvent.class);
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

        int range = ClientConfig.MUSIC_PLAY_RANGE.get();
        // range小于零则返回
        if (range < 0) { return; }

        // 检测移动范围
        boolean outOfRange = Math.abs(player.getX() - lastX) > range ||
                            Math.abs(player.getY() - lastY) > range || Math.abs(player.getZ() - lastZ) > range;
        if (outOfRange) {
            SimpleMusicPlayer.stopCurrentMusic();
            mc.player.displayClientMessage(Component.translatable(LoginMusic.MODID + ".message.out_of_range"), false);
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
