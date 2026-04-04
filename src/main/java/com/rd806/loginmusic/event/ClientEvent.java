package com.rd806.loginmusic.event;

import com.rd806.loginmusic.config.ClientConfig;
import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.media.music.MusicConfig;
import com.rd806.loginmusic.media.DownloadScreen;
import com.rd806.loginmusic.media.music.MusicEntry;
import com.rd806.loginmusic.media.SimpleMusicPlayer;
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
    // 记录自己的musicId
    private static boolean initial = false;
    private static String ownId = null;
    // 是否初始化位置
    private static boolean isInitialPos = false;
    // 是否已注册活动
    private static boolean listenerRegistered = false;
    // 记录玩家位置
    private static double lastX, lastY, lastZ;

    // 登录事件
    public static void playLoginMusic(String musicId) {
        if (mc.player == null) return;
        // 获取自己的musicId
        if (!initial) {
            ownId = musicId;
            initial = true;
        }
        // 是否为来自其他玩家的音乐
        if (!musicId.equals(ownId)) {
            // 设置为不允许则跳过
            if (!ClientConfig.getAllowOthersMusic()) {
                mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(LoginMusic.MODID + ".message.not_allow_others_music", musicId),
                        false
                );
                return;
            }
            // 当前有正在播放的音乐也跳过
            if (!SimpleMusicPlayer.isStopped()) {
                return;
            }
            // 播放来自其他玩家的音乐
            mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(LoginMusic.MODID + ".message.play_others_music", musicId),
                    false
            );
        }

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

        // 启用下载模式
        if (ClientConfig.getAllowDownload()) {
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
                SimpleMusicPlayer.startDownload(entry.getUrl(), entry.getName(), screen);
            });
        } else {
            // 不启用下载，直接读取音频流
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        Component.translatable(LoginMusic.MODID + ".message.download_forbidden"),
                        false
                );
            }
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

        boolean outOfRange = (Math.abs(player.getX() - lastX) > ClientConfig.getRange())
                || Math.abs(player.getY() - lastY) > ClientConfig.getRange()
                || Math.abs(player.getZ() - lastZ) > ClientConfig.getRange();

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
