package com.github.rd806.loginmusic.media.layer;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.config.ClientConfig.Position;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@Mod(value = LoginMusic.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = LoginMusic.MODID, value = Dist.CLIENT)
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
        int x = (screenWidth-textWidth) / 2;
        // 高度为中心加上偏移量
        int y = screenHeight/2 + (yOffset*screenHeight)/4;
        // 绘制文本
        guiGraphics.drawString(mc.font, text, x, y, textColor, false);
    }

    // 展示歌词
    public static void showLyric(String lyric) { currentLyricText = lyric; }

    // 设置歌词样式
    public static void setLyricLayer(Position pos, int hexColor) {
        textColor = hexColor;
        switch (pos) {
            case UP -> yOffset = -1;
            case MIDDLE -> yOffset = 0;
            case DOWN -> yOffset = 1;
        }
    }
}
