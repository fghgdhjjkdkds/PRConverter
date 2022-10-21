package com.purplerat.prconverter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.purplerat.prconverter.audio.AudioConvertingPack;
import com.purplerat.prconverter.audio.AudioConvertingService;
import com.purplerat.prconverter.video.VideoConvertingPack;
import com.purplerat.prconverter.video.VideoConvertingService;

import java.io.File;

public class ConvertingActivity extends AppCompatActivity {
    private View rootView = null;
    private ProgressBar progressBar = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converting);
        rootView = findViewById(android.R.id.content).getRootView();
        progressBar = rootView.findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);

        IntentFilter actionOnProgressReceiver = new IntentFilter();
        IntentFilter actionOnCompleteReceiver = new IntentFilter();
        actionOnProgressReceiver.addAction("progress");
        actionOnCompleteReceiver.addAction("complete");
        bm.registerReceiver(OnProgressReceive , actionOnProgressReceiver);
        bm.registerReceiver(OnCompleteReceive,actionOnCompleteReceiver);

        String action = getIntent().getStringExtra("action");

        switch (action){
            case "audio":
                AudioConvertingPack audioConvertingPack = (AudioConvertingPack) getIntent().getSerializableExtra("pack");
                Intent audioIntent = new Intent(this,AudioConvertingService.class);
                audioIntent.putExtra("pack",audioConvertingPack);
                startService(audioIntent);
                break;
            case "video":
                VideoConvertingPack videoConvertingPack = (VideoConvertingPack) getIntent().getSerializableExtra("pack");
                Intent videoIntent = new Intent(this, VideoConvertingService.class);
                videoIntent.putExtra("pack",videoConvertingPack);
                startService(videoIntent);
                break;
            case "resumption":
                break;
            default:
                finish();
                break;
        }
    }
    private final BroadcastReceiver OnProgressReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if(intent.hasExtra("progress")){
                    int progress = intent.getIntExtra("progress",0);
                    if(progressBar!=null){
                        progressBar.setProgress(progress);
                    }
                }
            }
        }
    };
    private final BroadcastReceiver OnCompleteReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if(intent.hasExtra("file")){
                    if(intent.getSerializableExtra("file")==null){
                        if(rootView != null) {
                            Snackbar.make(rootView,"Error while converting",Snackbar.LENGTH_SHORT).show();
                        }
                    }else{
                        File file = (File) intent.getSerializableExtra("file");
                        if(rootView != null) {
                            Snackbar.make(rootView,"Successfully converted as "+file.getAbsolutePath(),Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            }

        }
    };

    @Override
    public void onBackPressed() {}
}