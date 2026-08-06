package com.github.rd806.loginmusic.media.lyric;

import com.github.rd806.loginmusic.config.ClientConfig.Position;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// 显示歌词的专用区域
public class LyricLayer {

    private static LyricLayer lyricLayer;
    private String currentLyricText;
    private int textColor;
    private int yOffset;

    private LyricLayer() {
        // 注册监听事件
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static LyricLayer getInstance() {
        if (lyricLayer == null) {
            lyricLayer = new LyricLayer();
        }
        return lyricLayer;
    }

    // 展示歌词
    public void showLyric(String lyric) {
        this.currentLyricText = lyric;
    }

    // 设置歌词样式
    public void setLyricLayer(Position pos, int hexColor) {
        this.textColor = hexColor;
        switch (pos) {
            case UP -> this.yOffset = -1;
            case MIDDLE -> this.yOffset = 0;
            case DOWN -> this.yOffset = 1;
        }
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
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
}
