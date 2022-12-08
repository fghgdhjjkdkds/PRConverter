package com.purplerat.prconverter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.purplerat.prconverter.audio.AudioConvertingService;
import com.purplerat.prconverter.video.VideoConvertingService;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "com.purplerat.prconverter.restart_video_converting":
                Intent videoIntent = new Intent(context, VideoConvertingService.class).setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(videoIntent);
                } else {
                    context.startService(videoIntent);
                }
                break;
            case "com.purplerat.prconverter.restart_audio_converting":
                Intent arrayIntent = new Intent(context, AudioConvertingService.class).setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(arrayIntent);
                } else {
                    context.startService(arrayIntent);
                }
                break;
        }
    }
}
