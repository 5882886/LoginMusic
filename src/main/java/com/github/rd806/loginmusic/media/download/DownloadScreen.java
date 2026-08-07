package com.github.rd806.loginmusic.media.download;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

// 音乐下载界面
public class DownloadScreen extends Screen {

    public enum Status {
        DOWNLOAD,
        COMPLETED,
        ERROR
    }

    private static final int barWidth = 200;
    private static final int barHeight = 20;
    private static int barX;
    private static int barY;

    private final MusicEntry music;
    private final Runnable onComplete;
    // 进度信息
    private volatile Status audioStatus;
    private volatile Status lyricStatus;
    private volatile Component audioStatusMessage = Component.translatable(LoginMusic.MODID + ".gui.download.start");
    private volatile Component lyricStatusMessage = Component.translatable(LoginMusic.MODID + ".gui.download.start");
    private volatile float audioProgress;
    private volatile float lyricProgress;

    private volatile String errorMessage = "";

    public DownloadScreen(MusicEntry music, Runnable onComplete) {
        super(Component.translatable(LoginMusic.MODID + ".gui.download.title"));
        this.music = music;
        this.audioStatus = Status.DOWNLOAD;
        this.lyricStatus = Status.DOWNLOAD;
        this.onComplete = onComplete;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        barX = centerX - barWidth / 2;
        barY = centerY + 20;
        // 标题
        graphics.drawCenteredString(this.font, "[LoginMusic]", centerX, centerY, 0x00AAFF);
        graphics.drawCenteredString(
                this.font,
                Component.translatable(LoginMusic.MODID + ".gui.download.downloading"),
                centerX,
                centerY - 30,
                0xFFFFFF);
        graphics.drawCenteredString(this.font, music.getMusicName(), centerX, centerY - 10, 0xFFFFFF);
        // 设置渲染类型
        switch (audioStatus) {
            case COMPLETED -> renderAudioCompleted(graphics, centerX);
            case ERROR -> renderAudioError(graphics, centerX);
            case DOWNLOAD -> renderAudioDownload(graphics, centerX);
        }
        switch (lyricStatus) {
            case COMPLETED -> renderLyricCompleted(graphics, centerX);
            case ERROR -> renderLyricError(graphics, centerX);
            case DOWNLOAD -> renderLyricDownload(graphics, centerX);
        }

        if (audioStatus.equals(Status.COMPLETED) &&  lyricStatus.equals(Status.COMPLETED)) {
            onComplete.run();
        }
    }

    // 正在下载
    private void renderAudioDownload(GuiGraphics graphics, int centerX) {
        // 背景
        graphics.fill(
                barX, barY,
                barX + barWidth, barY + barHeight, 0xFF333333);
        // 进度条
        int audioWidth = (int) (barWidth * audioProgress);
        graphics.fill(
                barX, barY,
                barX + audioWidth, barY + barHeight,
                0xFF00AA00);
        // 状态文字
        graphics.drawCenteredString(this.font, audioStatusMessage, centerX, barY + 5, 0xFFFFFF);
    }
    private void renderLyricDownload(GuiGraphics graphics, int centerX) {
        // 背景
        graphics.fill(
                barX, barY + barHeight + 5,
                barX + barWidth, barY +  2* barHeight + 5, 0xFF333333);
        // 进度条
        int lyricWidth = (int) (barWidth * lyricProgress);
        graphics.fill(
                barX, barY + barHeight + 5,
                barX + lyricWidth, barY + 2 * barHeight + 5,
                0xFF00AA00);
        // 状态文字
        graphics.drawCenteredString(this.font, lyricStatusMessage, centerX, barY + barHeight + 10, 0xFFFFFF);
    }

    // 下载完成
    private void renderAudioCompleted(GuiGraphics graphics, int centerX) {
        graphics.drawCenteredString(this.font, "§a✓ 下载完成", centerX, barY + 5, 0x00FF00);
    }
    private void renderLyricCompleted(GuiGraphics graphics, int centerX) {
        graphics.drawCenteredString(this.font, "§a✓ 下载完成", centerX, barY + barHeight + 10, 0x00FF00);
    }

    // 下载失败的界面
    private void renderAudioError(GuiGraphics graphics, int centerX) {
        graphics.drawCenteredString(
                this.font, "§c✗ 下载失败" + errorMessage,
                centerX, barY + 50, 0xFF0000);
    }
    private void renderLyricError(GuiGraphics graphics, int centerX) {
        graphics.drawCenteredString(
                this.font, "§c✗ 下载失败" + errorMessage,
                centerX, barY + barHeight + 10, 0xFF0000);
    }

    // 线程安全的更新
    // 计算progress，保证在[0.0f, 1.0f]，max和min别写反了
    public synchronized void updateAudioProgress(float progress, Component status) {
        this.audioProgress = Math.max(0.0f, Math.min(1.0f, progress));
        this.audioStatusMessage = status;
    }
    public synchronized void updateLyricProgress(float progress, Component status) {
        this.lyricProgress = Math.max(0.0f, Math.min(1.0f, progress));
        this.lyricStatusMessage = status;
    }

    public void setAudioCompleted() { this.audioStatus = Status.COMPLETED; }

    public void setLyricCompleted() { this.lyricStatus = Status.COMPLETED; }

    public void setError(String Message) {
        this.audioStatus = Status.ERROR;
        this.errorMessage = Message;
    }

    // 是否允许ESC关闭
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
