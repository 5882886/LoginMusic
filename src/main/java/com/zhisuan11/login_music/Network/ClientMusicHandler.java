package com.zhisuan11.login_music.Network;

import com.zhisuan11.login_music.LoginMusic;
import com.zhisuan11.login_music.Music.MusicConfig;
import com.zhisuan11.login_music.Music.MusicEntry;
import com.zhisuan11.login_music.PlayMusic.JavaFXMusicPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientMusicHandler {

    private static final Minecraft mc = Minecraft.getInstance();

    // 音乐是否停止
    private static boolean isStopped = false;
    // 是否初始化位置
    private static boolean isInitialPos = false;
    // 是否已注册活动
    private static boolean listenerRegistered = false;
    // 记录玩家位置
    private static double lastX, lastY, lastZ;

    // 登录事件
    public static void PlayLoginMusic(String musicId) {
        isStopped = false;
        isInitialPos = false;

        MusicEntry entry = MusicConfig.getMusic(musicId);

        if (entry == null) {
            LoginMusic.LOGGER.warn("未找到音乐 {}", musicId);
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c[音乐] 未找到音乐: " + musicId),
                        true
                );
            }
            return;
        }

        RegisterListener();
        JavaFXMusicPlayer.PlayMusic(musicId, entry.getName(), entry.getUrl());
    }

    // 注册监听器
    private static void RegisterListener() {
        if (!listenerRegistered) {
            MinecraftForge.EVENT_BUS.register(ClientMusicHandler.class);
            listenerRegistered = true;
        }
    }

    // 检测玩家移动
    @SubscribeEvent
    public static void CheckMove(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 已经停止则不再检测
        if (isStopped || JavaFXMusicPlayer.getCurrentMusicId() == null) return;

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

        // 检测是否移动
        if (player.getX() != lastX || player.getY() != lastY || player.getZ() != lastZ) {
            JavaFXMusicPlayer.StopCurrentMusic();
            mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("检测到移动，已停止音乐播放"),
                    true
            );
            isStopped = true;
        }
    }

    // 检测键盘操作
    @SubscribeEvent
    public static void onInput(InputEvent event) {
        if (isStopped || JavaFXMusicPlayer.getCurrentMusicId() == null) return;

        // 如果有任何键盘输入，也停止音乐
        if (mc.player != null) {
            JavaFXMusicPlayer.StopCurrentMusic();
            isStopped = true;
        }
    }
}
