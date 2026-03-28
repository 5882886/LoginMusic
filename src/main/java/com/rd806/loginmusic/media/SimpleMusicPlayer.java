package com.rd806.loginmusic.media;

import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.config.ClientConfig;
import com.rd806.loginmusic.media.lyric.LyricEntry;
import com.rd806.loginmusic.media.lyric.LyricParser;
import com.rd806.loginmusic.media.lyric.LyricPlayer;
import com.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.sound.sampled.*;
import java.io.File;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SimpleMusicPlayer {
    private static final Minecraft mc = Minecraft.getInstance();

    private static Clip currentClip;
    private static String currentMusicId;
    private static boolean isPlaying = false;
    private static long startTimeMillis;

    private static List<LyricEntry> currentLyrics;
    private static boolean lyricStarted = false;

    private SimpleMusicPlayer() {}

    static {
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        LoginMusic.LOGGER.info("Supported music file:");
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

    // 播放音乐
    public static void playMusic(MusicEntry entry) {
        try {
            LoginMusic.LOGGER.info("Playing: {}", entry.getId());
            stopCurrentMusic();

            currentMusicId = entry.getId();
            isPlaying = true;

            File localFile = LoginMusic.CACHE_DIR.resolve(entry.getName()).toFile();

            if (localFile.exists()) {
                // 获取音频输入流
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(localFile);
                AudioFormat sourceFormat = audioStream.getFormat();


                // 转换为PCM格式（如果需要）
                AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        sourceFormat.getSampleRate(),
                        16,
                        sourceFormat.getChannels(),
                        sourceFormat.getChannels() * 2,
                        sourceFormat.getSampleRate(),
                        false
                );

                // 如果格式不匹配，进行转换
                if (!sourceFormat.matches(targetFormat)) {
                    LoginMusic.LOGGER.info("Changing format...");
                    audioStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                }

                DataLine.Info info = new DataLine.Info(Clip.class, targetFormat);

                if (!AudioSystem.isLineSupported(info)) {
                    LoginMusic.LOGGER.error("Unsupported music file: {}", entry.getName());
                    return;
                }

                Clip clip = (Clip) AudioSystem.getLine(info);

                // 添加播放完成监听
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        if (currentClip == clip) {
                            currentClip = null;
                            currentMusicId = null;
                            isPlaying = false;
                            // 播放结束通知
                            mc.execute(() -> {
                                if (Minecraft.getInstance().player != null) {
                                    Minecraft.getInstance().player.displayClientMessage(
                                            Component.translatable( LoginMusic.MODID + ".message.play_ended", entry.getName()),
                                            false
                                    );
                                }
                            });
                        }
                    }
                });

                clip.open(audioStream);
                // 在MC线程中执行
                mc.execute(() -> {
                    currentClip = clip;
                    clip.start();
                    isPlaying = true;
                    startTimeMillis = System.currentTimeMillis();
                    // 通知玩家
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(
                                Component.translatable(LoginMusic.MODID + ".message.play_music", entry.getName()),
                                false
                        );
                    }
                });

                // 播放歌词
                if (entry.getLyrics() != null && ClientConfig.getAllowLyrics()) {
                    LoginMusic.LOGGER.info("Lyrics prepared!");
                    // 异步播放歌词
                    LyricParser.loadLyricAsync(entry).thenAccept(lyricContent  -> {
                        if (lyricContent != null && !lyricContent.isEmpty()) {
                            currentLyrics = LyricParser.parseLRC(lyricContent);
                            lyricStarted = true;

                            if (isPlaying && startTimeMillis > 0) {
                                LoginMusic.LOGGER.info("Lyrics playing!");
                                // 计算展示歌词与播放开始的间隔时间
                                // 即已播放的时间
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

                } else if (!ClientConfig.getAllowLyrics()) {
                    LoginMusic.LOGGER.warn("Lyrics are disabled!");
                }

                LoginMusic.LOGGER.info("Playing music: {}", entry.getName());
            } else {
                LoginMusic.LOGGER.error("Music not found!");
            }
        } catch (UnsupportedAudioFileException e) {
            LoginMusic.LOGGER.error("Unsupported type: {}", e.getMessage());
        } catch (LineUnavailableException e) {
            LoginMusic.LOGGER.error("Not available: {}", e.getMessage());
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to play music: {} ", entry.getName());
        }
    }

    // 停止播放
    public static void stopCurrentMusic() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
            currentMusicId = null;
            isPlaying = false;
            LyricPlayer.stopLyricDisplay();
        }
    }

    // 获取播放状态
    public static boolean isStopped() { return !isPlaying; }

    // 获取播放的音乐id
    public static String getCurrentMusicId() { return currentMusicId; }
}
