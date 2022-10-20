package com.purplerat.prconverter.video;

import java.io.Serializable;

public class VideoStream implements Serializable {
    private final int bitrate;
    private final int width;
    private final int height;
    private final int fps;
    private final VideoFormats videoFormat;
    public VideoStream(int bitrate,int width,int height,int fps,VideoFormats videoFormat){
        this.bitrate = bitrate;
        this.fps = fps;
        this.width = width;
        this.height = height;
        this.videoFormat = videoFormat;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getFps() {
        return fps;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public VideoFormats getVideoFormat() {
        return videoFormat;
    }
}
