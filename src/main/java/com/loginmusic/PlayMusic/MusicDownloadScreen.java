package com.loginmusic.PlayMusic;

import com.loginmusic.LoginMusic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

// 音乐下载界面
public class MusicDownloadScreen extends Screen {
    private final String musicId;
    private final Runnable onComplete;

    private volatile boolean completed = false;
    private volatile boolean error = false;
    private volatile String errorMessage = "";
    private volatile float progress;
    private volatile Component status = Component.translatable(LoginMusic.MODID + ".gui.logindownload.start");

    private boolean callbackTriggered = false;

    public MusicDownloadScreen(String musicId, Runnable onComplete) {
        super(Component.translatable(LoginMusic.MODID + ".gui.logindownload.title"));
        this.musicId = musicId;
        this.onComplete = onComplete;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 渲染背景
        this.renderBackground(graphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 标题
        graphics.drawCenteredString(this.font, "[LoginMusic]", centerX, centerY, 0x00AAFF);

        // 设置渲染类型
        if (error) {
            renderError(graphics, centerX, centerY);
        } else if (completed) {
            renderCompleted(graphics, centerX, centerY);
            if (!callbackTriggered) {
                callbackTriggered = true;
                onComplete.run();
            }
        } else {
            renderDownload(graphics, centerX, centerY);
        }
    }

    // 正在下载
    private void renderDownload(GuiGraphics graphics, int centerX, int centerY) {
        graphics.drawCenteredString(
                this.font,
                Component.translatable(LoginMusic.MODID + ".gui.logindownload.downloading"),
                centerX,
                centerY - 30,
                0xFFFFFF);

        graphics.drawCenteredString(this.font, musicId, centerX, centerY - 10, 0xFFFFFF);
        // 进度条参数
        int barWidth = 200;
        int barHeight = 20;
        int barX = centerX - barWidth / 2;
        int barY = centerY + 20;

        // 背景
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        // 进度条
        int fillWith = (int) (barWidth * progress);
        graphics.fill(barX, barY, barX + fillWith, barY + barHeight, 0xFF00AA00);
        // 进度文字
        String progressText = String.format("%.1f%%", progress * 100);
        graphics.drawCenteredString(this.font, progressText, centerX, barY + 5, 0xFFFFFF);
        // 状态文字
        graphics.drawCenteredString(this.font, status, centerX, barY + 30, 0xAAAAAA);
    }

    // 下载完成
    private void renderCompleted(GuiGraphics graphics, int centerX, int centerY) {
        graphics.drawCenteredString(this.font, "§a✓ 下载完成", centerX, centerY - 20, 0x00FF00);
        graphics.drawCenteredString(this.font, "§e" + musicId, centerX, centerY, 0xFFFFAA);
        graphics.drawCenteredString(this.font, "§7正在播放...", centerX, centerY + 30, 0xAAAAAA);
    }

    // 下载失败的界面
    private void renderError(GuiGraphics graphics, int centerX, int centerY) {
        graphics.drawCenteredString(this.font, "§c✗ 下载失败", centerX, centerY - 20, 0xFF0000);
        graphics.drawCenteredString(this.font, errorMessage, centerX, centerY, 0xFF5555);
        graphics.drawCenteredString(this.font, "§7按ESC进入游戏", centerX, centerY + 40, 0x888888);
    }

    // 线程安全的更新
    public synchronized void updateProgress(float progress, Component status) {
        // 计算progress，保证在[0.0f, 1.0f]
        // max和min别写反了
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
        this.status = status;
    }

    public void setCompleted() { this.completed = true; }

    public void setError(String Message) {
        this.error = true;
        this.errorMessage = Message;
    }

    @Override
    // 是否允许ESC关闭
    public boolean shouldCloseOnEsc() { return error; }
}
