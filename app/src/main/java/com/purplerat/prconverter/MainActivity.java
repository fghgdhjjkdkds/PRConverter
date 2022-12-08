package com.purplerat.prconverter;

import static android.os.Build.VERSION.SDK_INT;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arthenica.ffmpegkit.FFprobeKit;
import com.google.android.material.snackbar.Snackbar;
import com.purplerat.prconverter.audio.AudioChannels;
import com.purplerat.prconverter.audio.AudioConverterDialog;
import com.purplerat.prconverter.audio.AudioConvertingPack;
import com.purplerat.prconverter.audio.AudioConvertingService;
import com.purplerat.prconverter.audio.AudioFormatMap;
import com.purplerat.prconverter.audio.AudioFormats;
import com.purplerat.prconverter.audio.AudioStream;
import com.purplerat.prconverter.image.ImageConverter;
import com.purplerat.prconverter.image.ImageConverterDialog;
import com.purplerat.prconverter.image.ImageFormats;
import com.purplerat.prconverter.video.VideoConvertingDialog;
import com.purplerat.prconverter.video.VideoConvertingPack;
import com.purplerat.prconverter.video.VideoConvertingService;
import com.purplerat.prconverter.video.VideoFormats;
import com.purplerat.prconverter.video.VideoFormatsMap;
import com.purplerat.prconverter.video.VideoStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private View rootView;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private final static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);
        rootView = findViewById(android.R.id.content).getRootView();
        setSupportActionBar(rootView.findViewById(R.id.toolbar));
        Button convert_video_button = rootView.findViewById(R.id.convert_video_button);
        Button convert_audio_button = rootView.findViewById(R.id.convert_audio_button);
        Button convert_image_button = rootView.findViewById(R.id.convert_image_button);
        convert_video_button.setOnClickListener(v->videoLauncher.launch("video/*"));
        convert_audio_button.setOnClickListener(v->audioLauncher.launch("audio/*"));
        convert_image_button.setOnClickListener(v->imageLauncher.launch("image/*"));
        if(isServiceRunning(AudioConvertingService.class) || isServiceRunning(VideoConvertingService.class)){
            Intent intent = new Intent(this, ConvertingActivity.class);
            startActivity(intent);
            return;
        }
        requestPermission();
        createNotificationChannel();
    }
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private final ActivityResultLauncher<String> imageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try {
                BitmapFactory.decodeStream(getContentResolver().openInputStream(result), null, options);
            } catch (FileNotFoundException e) {
                if(BuildConfig.DEBUG) Log.e(TAG,e.getMessage());
                Snackbar.make(rootView,"ERROR while getting file",Snackbar.LENGTH_SHORT).show();
                return;
            }
            String oldName = UriUtils.getFileName(this,result);

            ImageFormats format;
            switch (oldName.substring(oldName.lastIndexOf(".")+1).toLowerCase()){
                case "png":
                    format = ImageFormats.PNG;
                    break;
                case "jpeg":
                case "jpg":
                    format = ImageFormats.JPEG;
                    break;
                case "webp":
                    format = ImageFormats.WEBP;
                    break;
                default:
                    Snackbar.make(rootView,"ERROR while getting file",Snackbar.LENGTH_SHORT).show();
                    return;
            }
            new ImageConverterDialog(options.outWidth, options.outHeight, format, (x, y, imageFormat)-> {
                if(imageFormat== null){
                    return;
                }
                convertImage(result,x,y,oldName.substring(0,oldName.lastIndexOf(".")),imageFormat.getExtension());
            }).show(getSupportFragmentManager(),"image_converting_dialog");
        }
    });
    private final ActivityResultLauncher<String> audioLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if(result!= null){
            MediaExtractor mediaExtractor = new MediaExtractor();
            int oldBitrate;
            int oldSampleRate;
            AudioChannels oldChannel;
            String fileName = UriUtils.getFileName(this,result);
            String ext = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
            AudioFormats oldAudioFormat = AudioFormatMap.getAudioFormat(ext);
            if(oldAudioFormat == null){
                Snackbar.make(rootView,"ERROR while getting file",Snackbar.LENGTH_SHORT).show();
                return;
            }
            try {
                mediaExtractor.setDataSource(this,result,null);
                MediaFormat mf = mediaExtractor.getTrackFormat(0);
                oldBitrate = mf.getInteger(MediaFormat.KEY_BIT_RATE);
                oldSampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                if(mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 2){
                    oldChannel = AudioChannels.STEREO;
                }else{
                    oldChannel = AudioChannels.MONO;
                }
            } catch (Exception e) {
                if(BuildConfig.DEBUG) Log.e(TAG,e.getMessage());
                Snackbar.make(rootView,"ERROR while getting file",Snackbar.LENGTH_SHORT).show();
                return;
            }
            new AudioConverterDialog(oldBitrate, oldSampleRate, oldChannel, oldAudioFormat, (bitrate, sampleRate,channel, audioFormat)-> {
                if(audioFormat== null)
                    return;
                String oldName = UriUtils.getFileName(this,result);
                convertAudio(result,new AudioStream(bitrate,sampleRate,channel,audioFormat),oldName.substring(0,oldName.lastIndexOf(".")),audioFormat.getValue(),true);

            }).show(getSupportFragmentManager(),"audio_converter_dialog");
        }
    });
    private final ActivityResultLauncher<String> videoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            int width = 0;
            int height = 0;
            int fps = 0;
            int videoBitrate = 0;
            String buf;
            File file;
            try {
                file = UriUtils.getFileFromUri(this, result);
            }catch (Exception e) {
                if(BuildConfig.DEBUG) Log.e(TAG,e.getMessage());
                return;
            }
            String command = String.format("-v quiet -show_entries stream=codec_type -of csv=p=0 \"%s\"", Objects.requireNonNull(file).getAbsolutePath());
            String output = FFprobeKit.execute(command).getAllLogsAsString();
            VideoStream vs;
            AudioStream as;
            if(output.contains("video")) {
                String command1 = String.format("-v quiet -select_streams v:0 -show_entries stream=width,height,bit_rate,r_frame_rate -of default=noprint_wrappers=1 \"%s\"", file.getAbsolutePath());
                for (String line : FFprobeKit.execute(command1).getAllLogsAsString().split("\n")) {
                    buf = line.substring(line.lastIndexOf("=") + 1);
                    if (line.contains("width=")) {
                        if (!buf.equals("N/A")) {
                            width = Integer.parseInt(buf);
                        }
                    } else if (line.contains("height=")) {
                        if (!buf.equals("N/A")) {
                            height = Integer.parseInt(buf);
                        }
                    } else if (line.contains("r_frame_rate=")) {
                        buf = line.substring(line.lastIndexOf("=") + 1, line.lastIndexOf("/"));
                        if (!buf.equals("N/A")) {
                            fps = (int)Math.round(Float.parseFloat(buf)/Float.parseFloat(line.substring(line.lastIndexOf("/")+1)));
                        }
                    } else if (line.contains("bit_rate=")) {
                        if (!buf.equals("N/A")) {
                            videoBitrate = Integer.parseInt(buf);
                        }
                    }
                }
                String fileName = file.getName();
                VideoFormats videoFormat = VideoFormatsMap.getVideoFormat(fileName.substring(fileName.lastIndexOf(".") + 1));
                vs = new VideoStream(videoBitrate, width, height, fps, videoFormat);
            }else{
                vs = null;
            }
            if(output.contains("audio")){
                String command2 = String.format("-v quiet -select_streams a:0 -show_entries stream=bit_rate,sample_rate,channels -of default=noprint_wrappers=1 \"%s\"", file.getAbsolutePath());
                int sampleRate = 0;
                int audioBitrate = 0;
                AudioChannels audioChannel = null;
                for(String line : FFprobeKit.execute(command2).getAllLogsAsString().split("\n")){
                    buf = line.substring(line.lastIndexOf("=") + 1);
                    if(line.contains("sample_rate=")){
                        if(!buf.equals("N/A")){
                            sampleRate = Integer.parseInt(buf);
                        }
                    }else if(line.contains("bit_rate=")){
                        if(!buf.equals("N/A")){
                            audioBitrate = Integer.parseInt(buf);
                        }
                    }
                    else if(line.contains("channels=")){
                        if(!buf.equals("N/A")){
                            int a = Integer.parseInt(buf);
                            if(a == 1){
                                audioChannel = AudioChannels.MONO;
                            }else{
                                audioChannel = AudioChannels.STEREO;
                            }
                        }
                    }
                }
                as = new AudioStream(audioBitrate,sampleRate,audioChannel,null);
            }else{
                as = null;
            }
            new VideoConvertingDialog(vs, as, (videoStream,audioStream)-> {
                if(videoStream == null && audioStream!=null){
                    String oldName = UriUtils.getFileName(MainActivity.this,result);
                    convertAudio(result,audioStream,oldName.substring(0,oldName.lastIndexOf(".")),audioStream.getAudioFormat().getValue(),false);
                }else if(videoStream != null){
                    String oldName = UriUtils.getFileName(MainActivity.this,result);
                    convertVideo(result,videoStream,audioStream,oldName.substring(0,oldName.lastIndexOf(".")),videoStream.getVideoFormat().getValue());
                }
            }).show(getSupportFragmentManager(),"video_convert_video_dialog");
        }
    });
    private void convertVideo(final Uri importFile,VideoStream videoStream,AudioStream audioStream,final String fileName,final String ext){
        File parentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),getString(R.string.app_name));
        if(!parentFolder.exists()){
            if(!parentFolder.mkdirs()){
                return;
            }
        }
        File exportFile = new File(parentFolder,fileName+"."+ext);
        int n = 1;
        while(exportFile.exists()){
            exportFile = new File(parentFolder,fileName+"("+(n++)+")."+ext);
        }
        Intent intent = new Intent(this,ConvertingActivity.class);
        intent.putExtra("action","video");
        try {
            intent.putExtra("pack",new VideoConvertingPack(UriUtils.getFileFromUri(this,importFile),videoStream,audioStream, exportFile));
        } catch (Exception e) {
            if(BuildConfig.DEBUG) Log.e(TAG,e.getMessage());
            return;
        }
        startActivity(intent);
    }
    private void convertAudio(final Uri importFile,final AudioStream audioStream,final String fileName,final String ext,boolean onlyAudio){
        File parentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),getString(R.string.app_name));
        if(!parentFolder.exists()){
            if(!parentFolder.mkdirs()){
                return;
            }
        }
        File exportFile = new File(parentFolder,fileName+"."+ext);
        int n = 1;
        while(exportFile.exists()){
            exportFile = new File(parentFolder,fileName+"("+(n++)+")."+ext);
        }
        Intent intent = new Intent(this,ConvertingActivity.class);
        intent.putExtra("action","audio");
        try {
            intent.putExtra("pack",new AudioConvertingPack(UriUtils.getFileFromUri(this,importFile), audioStream, exportFile,onlyAudio));
        } catch (Exception e) {
            if(BuildConfig.DEBUG) Log.e(TAG,e.getMessage());
            return;
        }
        startActivity(intent);
    }
    private void convertImage(final Uri importFile,final int x,final int y,final String fileName,final String ext){
        File parentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),getString(R.string.app_name));
        if(!parentFolder.exists()){
            if(!parentFolder.mkdirs()){
                return;
            }
        }
        File exportFile = new File(parentFolder,fileName+"."+ext);
        int n = 1;
        while(exportFile.exists()){
            exportFile = new File(parentFolder,fileName+"("+(n++)+")."+ext);
        }
        new Thread(new ImageConverter(this,importFile,x,y,exportFile,file-> runOnUiThread(()->{
            if(file == null){
                Snackbar.make(rootView,"ERROR while converting",Snackbar.LENGTH_SHORT).show();
            }else{
                Snackbar.make(rootView,"Successfully converted as "+file.getAbsolutePath(),Snackbar.LENGTH_SHORT).show();
            }
        }))).start();


    }
    private void createNotificationChannel() {
        if (SDK_INT >= Build.VERSION_CODES.O) {
            String name = getString(R.string.notification_id);
            String desc = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.notification_id), name, importance);
            channel.setDescription(desc);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return permission == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
        }
    }
    private void requestPermission() {
        if(checkPermission()){
            return;
        }
        if (SDK_INT >= Build.VERSION_CODES.R) {
            new ManageFilesPermissionDialog().show(getSupportFragmentManager(),"manage_files_dialog");
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    public void managePermissionRequest(){
        if (!checkPermission() && SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Manage files permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_EXTERNAL_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "External storage permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}