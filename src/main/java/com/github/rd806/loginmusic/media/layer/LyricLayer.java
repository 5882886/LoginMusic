package com.github.rd806.loginmusic.media.layer;

import com.github.rd806.loginmusic.config.ClientConfig.Position;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 显示歌词的专用区域
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LyricLayer {

    private static String currentLyricText;
    private static int textColor;
    private static int yOffset;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (currentLyricText == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        var guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        // 计算文本位置（居中）
        String text = currentLyricText;
        int textWidth = mc.font.width(text);
        int x = (screenWidth - textWidth) / 2;
        // 高度为中心加上偏移量
        int y = screenHeight/2 + (yOffset*screenHeight)/4;
        // 绘制文本
        guiGraphics.drawString(mc.font, text, x, y, textColor, false);
    }

    // 展示歌词
    public static void showLyric(String lyric) { currentLyricText = lyric; }

    // 设置歌词样式
    public static void setLyric(Position pos, int hexColor) {
        textColor = hexColor;
        switch (pos) {
            case UP -> yOffset = -1;
            case MIDDLE -> yOffset = 0;
            case DOWN -> yOffset = 1;
        }
    }
}
