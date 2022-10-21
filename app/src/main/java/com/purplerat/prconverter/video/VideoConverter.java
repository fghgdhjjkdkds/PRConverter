package com.purplerat.prconverter.video;

import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.Statistics;
import com.purplerat.prconverter.audio.AudioStream;

import java.io.File;

public class VideoConverter implements Runnable{
    private final Context context;
    private final VideoConvertingPack pack;
    private final VideoConverterCallback videoConverterCallback;
    public VideoConverter(final Context context,VideoConvertingPack audioConvertingPack, final VideoConverterCallback videoConverterCallback){
        this.context = context;
        this.pack = audioConvertingPack;
        this.videoConverterCallback = videoConverterCallback;
    }
    @Override
    public void run() {
        videoConverterCallback.onComplete(convertVideo());
    }
    private File convertVideo(){
        String command;
        VideoStream videoStream = pack.getVideoStream();
        AudioStream audioStream = pack.getAudioStream();
        if(audioStream == null){
            command = String.format("-i \"%s\" -an -s %sx%s -r %s -b:v %s \"%s\"",
                    pack.getImportFile().getAbsolutePath(),
                    videoStream.getWidth(),
                    videoStream.getHeight(),
                    videoStream.getFps(),
                    videoStream.getBitrate(),
                    pack.getExportFile().getAbsolutePath());
        }else{
            command = String.format("-i \"%s\" -s %sx%s -r %s -b:v %s -b:a %s -ar %s -ac %s \"%s\"",
                    pack.getImportFile().getAbsolutePath(),
                    videoStream.getWidth(),
                    videoStream.getHeight(),
                    videoStream.getFps(),
                    videoStream.getBitrate(),
                    audioStream.getBitrate(),
                    audioStream.getSampleRate(),
                    audioStream.getChannel().getIntValue(),
                    pack.getExportFile().getAbsolutePath());
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
        }, log->
            System.out.println(log.getMessage())
        , statistics-> {
            int percent = Math.round(statistics.getTime() / time * 100.0f);
            videoConverterCallback.onProgress(percent);
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
    public interface VideoConverterCallback{
        void onProgress(int progress);
        void onComplete(File file);
    }
}
