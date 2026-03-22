package com.rd806.loginmusic.lyric;

import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.music.SimpleMusicPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

// 歌词播放器
public class LyricPlayer {
    private static final Minecraft mc = Minecraft.getInstance();
    private static Timer lyricTimer;
    private static List<LyricEntry> currentLyrics;
    private static long startTime = 0;
    private static LyricEntry lastLyricEntry = null;
    private static boolean isPlaying = false;
    private static AtomicReference<Long> currentPosition = new AtomicReference<>(0L);

    // 开始显示歌词
    public static void startLyricDisplay(List<LyricEntry> lyrics, long startTimeMillis) {
        if (lyrics == null ||  lyrics.isEmpty()) {
            LoginMusic.LOGGER.info("Lyrics is empty");
            return;
        }

        stopLyricDisplay();

        currentLyrics = lyrics;
        startTime = startTimeMillis;
        lastLyricEntry = null;
        isPlaying = true;

        // 启动定时器
        lyricTimer = new Timer("LyricTimer", true);
        lyricTimer.scheduleAtFixedRate(new  TimerTask() {
            @Override
            public void run() {
                if (!isPlaying) { return; }

                long elapsed = System.currentTimeMillis() - startTime;
                currentPosition.set(elapsed);

                LyricEntry currentLyric = LyricParser.getCurrentLyric(currentLyrics, elapsed);

                if (currentLyric != null && (lastLyricEntry == null || !lastLyricEntry.text().equals(currentLyric.text()))) {
                    lastLyricEntry = currentLyric;

                    mc.execute(() -> {
                        if (mc.player != null) {
                            Component lyricMessage = Component.literal("")
                                    .append(Component.literal("♪ "))
                                    .append(Component.literal(currentLyric.text()))
                                    .append(Component.literal(" ♪"));
                            // 在屏幕底部显示歌词
                            mc.player.displayClientMessage(lyricMessage, true);
                        }
                    });
                }
            }
        }, 0, 100);

        // 音乐播放结束，歌词也结束
        if (SimpleMusicPlayer.isStopped()) {
            stopLyricDisplay();
        }

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
        LoginMusic.LOGGER.info("Lyrics is stopped");
    }
}
