package com.purplerat.prconverter.audio;

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

import java.io.File;

public class AudioConverter implements Runnable{
    private static final String TAG = "AudioConverter";
    private final Context context;
    private final AudioConvertingPack pack;
    private final AudioConverterCallback audioConverterCallback;
    private volatile boolean stop = false;
    public AudioConverter(final Context context,AudioConvertingPack audioConvertingPack, final AudioConverterCallback audioConverterCallback){
        this.context = context;
        this.pack = audioConvertingPack;
        this.audioConverterCallback = audioConverterCallback;
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
        audioConverterCallback.onComplete(convertAudio());
    }
    private File convertAudio(){
        String command;
        AudioStream audioStream= pack.getAudioStream();
        File importFile = pack.getImportFile();
        File exportFile = pack.getExportFile();
        try {
            command = String.format("-i \"%s\" %s %s %s %s \"%s\"",
                    importFile.getAbsolutePath(),
                    audioStream.getBitrate() == 0? "":"-b:a "+audioStream.getBitrate(),
                    audioStream.getSampleRate() == 0? "":"-ar "+audioStream.getSampleRate(),
                    audioStream.getChannel() == null? "":"-ac "+audioStream.getChannel().getIntValue(),
                    pack.isOnlyAudio() && audioStream.getBitrate() == 0 && audioStream.getSampleRate() == 0 && audioStream.getChannel() == null && importFile.getName().substring(importFile.getName().lastIndexOf(".")).equals(exportFile.getName().substring(exportFile.getName().lastIndexOf(".")))?"-c:a copy":"",
                    exportFile.getAbsolutePath());
        } catch (Exception e) {
            if(BuildConfig.DEBUG) Log.e(TAG,e.getMessage());
            return null;
        }
        if(BuildConfig.DEBUG)Log.d(TAG,command);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, Uri.fromFile(pack.getImportFile()));
        float time = Float.parseFloat(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        retriever.release();
        final Byte[] waiter = new Byte[]{0};
        FFmpegSession fFmpegSession = FFmpegKit.executeAsync(command, session-> {
            if(session.getLastReceivedStatistics() == null || stop){
                waiter[0] = -1;
            }else{
                waiter[0] = 1;
            }
        }, log-> {if(BuildConfig.DEBUG)Log.d(TAG,log.getMessage());}, statistics-> {
            int percent = Math.round(statistics.getTime() / time * 100.0f);
            audioConverterCallback.onProgress(percent);
        });
        while(waiter[0]==0){
            if(stop)fFmpegSession.cancel();
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
    public interface AudioConverterCallback{
        void onProgress(int progress);
        void onComplete(File file);
    }
}
