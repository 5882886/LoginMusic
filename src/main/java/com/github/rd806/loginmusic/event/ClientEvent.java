package com.github.rd806.loginmusic.event;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.SelectionKey;
import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.media.SimpleMusicPlayer;
import com.github.rd806.loginmusic.media.load.LoadMethod;
import com.github.rd806.loginmusic.media.load.LoadScreen;
import com.github.rd806.loginmusic.media.layer.MusicInfo;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(value = LoginMusic.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = LoginMusic.MODID, value = Dist.CLIENT)
public class ClientEvent {

    // 记录玩家位置
    private static final Minecraft mc = Minecraft.getInstance();
    private static BlockPos pos = new BlockPos(0, 0, 0);

    // 登录事件
    public static void playLoginMusic(MusicEntry music, BlockPos blockPos, SelectionKey key) {
        Player player = mc.player;
        if (player == null) return;
        pos = blockPos;
        // 当前有正在播放的音乐也跳过
        if (!SimpleMusicPlayer.isStopped()) { return; }

        // 判断是否为来自其他玩家的音乐
        String musicId = music.getId();
        boolean isOwnMusic = false;
        switch (key) {
            case NAME -> isOwnMusic = musicId.equalsIgnoreCase(player.getName().getString()) || musicId.equalsIgnoreCase("Default");
            case UUID -> isOwnMusic = musicId.equalsIgnoreCase(player.getUUID().toString())  || musicId.equalsIgnoreCase("Default");
            case RANDOM -> isOwnMusic = true;
        }

        // 是否为来自其他玩家的音乐
        if (!isOwnMusic) {
            // 设置为不允许则跳过
            if (!ClientConfig.ALLOW_OTHERS_MUSIC.get()) {
                mc.player.displayClientMessage(
                        Component.translatable("message.loginmusic.play.not_allow_others", musicId), false);
                return;
            }
            // 播放来自其他玩家的音乐
            mc.player.displayClientMessage(
                    Component.translatable("message.loginmusic.play.others", musicId), false);
        }

        // 启用下载模式
        mc.execute(() -> {
            // 创建下载界面
            LoadScreen screen = new LoadScreen(music, () -> mc.execute(() -> mc.setScreen(null)));
            // 显示加载界面
            if (ClientConfig.SHOW_LOADING.get()) {
                mc.setScreen(screen);
            }
            LoadMethod.startDownload(music, screen);
            MusicInfo.prepareMusicInfo(music);
        });
    }

    // 检测玩家移动
    @SubscribeEvent
    public static void checkMove(ClientTickEvent.Post event) {
        // 已经停止则不再检测
        if (SimpleMusicPlayer.isStopped() || ClientConfig.MUSIC_PLAY_RANGE.get() == 0) return;

        LocalPlayer player = mc.player;
        if (player == null) return;
        // 获取玩家坐标
        double lastX = pos.getX();
        double lastY = pos.getY();
        double lastZ = pos.getZ();

        boolean outOfRange = (Math.abs(player.getX() - lastX) > ClientConfig.MUSIC_PLAY_RANGE.get())
                            || Math.abs(player.getY() - lastY) > ClientConfig.MUSIC_PLAY_RANGE.get()
                            || Math.abs(player.getZ() - lastZ) > ClientConfig.MUSIC_PLAY_RANGE.get();

        // 检测移动范围
        if (outOfRange) {
            SimpleMusicPlayer.stopMusic();
            mc.player.displayClientMessage(
                    Component.translatable("message.loginmusic.play.out_of_range"),
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