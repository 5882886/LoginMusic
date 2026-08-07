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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@OnlyIn(Dist.CLIENT)
public class SimpleMusicPlayer {

    private static final Minecraft mc = Minecraft.getInstance();
    private static MusicEntry currentMusic;

    private static PreparedAudio preparedAudio;
    private static SoundEvent soundEvent;

    private static Clip currentClip;
    private static boolean isPlaying = false;

    private static boolean lyricStarted = false;

    // 音频播放线程
    public static final ExecutorService MUSIC_PLAYER = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "LoginMusic MusicPlayer");
        t.setDaemon(true);
        return t;
    });

    static {
        // 获取原生支持的音频格式
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        LoginMusic.LOGGER.info("Supported music file: ");
        for (AudioFileFormat.Type type : types) {
            LoginMusic.LOGGER.info("  - {}", type.getExtension());
        }
        // 检查MP3 SPI是否加载
        try {
            Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader");
            LoginMusic.LOGGER.info("MP3 support loaded!");
        } catch (ClassNotFoundException e) {
            LoginMusic.LOGGER.warn("MP3 support not found!");
        }
    }

    /* ----- 加载音频逻辑 ----- */
    // 播放音乐，加载和播放音乐分为两个线程
    public static void playMusic(MusicEntry music, ByteArrayInputStream inputStream, String lyric) {
        currentMusic = music;
        if (mc.player == null) {
            LoginMusic.LOGGER.warn("Player not found!");
            return;
        }
        if (inputStream == null) {
            LoginMusic.LOGGER.warn("Music stream not found!");
            return;
        }
        // 在音频线程池中加载
        MUSIC_PLAYER.submit(() -> {
            try {
                try {
                    soundEvent = PrepareMusic.findMusic(music);
                } catch (Exception e) {
                    preparedAudio = PrepareMusic.prepareAudio(inputStream);
                }
                // 切换到渲染线程播放
                mc.execute(() -> startCurrentMusic(music, lyric));
            } catch (Exception e) {
                LoginMusic.LOGGER.error("Failed to load audio", e);
                mc.execute(() -> mc.player.displayClientMessage(
                        Component.translatable(LoginMusic.MODID + ".message.load_failed", music.getMusicName()),
                        false
                ));
            }
        });
    }

    // 播放音频，在渲染进程进行
    private static void startCurrentMusic(MusicEntry entry, String lyric) {
        try {
            if (mc.player == null) { return; }
            // 尝试播放
            switch (PrepareMusic.type) {
                case SOUND_EVENT -> playMusicFromResources(soundEvent);
                case PREPARED_AUDIO -> playMusicFromFiles(preparedAudio, lyric);
                case DEFAULT -> LoginMusic.LOGGER.error("No audio file found");
            }
            // 显示播放信息
            mc.player.displayClientMessage(
                    Component.translatable(LoginMusic.MODID + ".message.play_music", entry.getMusicName()),
                    false);
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Failed to play music: {} ", entry.getMusicName());
            LoginMusic.LOGGER.error(e.getMessage());
        }
    }

    // 播放资源包中的音乐
    private static void playMusicFromResources(SoundEvent soundEvent) {
        SimpleSoundInstance currentMusic = SimpleSoundInstance.forUI(soundEvent, 1.0f, 1.0f);
        Minecraft.getInstance().getSoundManager().play(currentMusic);
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
            long startTimeMillis = System.currentTimeMillis();
            startLyrics(currentMusic, lyric, startTimeMillis);
            isPlaying = true;
            // 音频结束操作
            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && isPlaying) {
                    stopMusic();
                    if (mc.player != null) {
                        LoginMusic.LOGGER.info("Music {} stopped!", currentMusic.getMusicName());
                        mc.player.displayClientMessage(
                                Component.translatable(LoginMusic.MODID + ".message.play_ended", currentMusic.getMusicName()),
                                false);
                    }
                }
            });
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to play music from files, {}", e.getMessage());
        }
    }

    // 播放歌词
    private static void startLyrics(MusicEntry entry, String lyric, long startTimeMillis) {
        if (entry.getLyricName() != null && ClientConfig.ALLOW_LYRICS.get()) {
            LoginMusic.LOGGER.info("Lyrics prepared!");
            if (lyric != null && !lyric.isEmpty() && !lyricStarted) {
                List<LyricEntry> currentLyrics = LyricParser.parseLRC(lyric);
                lyricStarted = true;
                if (startTimeMillis > 0) {
                    LoginMusic.LOGGER.info("Lyrics playing!");
                    // 计算展示歌词与播放开始的间隔时间，即已播放的时间
                    long elapsedTime = System.currentTimeMillis() - startTimeMillis;
                    LyricPlayer.startLyricDisplay(currentLyrics, elapsedTime);
                } else  {
                    LoginMusic.LOGGER.warn("No lyrics found!");
                }
            }
        } else if (!ClientConfig.ALLOW_LYRICS.get()) {
            LoginMusic.LOGGER.warn("Lyrics are disabled!");
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
