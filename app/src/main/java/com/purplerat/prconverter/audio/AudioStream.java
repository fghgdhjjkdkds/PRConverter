package com.purplerat.prconverter.audio;

import java.io.File;
import java.io.Serializable;

public class AudioStream implements Serializable {
    private final int bitrate;
    private final int sampleRate;
    private final AudioChannels channel;
    public AudioStream(final int bitrate, final int sampleRate, final AudioChannels channel){
        this.bitrate = bitrate;
        this.sampleRate = sampleRate;
        this.channel = channel;
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
}
