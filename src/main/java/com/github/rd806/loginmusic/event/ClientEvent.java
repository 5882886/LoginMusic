package com.github.rd806.loginmusic.event;

import com.github.rd806.loginmusic.SelectionKey;
import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.media.download.DownloadMethod;
import com.github.rd806.loginmusic.media.download.DownloadScreen;
import com.github.rd806.loginmusic.media.layer.MusicInfo;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import com.github.rd806.loginmusic.media.SimpleMusicPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientEvent {

    // 是否初始化位置
    public static boolean isInitialPos = false;
    // 记录玩家位置
    private static double lastX, lastY, lastZ;
    private static final Minecraft mc = Minecraft.getInstance();

    // 登录事件
    public static void playLoginMusic(MusicEntry music, SelectionKey key) {
        Player player = mc.player;
        if (player == null) return;

        // 当前有正在播放的音乐跳过
        if (SimpleMusicPlayer.isPlaying()) { return; }

        // 判断是否为来自其他玩家的音乐
        String musicId = music.getId();
        boolean isOwnMusic = false;
        switch (key) {
            case NAME -> isOwnMusic = musicId.equalsIgnoreCase(player.getName().getString()) || musicId.equalsIgnoreCase("Default");
            case UUID -> isOwnMusic = musicId.equalsIgnoreCase(player.getUUID().toString())  || musicId.equalsIgnoreCase("Default");
            case RANDOM -> isOwnMusic = true;
        }

        // 不是则进入判断
        if (!isOwnMusic) {
            // 设置为不允许则跳过
            if (!ClientConfig.ALLOW_OTHERS_MUSIC.get()) {
                player.displayClientMessage(
                        Component.translatable(LoginMusic.MODID + ".message.not_allow_others_music", musicId),
                        false);
                return;
            }
            // 播放来自其他玩家的音乐
            player.displayClientMessage(
                    Component.translatable(LoginMusic.MODID + ".message.play_others_music", musicId),
                    false);
        }

        isInitialPos = false;
        // 启用下载模式
        mc.execute(() -> {
            // 创建并显示下载界面
            DownloadScreen screen = new DownloadScreen(music, () -> mc.execute(() -> mc.setScreen(null)));
            if (ClientConfig.SHOW_LOADING.get()) {
                mc.setScreen(screen);
            }
            DownloadMethod.startDownload(music, screen);
            MusicInfo.prepareMusicInfo(music);
        });
    }

    // 玩家退出世界则停止播放
    @SubscribeEvent
    public static void checkLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        SimpleMusicPlayer.stopCurrentMusic();
        LoginMusic.LOGGER.info("Player exits the world, stop playing!");
    }

    // 检测玩家移动
    @SubscribeEvent
    public static void checkMove(TickEvent.ClientTickEvent event) {
        // 已经停止则不再检测
        if (!SimpleMusicPlayer.isPlaying() || ClientConfig.MUSIC_PLAY_RANGE.get() == 0) return;

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
        // 检测移动范围
        int range = ClientConfig.MUSIC_PLAY_RANGE.get();
        boolean outOfRange = Math.abs(player.getX() - lastX) > range ||
                Math.abs(player.getY() - lastY) > range || Math.abs(player.getZ() - lastZ) > range;

        if (outOfRange) {
            SimpleMusicPlayer.stopCurrentMusic();
            mc.player.displayClientMessage(
                    Component.translatable(LoginMusic.MODID + ".message.out_of_range"),
                    false);
        }
    }
}
