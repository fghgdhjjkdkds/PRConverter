package com.purplerat.prconverter.video;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.purplerat.prconverter.R;
import com.purplerat.prconverter.audio.AudioChannels;
import com.purplerat.prconverter.audio.AudioStream;

public class VideoConvertingDialog extends DialogFragment {
    private final boolean[] videoStreamErrors = new boolean[]{false,false,false,false};
    private final boolean[] audioStreamErrors = new boolean[]{false,false};
    private Button show_video_stream_button;
    private Button show_audio_stream_button;
    private ConstraintLayout video_converter_dialog_video_block;
    private ConstraintLayout video_converter_dialog_audio_block;
    private CheckBox video_converter_dialog_delete_video_checkbox;
    private ConstraintLayout video_converter_dialog_inner_video_block;
    private CheckBox video_converter_dialog_delete_audio_checkbox;
    private ConstraintLayout video_converter_dialog_inner_audio_block;
    private VideoStream videoStream;
    private AudioStream audioStream;
    private final VideoConvertingDialogCallback videoConvertingDialogCallback;

    public VideoConvertingDialog(VideoStream videoStream,AudioStream audioStream,VideoConvertingDialogCallback videoConvertingDialogCallback) {
        this.videoStream = videoStream;
        this.audioStream = audioStream;
        this.videoConvertingDialogCallback = videoConvertingDialogCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        dialog.setTitle("Video converting");
        dialog.setPositiveButton(android.R.string.ok,(d,i)->{});
        dialog.setNegativeButton(android.R.string.cancel,(d,i)->{videoStream = null;audioStream = null;});
        View rootView  = getLayoutInflater().inflate(R.layout.video_converter_dialog_view,(ViewGroup) getView(),false);

        show_video_stream_button = rootView.findViewById(R.id.show_video_stream_button);
        video_converter_dialog_video_block = rootView.findViewById(R.id.video_converter_dialog_video_block);
        show_audio_stream_button = rootView.findViewById(R.id.show_audio_stream_button);
        video_converter_dialog_audio_block = rootView.findViewById(R.id.video_converter_dialog_audio_block);
        video_converter_dialog_delete_video_checkbox = rootView.findViewById(R.id.video_converter_dialog_delete_video_checkbox);
        video_converter_dialog_inner_video_block = rootView.findViewById(R.id.video_converter_dialog_inner_video_block);
        video_converter_dialog_delete_audio_checkbox = rootView.findViewById(R.id.video_converter_dialog_delete_audio_checkbox);
        video_converter_dialog_inner_audio_block = rootView.findViewById(R.id.video_converter_dialog_inner_audio_block);

        if(videoStream == null){
            show_video_stream_button.setVisibility(View.GONE);
        }else{
            final TextInputLayout video_dialog_width_box = rootView.findViewById(R.id.video_dialog_width_box);
            final TextInputEditText video_dialog_width_text = rootView.findViewById(R.id.video_dialog_width_text);
            video_dialog_width_text.addTextChangedListener(new VideoTextFieldTemplate(video_dialog_width_box,0));
            video_dialog_width_text.setText(String.valueOf(videoStream.getWidth()));

            final TextInputLayout video_dialog_height_box = rootView.findViewById(R.id.video_dialog_height_box);
            final TextInputEditText video_dialog_height_text = rootView.findViewById(R.id.video_dialog_height_text);
            video_dialog_height_text.addTextChangedListener(new VideoTextFieldTemplate(video_dialog_height_box,1));
            video_dialog_height_text.setText(String.valueOf(videoStream.getHeight()));

            final TextInputLayout video_dialog_fps_box = rootView.findViewById(R.id.video_dialog_fps_box);
            final TextInputEditText video_dialog_fps_text = rootView.findViewById(R.id.video_dialog_fps_text);
            video_dialog_fps_text.addTextChangedListener(new VideoTextFieldTemplate(video_dialog_fps_box,2));
            video_dialog_fps_text.setText(String.valueOf(videoStream.getFps()));

            final TextInputLayout video_dialog_bitrate_video_box = rootView.findViewById(R.id.video_dialog_video_bitrate_box);
            final TextInputEditText video_dialog_video_text = rootView.findViewById(R.id.video_dialog_video_bitrate_text);
            video_dialog_video_text.addTextChangedListener(new VideoTextFieldTemplate(video_dialog_bitrate_video_box,3));
            video_dialog_video_text.setText(String.valueOf(videoStream.getBitrate()));

            final AutoCompleteTextView video_dialog_video_format = rootView.findViewById(R.id.video_dialog_video_format);
            video_dialog_video_format.setText(videoStream.getVideoFormat().getValue());
            video_dialog_video_format.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,VideoFormats.getEnableVideoFormats()));
        }
        if(audioStream == null){
            show_audio_stream_button.setVisibility(View.GONE);
        }else{
            final TextInputLayout video_dialog_audio_bitrate_box = rootView.findViewById(R.id.video_dialog_audio_bitrate_box);
            final TextInputEditText video_dialog_audio_bitrate_text = rootView.findViewById(R.id.video_dialog_audio_bitrate_text);
            video_dialog_audio_bitrate_text.addTextChangedListener(new AudioTextFieldTemplate(video_dialog_audio_bitrate_box,0));
            video_dialog_audio_bitrate_text.setText(String.valueOf(audioStream.getBitrate()));

            final TextInputLayout video_dialog_sample_rate_box = rootView.findViewById(R.id.video_dialog_sample_rate_box);
            final TextInputEditText video_dialog_sample_rate_text = rootView.findViewById(R.id.video_dialog_sample_rate_text);
            video_dialog_sample_rate_text.addTextChangedListener(new AudioTextFieldTemplate(video_dialog_sample_rate_box,1));
            video_dialog_sample_rate_text.setText(String.valueOf(audioStream.getSampleRate()));

            final AutoCompleteTextView video_dialog_channels = rootView.findViewById(R.id.video_dialog_channels);
            video_dialog_channels.setText(audioStream.getChannel().getValue());
            video_dialog_channels.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, AudioChannels.getEnableChannels()));

        }

        show_video_stream_button.setOnClickListener(v->{
            if(video_converter_dialog_video_block.getVisibility() == View.VISIBLE){
                hideVideoBlock();
            }else if(video_converter_dialog_video_block.getVisibility() == View.GONE){
                showVideoBlock();
                hideAudioBlock();
            }
        });
        show_audio_stream_button.setOnClickListener(V->{
            if(video_converter_dialog_audio_block.getVisibility() == View.VISIBLE){
                hideAudioBlock();
            }else if(video_converter_dialog_audio_block.getVisibility() == View.GONE){
                showAudioBlock();
                hideVideoBlock();
            }
        });
        video_converter_dialog_delete_video_checkbox.setOnCheckedChangeListener((c,b)->{
            if(b){
                hideInnerVideoBlock();
            }else{
                showInnerVideoBlock();
            }
        });
        video_converter_dialog_delete_audio_checkbox.setOnCheckedChangeListener((c,b)->{
            if(b){
                hideInnerAudioBlock();
            }else{
                showInnerAudioBlock();
            }
        });
        dialog.setView(rootView);
        AlertDialog alertDialog = dialog.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnShowListener(alertDialogView->{
            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v->{
                if(video_converter_dialog_delete_video_checkbox.isChecked()){
                    videoStream = null;
                }else if(videoStream!= null){

                }
                if(video_converter_dialog_delete_audio_checkbox.isChecked()){
                    audioStream = null;
                }else if(audioStream!= null){

                }
                dismiss();
            });
        });
        return alertDialog;
    }
    private class VideoTextFieldTemplate implements TextWatcher{
        private final TextInputLayout layout;
        private final int error;
        public VideoTextFieldTemplate(TextInputLayout layout,int error){
            this.layout = layout;
            this.error = error;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.toString().equals("")){
                layout.setError("Please enter the value");
                layout.setErrorEnabled(true);
                videoStreamErrors[error]= true;
                return;
            }
            int value = Integer.parseInt(editable.toString());
            if(value == 0){
                layout.setError("Invalid value");
                layout.setErrorEnabled(true);
                videoStreamErrors[error]= true;
                return;
            }
            videoStreamErrors[error] = false;
            layout.setErrorEnabled(false);
        }
    }
    private class AudioTextFieldTemplate implements TextWatcher{
        private final TextInputLayout layout;
        private final int error;
        public AudioTextFieldTemplate(TextInputLayout layout,int error){
            this.layout = layout;
            this.error = error;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.toString().equals("")){
                layout.setError("Please enter the value");
                layout.setErrorEnabled(true);
                audioStreamErrors[error]= true;
                return;
            }
            int value = Integer.parseInt(editable.toString());
            if(value == 0){
                layout.setError("Invalid value");
                layout.setErrorEnabled(true);
                audioStreamErrors[error]= true;
                return;
            }
            audioStreamErrors[error] = false;
            layout.setErrorEnabled(false);
        }
    }
    private void showVideoBlock(){
        if(video_converter_dialog_video_block.getVisibility() == View.GONE){
            video_converter_dialog_video_block.setVisibility(View.VISIBLE);
            show_video_stream_button.setText(getString(R.string.hide_video_stream));
        }
    }
    private void hideVideoBlock(){
        if(video_converter_dialog_video_block.getVisibility() == View.VISIBLE){
            video_converter_dialog_video_block.setVisibility(View.GONE);
            show_video_stream_button.setText(getString(R.string.show_video_stream));
        }
    }
    private void showAudioBlock(){
        if(video_converter_dialog_audio_block.getVisibility() == View.GONE){
            video_converter_dialog_audio_block.setVisibility(View.VISIBLE);
            show_audio_stream_button.setText(getString(R.string.hide_audio_stream));
        }
    }
    private void hideAudioBlock(){
        if(video_converter_dialog_audio_block.getVisibility() == View.VISIBLE){
            video_converter_dialog_audio_block.setVisibility(View.GONE);
            show_audio_stream_button.setText(getString(R.string.show_audio_stream));
        }
    }
    private void showInnerVideoBlock(){
        if(video_converter_dialog_inner_video_block.getVisibility() == View.GONE){
            video_converter_dialog_inner_video_block.setVisibility(View.VISIBLE);
        }
    }
    private void hideInnerVideoBlock(){
        if(video_converter_dialog_inner_video_block.getVisibility() == View.VISIBLE){
            video_converter_dialog_inner_video_block.setVisibility(View.GONE);
        }
    }
    private void showInnerAudioBlock(){
        if(video_converter_dialog_inner_audio_block.getVisibility() == View.GONE){
            video_converter_dialog_inner_audio_block.setVisibility(View.VISIBLE);
        }
    }
    private void hideInnerAudioBlock(){
        if(video_converter_dialog_inner_audio_block.getVisibility() == View.VISIBLE){
            video_converter_dialog_inner_audio_block.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        videoConvertingDialogCallback.onComplete(videoStream,audioStream);
    }

    public interface VideoConvertingDialogCallback{
        void onComplete(VideoStream videoStream, AudioStream audioStream);
    }
}
