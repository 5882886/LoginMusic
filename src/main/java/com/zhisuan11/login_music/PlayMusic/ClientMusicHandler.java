package com.zhisuan11.login_music.PlayMusic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// 仅在客户端执行
@OnlyIn(Dist.CLIENT)
public class ClientMusicHandler {
    private static final Minecraft mc = Minecraft.getInstance();

    // 当前播放的音乐文件
    private static SimpleSoundInstance currentMusic = null;
    // 音乐是否停止
    private static boolean isStopped = false;
    // 是否初始化位置
    private static boolean isInitialPos = false;
    // 是否已注册活动
    private static boolean listenerRegistered = false;
    // 记录玩家位置
    private static double lastX, lastY, lastZ;


    // 播放登录音乐
    public static void PlayMusic(String musicID) {
        isStopped = false;
        isInitialPos = false;

        StopMusic();

        currentMusic = SimpleSoundInstance.forUI(Sounds.LOGIN_MUSIC.get(), 1.0f, 1.0f);
        mc.getSoundManager().play(currentMusic);

        RegisterListener();
    }


    private static void RegisterListener() {
        if (!listenerRegistered) {
            MinecraftForge.EVENT_BUS.register(ClientMusicHandler.class);
            listenerRegistered = true;
        }
    }

    // 停止播放音乐
    private static void StopMusic() {
        if (currentMusic != null) {
            mc.getSoundManager().stop(currentMusic);
            currentMusic = null;
        }
    }

    // 检测玩家移动
    @SubscribeEvent
    public static void CheckMove(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (isStopped || currentMusic == null) return;

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
            StopMusic();
            isStopped = true;
            // 发送提示信息
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("检测到移动，已停止登录音乐"),
                    true
            );
        }
    }
}
