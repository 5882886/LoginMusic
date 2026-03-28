package com.rd806.loginmusic.media.lyric;

import com.rd806.loginmusic.config.ClientConfig.Position;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public class LyricLayer {

    private static LyricLayer lyricLayer;
    private String currentLyricText;
    private int textColor;
    private int yOffset;

    private LyricLayer() {
        // 注册监听事件
        NeoForge.EVENT_BUS.register(this);
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
    public void setLyricLayer(Position pos, String text) {
        this.textColor = parseColor(text);
        switch (pos) {
            case UP:
                // 屏幕上四分之一
                this.yOffset = -1;
                break;
            case MIDDLE:
                // 屏幕正中心
                this.yOffset = 0;
                break;
            case DOWN:
                // 屏幕下四分之一
                this.yOffset = 1;
                break;
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
        int x = (screenWidth-textWidth) / 2;
        // 高度为中心加上偏移量
        int y = screenHeight/2 + (yOffset*screenHeight)/4;
        // 绘制文本
        guiGraphics.drawString(mc.font, text, x, y, textColor);
    }

    // 解析文本颜色
    private static int parseColor(String hex) {
        // 去除可能的前缀
        String clean = hex.replace("0x", "").replace("#", "").trim();
        int rgb;

        if (clean.length() == 8) {
            // ARGB 格式，需要转换为 RGB（忽略 Alpha）
            int argb = (int) Long.parseLong(clean, 16);
            rgb = argb & 0x00FFFFFF;  // 移除 Alpha 通道
        } else if (clean.length() == 6) {
            // RGB 格式
            rgb = (int) Long.parseLong(clean, 16);
        } else {
            // 无效格式，返回白色
            return 0xFFFFFF;
        }

        return rgb;
    }

}
