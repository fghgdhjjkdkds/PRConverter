package com.purplerat.prconverter.video;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.purplerat.prconverter.BuildConfig;
import com.purplerat.prconverter.audio.AudioStream;

import java.io.File;
import java.nio.file.Paths;

public class VideoConverter implements Runnable{
    private static final String TAG = "VideoConverter";
    private final Context context;
    private final VideoConvertingPack pack;
    private final VideoConverterCallback videoConverterCallback;
    private volatile boolean stop = false;
    public VideoConverter(final Context context,VideoConvertingPack audioConvertingPack, final VideoConverterCallback videoConverterCallback){
        this.context = context;
        this.pack = audioConvertingPack;
        this.videoConverterCallback = videoConverterCallback;
    }
    @Override
    public void run() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stop = true;
            }
        },new IntentFilter("com.purplerat.prconverter.stop_converting"));
        videoConverterCallback.onComplete(convertVideo());
    }
    private File convertVideo(){
        String command;
        VideoStream videoStream = pack.getVideoStream();
        AudioStream audioStream = pack.getAudioStream();
        File importFile = pack.getImportFile();
        File exportFile = pack.getExportFile();
        if(audioStream == null){
            command = String.format("-i \"%s\" -an %s %s %s %s \"%s\"",
                    importFile.getAbsolutePath(),
                    videoStream.getWidth() == 0? "":String.format("-s %sx%s",videoStream.getWidth(),videoStream.getHeight()),
                    videoStream.getFps() == 0?"": "-r "+videoStream.getFps(),
                    videoStream.getBitrate() == 0? "" : "-b:v " + videoStream.getBitrate(),
                    videoStream.getWidth() == 0 && videoStream.getFps() == 0 && videoStream.getBitrate() == 0 && importFile.getName().substring(importFile.getName().lastIndexOf(".")).equals(exportFile.getName().substring(exportFile.getName().lastIndexOf(".")))? "-c:v copy":"",
                    exportFile.getAbsolutePath());
        }else{
            command = String.format("-i \"%s\" %s %s %s %s %s %s %s %s \"%s\"",
                    importFile.getAbsolutePath(),
                    videoStream.getWidth() == 0? "":String.format("-s %sx%s",videoStream.getWidth(),videoStream.getHeight()),
                    videoStream.getFps() == 0?"": "-r "+videoStream.getFps(),
                    videoStream.getBitrate() == 0? "" : "-b:v " + videoStream.getBitrate(),
                    videoStream.getWidth() == 0 && videoStream.getFps() == 0 && videoStream.getBitrate() == 0 && importFile.getName().substring(importFile.getName().lastIndexOf(".")).equals(exportFile.getName().substring(exportFile.getName().lastIndexOf(".")))? "-c:v copy":"",
                    audioStream.getBitrate() == 0? "" : "-b:a " + audioStream.getBitrate(),
                    audioStream.getSampleRate() == 0? "": "-ar "+audioStream.getSampleRate(),
                    audioStream.getChannel() == null? "" : "-ac "+audioStream.getChannel().getIntValue(),
                    audioStream.getBitrate() == 0 && audioStream.getSampleRate() == 0 && audioStream.getChannel() == null? "-c:a copy":"",
                    exportFile.getAbsolutePath());
        }
        if(BuildConfig.DEBUG)Log.d(TAG,command);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, Uri.fromFile(pack.getImportFile()));
        float time = Float.parseFloat(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        retriever.release();
        Byte[] waiter = new Byte[]{0};
        FFmpegSession fmpegSession = FFmpegKit.executeAsync(command, session-> {
            if(session.getLastReceivedStatistics() == null || stop){
                waiter[0] = -1;
            }else{
                waiter[0] = 1;
            }
        }, log-> {if (BuildConfig.DEBUG) Log.d(TAG,log.getMessage());}, statistics-> {
            int percent = Math.round(statistics.getTime() / time * 100.0f);
            videoConverterCallback.onProgress(percent);
        });
        while(waiter[0]==0){
            if(stop)fmpegSession.cancel();
            try{
                Thread.sleep(250);
            }catch (InterruptedException e){
                if(BuildConfig.DEBUG)Log.e(TAG,e.getMessage());
            }
        }
        if(waiter[0] == -1){
            File file = pack.getExportFile();
            while(file.exists()){
                if(file.delete())break;
            }
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
