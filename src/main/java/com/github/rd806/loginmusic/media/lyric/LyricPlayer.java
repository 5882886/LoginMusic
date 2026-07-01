package com.github.rd806.loginmusic.media.lyric;

import com.github.rd806.loginmusic.LoginMusic;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class LyricPlayer {
    private static final Minecraft mc = Minecraft.getInstance();
    // 定时任务
    private static Timer lyricTimer;
    private static List<LyricEntry> currentLyrics;
    // 当前播放时间
    private static long startTime;
    private static long currentPlayTime;
    private static LyricEntry lastLyricEntry = null;
    private static boolean isPlaying = false;
    private static final AtomicReference<Long> currentPosition = new AtomicReference<>(0L);

    // 开始显示歌词
    public static void startLyricDisplay(List<LyricEntry> lyrics, long elapsedTime) {
        if (lyrics == null ||  lyrics.isEmpty()) {
            LoginMusic.LOGGER.info("Lyrics is empty");
            return;
        }

        currentLyrics = lyrics;
        // 开始时间为当前时间减去已播放的时间
        startTime = System.currentTimeMillis() - elapsedTime;
        lastLyricEntry = null;
        isPlaying = true;

        // 启动定时器
        // 每500ms检查一次
        lyricTimer = new Timer("LyricTimer", true);
        lyricTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPlaying) { return; }
                // 获取当前应处于的播放位置
                currentPlayTime = System.currentTimeMillis() - startTime;
                currentPosition.set(currentPlayTime);
                LyricEntry currentLyric = LyricParser.getCurrentLyric(currentLyrics, currentPlayTime);

                if (currentLyric != null && (lastLyricEntry == null || !lastLyricEntry.getText().equals(currentLyric.getText()))) {
                    lastLyricEntry = currentLyric;

                    mc.execute(() -> {
                        if (mc.player != null) {
                            String lyricMessage = "♪ " + currentLyric.getText() + " ♪";
                            // 在屏幕底部显示歌词
                            LyricLayer.getInstance().showLyric(lyricMessage);
                        }
                    });
                }
            }
        }, 0, 100);
    }

    // 停止显示歌词
    public static void stopLyricDisplay() {
        if (lyricTimer != null) {
            lyricTimer.cancel();
            lyricTimer = null;
        }
        isPlaying = false;
        currentLyrics = null;
        lastLyricEntry = null;
        // 清除歌词内容
        LyricLayer.getInstance().showLyric(null);
        LoginMusic.LOGGER.info("Lyrics is stopped!");
    }
}
