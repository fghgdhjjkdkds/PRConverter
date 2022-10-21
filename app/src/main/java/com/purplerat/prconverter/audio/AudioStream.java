package com.purplerat.prconverter.audio;

import java.io.Serializable;

public class AudioStream implements Serializable {
    private final int bitrate;
    private final int sampleRate;
    private final AudioChannels channel;
    private final AudioFormats audioFormat;
    public AudioStream(final int bitrate, final int sampleRate, final AudioChannels channel,final AudioFormats audioFormat){
        this.bitrate = bitrate;
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.audioFormat = audioFormat;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBitrate() {
        return bitrate;
    }

    public AudioChannels getChannel() {
        return channel;
    }

    public AudioFormats getAudioFormat() {
        return audioFormat;
    }
}
