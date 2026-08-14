package com.github.rd806.loginmusic.media;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.media.download.DownloadMethod;
import com.github.rd806.loginmusic.media.layer.MusicInfo;
import com.github.rd806.loginmusic.media.lyric.LyricEntry;
import com.github.rd806.loginmusic.media.lyric.LyricParser;
import com.github.rd806.loginmusic.media.lyric.LyricPlayer;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SimpleMusicPlayer {

    private static final Minecraft mc = Minecraft.getInstance();
    private static MusicEntry musicEntry;
    private static String musicName;

    private static Clip currentClip;
    private static boolean musicPlaying = false;
    private static boolean lyricPlaying = false;

    // 播放音乐
    public static void playMusic(MusicEntry music, PreparedAudio audio, String lyric) {
        musicEntry = music;
        musicName = LoginMusic.removeExtension(music.getMusicName());
        if (audio == null) {
            LoginMusic.LOGGER.warn("Music audio not found!");
            return;
        }
        startMusic(audio, lyric);
        MusicInfo.playMusicInfo();
    }

    // 播放外部音乐
    private static void startMusic(PreparedAudio preparedAudio, String lyric) {
        try {
            if (preparedAudio == null) {
                LoginMusic.LOGGER.error("Audio is not available!");
                return;
            }
            // 使用预加载的数据
            currentClip = (Clip) AudioSystem.getLine(preparedAudio.info());
            AudioInputStream stream = new AudioInputStream(
                    new ByteArrayInputStream(preparedAudio.data()),
                    preparedAudio.format(),
                    preparedAudio.data().length / preparedAudio.format().getFrameSize());
            // 播放音频
            currentClip.open(stream);
            currentClip.start();
            // 播放歌词
            startLyric(musicEntry, lyric, System.currentTimeMillis());
            musicPlaying = true;
            // 音频结束操作
            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && musicPlaying) {
                    stopMusic();
                    if (mc.player != null) {
                        LoginMusic.LOGGER.info("Music {} stopped!", musicEntry.getMusicName());
                        mc.player.displayClientMessage(
                                Component.translatable(LoginMusic.MODID + ".message.play_ended", musicName),
                                false);
                    }
                }
            });
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to play music from files.", e);
        }
    }

    // 播放歌词
    private static void startLyric(MusicEntry entry, String lyric, long startTimeMillis) {
        if (ClientConfig.ALLOW_LYRICS.get()) {
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
            LoginMusic.LOGGER.info("Lyrics are not available!");
        }
    }

    // 停止播放
    private static void stopMusic() {
        currentClip.stop();
        currentClip.close();
        // 停止歌词
        LyricPlayer.stopLyricDisplay();
        MusicInfo.stopMusicInfo();
        lyricPlaying = false;
        musicPlaying = false;
        DownloadMethod.stopDownloading();
    }

    // 立即停止播放
    public static void stopCurrentMusic() {
        if (!musicPlaying) { return; }
        stopMusic();
    }

    // 获取播放状态
    public static boolean isPlaying() { return musicPlaying; }
}
