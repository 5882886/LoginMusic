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
        // 标题
        graphics.drawCenteredString(this.font, "[LoginMusic]", centerX, centerY, 0x00AAFF);
        // 设置渲染类型
        switch (audioStatus) {
            case COMPLETED -> renderCompleted(graphics, centerX, centerY);
            case ERROR -> renderError(graphics, centerX, centerY);
            case DOWNLOAD -> renderDownload(graphics, centerX, centerY);
        }

        if (audioStatus.equals(Status.COMPLETED) &&  lyricStatus.equals(Status.COMPLETED)) {
            onComplete.run();
        }
    }

    // 正在下载
    private void renderDownload(GuiGraphics graphics, int centerX, int centerY) {
        graphics.drawCenteredString(
                this.font,
                Component.translatable(LoginMusic.MODID + ".gui.download.downloading"),
                centerX,
                centerY - 30,
                0xFFFFFF);

        graphics.drawCenteredString(this.font, music.getMusicName(), centerX, centerY - 10, 0xFFFFFF);
        // 进度条参数
        // 组件信息
        int barHeight = 20;
        int barWidth = 200;
        int barX = centerX - barWidth / 2;
        int barY = centerY + 20;
        // 背景
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        graphics.fill(barX, barY + barHeight + 5, barX + barWidth, barY + barHeight, 0xFF333333);
        // 进度条
        int audioWidth = (int) (barWidth * audioProgress);
        int lyricWidth = (int) (barWidth * lyricProgress);
        graphics.fill(
                barX, barY,
                barX + audioWidth, barY + barHeight,
                0xFF00AA00);
        graphics.fill(
                barX, barY + barHeight + 5,
                barX + lyricWidth, barY + 2* barHeight + 5,
                0xFF00AA00);
        // 状态文字
        graphics.drawCenteredString(this.font, audioStatusMessage, centerX, barY + 5, 0xFFFFFF);
        graphics.drawCenteredString(this.font, lyricStatusMessage, centerX, barY + barHeight + 10, 0xFFFFFF);
    }

    // 下载完成
    private void renderCompleted(GuiGraphics graphics, int centerX, int centerY) {
        graphics.drawCenteredString(this.font, "§a✓ 下载完成", centerX, centerY - 20, 0x00FF00);
        graphics.drawCenteredString(this.font, "§e" + music.getId(), centerX, centerY, 0xFFFFAA);
        graphics.drawCenteredString(this.font, "§7正在播放...", centerX, centerY + 30, 0xAAAAAA);
    }

    // 下载失败的界面
    private void renderError(GuiGraphics graphics, int centerX, int centerY) {
        graphics.drawCenteredString(this.font, "§c✗ 下载失败", centerX, centerY - 20, 0xFF0000);
        graphics.drawCenteredString(this.font, errorMessage, centerX, centerY, 0xFF5555);
        graphics.drawCenteredString(this.font, "§7按ESC进入游戏", centerX, centerY + 40, 0x888888);
    }

    // 线程安全的更新
    public synchronized void updateAudioProgress(float progress, Component status) {
        // 计算progress，保证在[0.0f, 1.0f]，max和min别写反了
        this.audioProgress = Math.max(0.0f, Math.min(1.0f, progress));
        this.audioStatusMessage = status;
    }
    public synchronized void updateLyricProgress(float progress, Component status) {
        this.lyricProgress = Math.max(0.0f, Math.min(1.0f, progress));
        this.lyricStatusMessage = status;
    }

    public void setAudioCompleted() {
        this.audioStatus = Status.COMPLETED;
    }

    public void setLyricCompleted() {
        this.lyricStatus = Status.COMPLETED;
    }

    public void setError(String Message) {
        this.audioStatus = Status.ERROR;
        this.errorMessage = Message;
    }

    // 是否允许ESC关闭
    @Override
    public boolean shouldCloseOnEsc() {
        return audioStatus.equals(Status.ERROR);
    }
}
