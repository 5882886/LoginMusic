package com.github.rd806.loginmusic.media;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.media.download.PrepareMusic;
import com.github.rd806.loginmusic.media.lyric.LyricEntry;
import com.github.rd806.loginmusic.media.lyric.LyricParser;
import com.github.rd806.loginmusic.media.lyric.LyricPlayer;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
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

    private static SoundEvent soundEvent;

    private static Clip currentClip;
    private static boolean isPlaying = false;

    private static boolean lyricStarted = false;

    // 播放音乐
    public static void playMusic(MusicEntry music, PreparedAudio audio, String lyric) {
        musicEntry = music;
        musicName = LoginMusic.removeExtension(music.getMusicName());
        if (mc.player == null) {
            LoginMusic.LOGGER.warn("Player not found!");
            return;
        }
        if (audio == null) {
            LoginMusic.LOGGER.warn("Music audio not found!");
            return;
        }
        mc.execute(() -> startMusic(music, audio, lyric));
    }

    // 播放音频，在渲染进程进行
    private static void startMusic(MusicEntry entry, PreparedAudio audio, String lyric) {
        try {
            if (mc.player == null) { return; }
            // 尝试播放
            switch (PrepareMusic.type) {
                case SOUND_EVENT -> playMusicFromResources(soundEvent, lyric);
                case PREPARED_AUDIO -> playMusicFromFiles(audio, lyric);
                case DEFAULT -> LoginMusic.LOGGER.error("No audio file found");
            }
            // 显示播放信息
            mc.player.displayClientMessage(
                    Component.translatable(LoginMusic.MODID + ".message.play_music", musicName),
                    false);
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Failed to play music: {} ", entry.getMusicName());
            LoginMusic.LOGGER.error(e.getMessage());
        }
    }

    // 播放资源包中的音乐
    private static void playMusicFromResources(SoundEvent soundEvent, String lyric) {
        SimpleSoundInstance currentMusic = SimpleSoundInstance.forUI(soundEvent, 1.0f, 1.0f);
        Minecraft.getInstance().getSoundManager().play(currentMusic);
        startLyrics(musicEntry, lyric, System.currentTimeMillis());
    }

    // 播放外部音乐
    private static void playMusicFromFiles(PreparedAudio preparedAudio, String lyric) {
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
            startLyrics(musicEntry, lyric, System.currentTimeMillis());
            isPlaying = true;
            // 音频结束操作
            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && isPlaying) {
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
    private static void stopMusic() {
        switch (PrepareMusic.type) {
            case SOUND_EVENT -> {
                soundEvent = null;
                Minecraft.getInstance().getSoundManager().stop();
            }
            case PREPARED_AUDIO -> {
                currentClip.stop();
                currentClip.close();
            }
            case DEFAULT -> {}
        }
        // 停止歌词
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
    public static boolean isPlaying() { return isPlaying; }
}
