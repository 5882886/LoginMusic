package com.github.rd806.loginmusic.media;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.media.lyric.LyricEntry;
import com.github.rd806.loginmusic.media.lyric.LyricParser;
import com.github.rd806.loginmusic.media.lyric.LyricPlayer;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.sound.sampled.*;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SimpleMusicPlayer {
    private static final Minecraft mc = Minecraft.getInstance();

    private static Clip currentClip;
    private static MusicEntry musicEntry;
    private static boolean isPlaying = false;

    private static boolean lyricStarted = false;

    /* ----- 加载音频逻辑 ----- */
    // 播放音乐，加载和播放音乐分为两个线程
    public static void playMusic(MusicEntry music, PreparedAudio audio, String lyric) {
        musicEntry = music;
        // 显示加载提示
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.translatable(LoginMusic.MODID + ".message.loading_music", music.getMusicName()),
                    false);
        }
        mc.execute(() -> startCurrentMusic(music, audio, lyric));
    }

    // 播放音频，在渲染进程进行
    private static void startCurrentMusic(MusicEntry music, PreparedAudio preparedAudio, String lyric) {
        try {
            if (preparedAudio == null) {
                LoginMusic.LOGGER.error("Audio is not available!");
                return;
            }
            // 使用预加载的数据
            currentClip = (Clip) AudioSystem.getLine(preparedAudio.info());
            AudioInputStream stream = new AudioInputStream(
                    new java.io.ByteArrayInputStream(preparedAudio.data()),
                    preparedAudio.format(),
                    preparedAudio.data().length / preparedAudio.format().getFrameSize()
            );
            currentClip.open(stream);
            currentClip.start();
            isPlaying = true;
            // 播放歌词
            startLyrics(musicEntry, lyric, System.currentTimeMillis());
            // 音频结束操作
            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    stopMusic();
                    if (mc.player != null) {
                        mc.player.displayClientMessage(
                                Component.translatable(LoginMusic.MODID + ".message.play_ended", music.getMusicName()),
                                false
                        );
                    }
                }
            });
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        Component.translatable(LoginMusic.MODID + ".message.play_music", music.getMusicName()),
                        false
                );
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Failed to play music: {} ", music.getMusicName());
        }
    }

    // 播放歌词
    private static void startLyrics(MusicEntry entry, String lyric, long startTimeMillis) {
        if (ClientConfig.ALLOW_LYRICS.get()) {
            LoginMusic.LOGGER.info("Lyrics prepared!");
            if (lyric != null && !lyric.isEmpty() && !lyricStarted) {
                List<LyricEntry> currentLyrics = LyricParser.parseLRC(lyric);
                lyricStarted = true;
                // 计算展示歌词与播放开始的间隔时间，即已播放的时间
                if (startTimeMillis > 0) {
                    long elapsedTime = System.currentTimeMillis() - startTimeMillis;
                    LyricPlayer.startLyricDisplay(currentLyrics, elapsedTime);
                }
            } else  {
                LoginMusic.LOGGER.warn("No lyrics found, {}", entry.getMusicName());
            }
        } else {
            LoginMusic.LOGGER.warn("Lyrics not available!");
        }
    }

    // 停止播放
    public static void stopMusic() {
        currentClip.stop();
        currentClip.close();
        LyricPlayer.stopLyricDisplay();
        lyricStarted = false;
        isPlaying = false;
    }

    // 立即停止播放
    public static void stopCurrentMusic() {
        if (!isPlaying) { return; }
        stopMusic();
    }

    // 获取播放状态
    public static boolean isStopped() { return !isPlaying; }
}
