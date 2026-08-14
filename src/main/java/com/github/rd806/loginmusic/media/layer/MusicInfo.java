package com.github.rd806.loginmusic.media.layer;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@Mod(value = LoginMusic.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = LoginMusic.MODID, value = Dist.CLIENT)
public class MusicInfo {

    private static MusicEntry music;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        // 获取文件名称
        if (music == null) return;
        String musicName = LoginMusic.removeExtension(music.getMusicName());
        // 绘制文字
        var guiGraphics = event.getGuiGraphics();
        Component text = Component.translatable(LoginMusic.MODID + ".gui.info", musicName);
        guiGraphics.drawString(mc.font, text, 20, 20, 0xffffff, false);
    }

    public static void setMusic(MusicEntry musicEntry) {
        music = musicEntry;
    }
}
