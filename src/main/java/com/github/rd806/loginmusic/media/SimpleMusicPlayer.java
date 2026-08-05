package com.github.rd806.loginmusic.media;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.media.lyric.LyricEntry;
import com.github.rd806.loginmusic.media.lyric.LyricParser;
import com.github.rd806.loginmusic.media.lyric.LyricPlayer;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@OnlyIn(Dist.CLIENT)
public class SimpleMusicPlayer {
    private static final Minecraft mc = Minecraft.getInstance();
    private static MusicEntry currentMusicEntry;

    private static PreparedAudio preparedAudio;
    private static SoundEvent soundEvent;

    private static Clip currentClip;
    private static boolean isPlaying = false;
    private static long startTimeMillis;

    private static List<LyricEntry> currentLyrics;
    private static boolean lyricStarted = false;

    // 音频加载线程
    private static final ExecutorService AUDIO_LOADER = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "AudioLoader");
        t.setDaemon(true);
        return t;
    });

    private record PreparedAudio(byte[] data, AudioFormat format, DataLine.Info info) { }

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
    public static void playMusic(MusicEntry entry) {
        stopCurrentMusic();
        currentMusicEntry = entry;

        // 显示加载提示
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.translatable(LoginMusic.MODID + ".message.loading_music", currentMusicEntry.getMusic()),
                    false
            );
        }

        // 在音频线程池中加载
        AUDIO_LOADER.submit(() -> {
            try {
                // 加载音频数据
                try {
                    soundEvent = findMusic(currentMusicEntry);
                } catch (Exception e) {
                    preparedAudio = prepareAudio(currentMusicEntry);
                }
                // 切换到渲染线程播放
                mc.execute(() -> startCurrentMusic(currentMusicEntry));
            } catch (Exception e) {
                LoginMusic.LOGGER.error("Failed to load audio", e);
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.displayClientMessage(
                                Component.translatable(LoginMusic.MODID + ".message.load_failed", currentMusicEntry.getMusic()),
                                false
                        );
                    }
                });
            }
        });
    }

    // 准备已注册到游戏内的音乐
    private static SoundEvent findMusic(MusicEntry entry) {
        ResourceLocation music = ResourceLocation.parse(entry.getMusic());
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(music);
        if (soundEvent != null) {
            LoginMusic.LOGGER.info("Found audio music for {}: {}", entry.getMusic(), soundEvent);
            return soundEvent;
        }
        return null;
    }

    // 准备外部音乐
    private static PreparedAudio prepareAudio(MusicEntry entry) {
        File localFile = LoginMusic.MUSICS_DIR.resolve(entry.getMusic()).toFile();
        AudioInputStream audioStream;

        try {
            if (localFile.exists()) {
                audioStream = AudioSystem.getAudioInputStream(localFile);
            } else {
                URI uri = new URI(entry.getMusicUrl());
                URL url = uri.toURL();
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(connection.getInputStream());
                audioStream = AudioSystem.getAudioInputStream(bufferedInputStream);
            }

            // 转换格式
            AudioFormat sourceFormat = audioStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false
            );
            if (!sourceFormat.matches(targetFormat)) {
                audioStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
            }
            DataLine.Info info = new DataLine.Info(Clip.class, targetFormat);
            if (!AudioSystem.isLineSupported(info)) {
                throw new UnsupportedAudioFileException("Audio format not supported");
            }
            // 可选：预加载音频数据到字节数组，减少Clip.open()时间
            byte[] audioData = audioStream.readAllBytes();

            return new PreparedAudio(audioData, targetFormat, info);
        } catch (UnsupportedAudioFileException e) {
            LoginMusic.LOGGER.error("Unsupported type: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to play music: {} ", entry.getMusic());
            return null;
        }
    }

    // 播放音频，在渲染进程进行
    private static void startCurrentMusic(MusicEntry entry) {
        try {
            if (mc.player == null) { return; }

            if (soundEvent != null) {
                SimpleSoundInstance currentMusic = SimpleSoundInstance.forUI(soundEvent, 1.0f, 1.0f);
                Minecraft.getInstance().getSoundManager().play(currentMusic);
            } else {
                Clip clip = (Clip) AudioSystem.getLine(preparedAudio.info);
                // 音频结束操作
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        if (currentClip == clip) {
                            stopCurrentMusic();
                            mc.player.displayClientMessage(
                                    Component.translatable(LoginMusic.MODID + ".message.play_ended", entry.getMusic()),
                                    false
                            );
                        }
                    }
                });
                // 使用预加载的数据
                AudioInputStream stream = new AudioInputStream(
                        new java.io.ByteArrayInputStream(preparedAudio.data),
                        preparedAudio.format,
                        preparedAudio.data.length / preparedAudio.format.getFrameSize()
                );
                clip.open(stream);
                currentClip = clip;
                clip.start();
                startTimeMillis = System.currentTimeMillis();
                // 播放歌词
                playLyric(entry);
                isPlaying = true;
            }
            // 显示播放信息
            mc.player.displayClientMessage(
                    Component.translatable(LoginMusic.MODID + ".message.play_music", entry.getMusic()),
                    false
            );
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Failed to play music: {} ", entry.getMusic());
            LoginMusic.LOGGER.error(e.getMessage());
        }
    }

    // 播放歌词
    private static void playLyric(MusicEntry entry) {
        if (entry.getLyric() != null && ClientConfig.ALLOW_LYRICS.get()) {
            LoginMusic.LOGGER.info("Lyrics prepared!");
            // 异步播放歌词
            LyricParser.loadLyricAsync(entry).thenAccept(lyricContent  -> {
                if (lyricContent != null && !lyricContent.isEmpty() && !lyricStarted) {
                    currentLyrics = LyricParser.parseLRC(lyricContent);
                    lyricStarted = true;

                    if (isPlaying && startTimeMillis > 0) {
                        LoginMusic.LOGGER.info("Lyrics playing!");
                        // 计算展示歌词与播放开始的间隔时间，即已播放的时间
                        long elapsedTime = System.currentTimeMillis() - startTimeMillis;
                        LyricPlayer.startLyricDisplay(currentLyrics, elapsedTime);
                    } else  {
                        LoginMusic.LOGGER.warn("No lyrics found!");
                    }
                }
            }).exceptionally(throwable -> {
                LoginMusic.LOGGER.warn("Error loading lyrics!", throwable);
                return null;
            });
        } else if (!ClientConfig.ALLOW_LYRICS.get()) {
            LoginMusic.LOGGER.warn("Lyrics are disabled!");
        }
    }

    // 停止播放
    public static void stopCurrentMusic() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
            currentMusicEntry = null;
            preparedAudio = null;
            isPlaying = false;
            LyricPlayer.stopLyricDisplay();
            lyricStarted = false;
        } else if (soundEvent != null) {
            soundEvent = null;
            Minecraft.getInstance().getSoundManager().stop();
        }
    }

    // 获取播放状态
    public static boolean isStopped() { return !isPlaying; }
}
