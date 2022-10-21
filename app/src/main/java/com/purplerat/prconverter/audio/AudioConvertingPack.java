package com.purplerat.prconverter.audio;

import java.io.File;
import java.io.Serializable;

public class AudioConvertingPack implements Serializable {
    private final File importFile;
    private final File exportFile;
    private final AudioStream audioStream;
    public AudioConvertingPack(final File importFile, AudioStream audioStream, final File exportFile){
        this.importFile = importFile;
        this.audioStream = audioStream;
        this.exportFile = exportFile;
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
}
