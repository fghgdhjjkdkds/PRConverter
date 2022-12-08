package com.purplerat.prconverter.audio;

import java.io.File;
import java.io.Serializable;

public class AudioConvertingPack implements Serializable {
    private final File importFile;
    private final File exportFile;
    private final AudioStream audioStream;
    private final boolean onlyAudio;
    public AudioConvertingPack(final File importFile, AudioStream audioStream, final File exportFile,final boolean onlyAudio){
        this.importFile = importFile;
        this.audioStream = audioStream;
        this.exportFile = exportFile;
        this.onlyAudio = onlyAudio;
    }

    public File getExportFile() {
        return exportFile;
    }

    public AudioStream getAudioStream() {
        return audioStream;
    }
    public File getImportFile() {
        return importFile;
    }

    public boolean isOnlyAudio() {
        return onlyAudio;
    }
}
