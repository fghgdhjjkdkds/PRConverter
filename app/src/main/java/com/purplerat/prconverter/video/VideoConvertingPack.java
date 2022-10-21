package com.purplerat.prconverter.video;

import com.purplerat.prconverter.audio.AudioStream;

import java.io.File;
import java.io.Serializable;

public class VideoConvertingPack implements Serializable {
    private final File importFile;
    private final VideoStream videoStream;
    private final AudioStream audioStream;
    private final File exportFile;
    public VideoConvertingPack(File importFile,VideoStream videoStream,AudioStream audioStream,File exportFile){
        this.importFile = importFile;
        this.videoStream = videoStream;
        this.audioStream = audioStream;
        this.exportFile = exportFile;
    }

    public AudioStream getAudioStream() {
        return audioStream;
    }

    public File getExportFile() {
        return exportFile;
    }

    public File getImportFile() {
        return importFile;
    }

    public VideoStream getVideoStream() {
        return videoStream;
    }

}
