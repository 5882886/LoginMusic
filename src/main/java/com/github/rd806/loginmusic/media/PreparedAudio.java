package com.github.rd806.loginmusic.media;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;

@OnlyIn(Dist.CLIENT)
public record PreparedAudio(byte[] data, AudioFormat format, DataLine.Info info) {}

