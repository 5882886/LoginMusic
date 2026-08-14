package com.github.rd806.loginmusic.media;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.media.layer.MusicInfo;
import com.github.rd806.loginmusic.media.lyric.LyricEntry;
import com.github.rd806.loginmusic.media.lyric.LyricParser;
import com.github.rd806.loginmusic.media.lyric.LyricPlayer;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SimpleMusicPlayer {

    private static final Minecraft mc = Minecraft.getInstance();

    private static Clip currentClip;
    private static MusicEntry musicEntry;
    private static String musicName;

    private static boolean musicPlaying = false;
    private static boolean lyricPlaying = false;

    /* ----- 加载音频逻辑 ----- */
    // 播放音乐，加载和播放音乐分为两个线程
    public static void playMusic(MusicEntry music, PreparedAudio audio, String lyric) {
        musicEntry = music;
        musicName = LoginMusic.removeExtension(musicEntry.getMusicName());
        // 显示加载提示
        mc.execute(() -> startMusic(music, audio, lyric));
        MusicInfo.setMusic(music);
    }

    // 播放音频，在渲染进程进行
    private static void startMusic(MusicEntry music, PreparedAudio preparedAudio, String lyric) {
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
            musicPlaying = true;
            // 播放歌词
            startLyrics(musicEntry, lyric, System.currentTimeMillis());
            // 音频结束操作
            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    stopMusic();
                    if (mc.player != null) {
                        mc.player.displayClientMessage(
                                Component.translatable(LoginMusic.MODID + ".message.play_ended", musicName),
                                false
                        );
                    }
                }
            });
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Failed to play music: {} ", music.getMusicName());
        }
    }

    // 播放歌词
    private static void startLyrics(MusicEntry entry, String lyric, long startTimeMillis) {
        if (ClientConfig.ALLOW_LYRICS.get()) {
            LoginMusic.LOGGER.info("Lyrics prepared!");
            if (lyric != null && !lyric.isEmpty() && !lyricPlaying) {
                List<LyricEntry> currentLyrics = LyricParser.parseLRC(lyric);
                lyricPlaying = true;
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
        MusicInfo.setMusic(null);
        lyricPlaying = false;
        musicPlaying = false;
    }

    // 立即停止播放
    public static void stopCurrentMusic() {
        if (!musicPlaying) { return; }
        stopMusic();
    }

    // 获取播放状态
    public static boolean isStopped() { return !musicPlaying; }
}
