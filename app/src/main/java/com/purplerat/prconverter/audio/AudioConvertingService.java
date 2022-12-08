package com.purplerat.prconverter.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.purplerat.prconverter.MyBroadcastReceiver;
import com.purplerat.prconverter.R;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioConvertingService extends Service {
    private final AtomicBoolean success = new AtomicBoolean(false);
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground();
    }
    private void startForeground()
    {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.notification_id));
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Converting in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(99,notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        success.set(false);
        int notificationID = 1;
        final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(),  getString(R.string.notification_id))
                .setContentTitle("Converting audio")
                .setProgress(100, 0, false)
                .setSmallIcon(R.drawable.ic_baseline_refresh_24)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setOngoing(true);
        final NotificationCompat.Builder finalNotification  = new NotificationCompat.Builder(getApplicationContext(),  getString(R.string.notification_id))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(false);

        AudioConvertingPack audioConvertingPack = (AudioConvertingPack)intent.getSerializableExtra("pack");
        if(audioConvertingPack != null){
            notificationManagerCompat.notify(notificationID, notification.build());
            new Thread(new AudioConverter(getApplicationContext(), audioConvertingPack, new AudioConverter.AudioConverterCallback() {
                @Override
                public void onProgress(int progress) {
                    notification.setProgress(100,progress,false);
                    notificationManagerCompat.notify(1,notification.build());
                    Intent intent = new Intent("progress").putExtra("progress",progress);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }

                @Override
                public void onComplete(File file) {
                    if(file == null){
                        finalNotification.setContentTitle("Error while converting");
                        finalNotification.setSmallIcon(R.drawable.ic_baseline_error_24);
                    }else{
                        finalNotification.setContentTitle("Successfully converted");
                        finalNotification.setSmallIcon(R.drawable.ic_baseline_download_done_24);
                    }
                    notificationManagerCompat.cancel(1);
                    notificationManagerCompat.notify(1,finalNotification.build());
                    Intent intent = new Intent("complete").putExtra("file",file);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    success.set(true);
                    stopSelf();
                }
            })).start();
        }else{
            success.set(true);
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!success.get())sendBroadcast(new Intent("com.purplerat.prconverter.restart_audio_converting").setClass(this, MyBroadcastReceiver.class));
    }
}

