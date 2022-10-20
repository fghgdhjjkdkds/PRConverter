package com.purplerat.prconverter.audio;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.Log;
import com.arthenica.ffmpegkit.LogCallback;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import com.purplerat.prconverter.UriUtils;

import java.io.File;

public class AudioConverter implements Runnable{
    private final Context context;
    private final AudioConvertingPack pack;
    private final AudioConverterCallback audioConverterCallback;
    public AudioConverter(final Context context,AudioConvertingPack audioConvertingPack, final AudioConverterCallback audioConverterCallback){
        this.context = context;
        this.pack = audioConvertingPack;
        this.audioConverterCallback = audioConverterCallback;
    }
    @Override
    public void run() {
        audioConverterCallback.onComplete(convertAudio());
    }
    private File convertAudio(){
        String command;
        AudioStream audioStream= pack.getAudioStream();
        try {
            command = String.format("-i \"%s\" -b:a %s -ar %s -ac %s \"%s\"",
                    pack.getImportFile().getAbsolutePath(),
                    audioStream.getBitrate(),
                    audioStream.getSampleRate(),
                    audioStream.getChannel().getIntValue(),
                    pack.getExportFile().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, Uri.fromFile(pack.getImportFile()));
        float time = Float.parseFloat(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        retriever.release();
        final Statistics[] waiter = new Statistics[1];
        FFmpegKit.executeAsync(command, session-> {
            synchronized (waiter){
                waiter[0] = session.getLastReceivedStatistics();
                waiter.notify();
            }
        }, log-> {
            System.out.println(log.getMessage());
        }, statistics-> {
            int percent = Math.round(statistics.getTime() / time * 100.0f);
            audioConverterCallback.onProgress(percent);
        });
        synchronized (waiter){
            try {
                waiter.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(waiter[0] == null){
            pack.getExportFile().delete();
            return null;
        }else{
            MediaScannerConnection.scanFile(context,new String[]{pack.getExportFile().getPath()},new String[] {getMimeType(Uri.fromFile(pack.getExportFile()))},null);
            return pack.getExportFile();
        }
    }
    private String getMimeType(Uri uri){
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    public interface AudioConverterCallback{
        void onProgress(int progress);
        void onComplete(File file);
    }
}
