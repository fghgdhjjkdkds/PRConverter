package com.purplerat.prconverter.audio;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.purplerat.prconverter.MainActivity;
import com.purplerat.prconverter.R;

import java.io.File;

public class AudioConvertingService extends Service {
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
                .setContentTitle("Successfully converted")
                .setSmallIcon(R.drawable.ic_baseline_download_done_24)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(false);

        notificationManagerCompat.notify(notificationID, notification.build());
        AudioConvertingPack audioConvertingPack = (AudioConvertingPack)intent.getSerializableExtra("pack");
        if(audioConvertingPack != null){
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
                    }else{
                        finalNotification.setContentTitle("Successfully converted");
                    }
                    Intent intent = new Intent("complete").putExtra("file",file);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    notificationManagerCompat.cancel(1);
                    notificationManagerCompat.notify(1,finalNotification.build());
                    stopSelf();
                }
            })).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restart_audio_converting_service");
        broadcastIntent.setClass(this, AudioConvertingRestarter.class);
        this.sendBroadcast(broadcastIntent);
    }
}
class AudioConvertingRestarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, AudioConvertingService.class));
        }
    }
}

