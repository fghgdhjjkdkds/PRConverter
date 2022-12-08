package com.purplerat.prconverter.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.purplerat.prconverter.R;
import com.purplerat.prconverter.audio.AudioChannels;
import com.purplerat.prconverter.audio.AudioChannelsMap;
import com.purplerat.prconverter.audio.AudioFormatMap;
import com.purplerat.prconverter.audio.AudioFormats;
import com.purplerat.prconverter.audio.AudioStream;

import java.util.Objects;

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
    private TextInputLayout video_dialog_audio_format_box;
    private TextInputEditText video_dialog_width_text;
    private TextInputEditText video_dialog_height_text;
    private TextInputEditText video_dialog_fps_text;
    private TextInputEditText video_dialog_video_bitrate_text;
    private AutoCompleteTextView video_dialog_video_format;

    private TextInputEditText video_dialog_audio_bitrate_text;
    private TextInputEditText video_dialog_sample_rate_text;
    private AutoCompleteTextView video_dialog_channels;
    private AutoCompleteTextView video_dialog_audio_format;

    private VideoStream videoStream;
    private AudioStream audioStream;

    private VideoStream exportVideoStream = null;
    private AudioStream exportAudioStream = null;
    private final VideoConvertingDialogCallback videoConvertingDialogCallback;

    public VideoConvertingDialog(VideoStream videoStream,AudioStream audioStream,VideoConvertingDialogCallback videoConvertingDialogCallback) {
        this.videoStream = videoStream;
        this.audioStream = audioStream;
        this.videoConvertingDialogCallback = videoConvertingDialogCallback;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP);
        dialog.setTitle("Video converting");
        dialog.setPositiveButton(android.R.string.ok,(d,i)->{});
        dialog.setNegativeButton(android.R.string.cancel,(d,i)->{});
        View rootView  = getLayoutInflater().inflate(R.layout.video_converter_dialog_view,(ViewGroup) getView(),false);

        show_video_stream_button = rootView.findViewById(R.id.show_video_stream_button);
        video_converter_dialog_video_block = rootView.findViewById(R.id.video_converter_dialog_video_block);
        show_audio_stream_button = rootView.findViewById(R.id.show_audio_stream_button);
        video_converter_dialog_audio_block = rootView.findViewById(R.id.video_converter_dialog_audio_block);
        video_converter_dialog_delete_video_checkbox = rootView.findViewById(R.id.video_converter_dialog_delete_video_checkbox);
        video_converter_dialog_inner_video_block = rootView.findViewById(R.id.video_converter_dialog_inner_video_block);
        video_converter_dialog_delete_audio_checkbox = rootView.findViewById(R.id.video_converter_dialog_delete_audio_checkbox);
        video_converter_dialog_inner_audio_block = rootView.findViewById(R.id.video_converter_dialog_inner_audio_block);
        video_dialog_audio_format_box = rootView.findViewById(R.id.video_dialog_audio_format_box);
        rootView.findViewById(R.id.video_converter_focus_view).setOnTouchListener((view, motionEvent) -> {
            if(inputMethodManager!=null&&inputMethodManager.isActive())inputMethodManager.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
            return false;
        });

        if(videoStream == null){
            show_video_stream_button.setVisibility(View.GONE);
        }else{
            final TextInputLayout video_dialog_width_box = rootView.findViewById(R.id.video_dialog_width_box);
            video_dialog_width_text = rootView.findViewById(R.id.video_dialog_width_text);
            video_dialog_width_text.addTextChangedListener(new VideoTextFieldTemplate(video_dialog_width_box,0));
            video_dialog_width_text.setText(String.valueOf(videoStream.getWidth()));

            final TextInputLayout video_dialog_height_box = rootView.findViewById(R.id.video_dialog_height_box);
            video_dialog_height_text = rootView.findViewById(R.id.video_dialog_height_text);
            video_dialog_height_text.addTextChangedListener(new VideoTextFieldTemplate(video_dialog_height_box,1));
            video_dialog_height_text.setText(String.valueOf(videoStream.getHeight()));

            final TextInputLayout video_dialog_fps_box = rootView.findViewById(R.id.video_dialog_fps_box);
            video_dialog_fps_text = rootView.findViewById(R.id.video_dialog_fps_text);
            video_dialog_fps_text.addTextChangedListener(new VideoTextFieldTemplate(video_dialog_fps_box,2));
            video_dialog_fps_text.setText(String.valueOf(videoStream.getFps()));

            final TextInputLayout video_dialog_bitrate_video_box = rootView.findViewById(R.id.video_dialog_video_bitrate_box);
            video_dialog_video_bitrate_text = rootView.findViewById(R.id.video_dialog_video_bitrate_text);
            if(videoStream.getBitrate()!=0) {
                video_dialog_video_bitrate_text.addTextChangedListener(new VideoTextFieldTemplate(video_dialog_bitrate_video_box, 3));
                video_dialog_video_bitrate_text.setText(String.valueOf(videoStream.getBitrate()));
            }else{
                video_dialog_bitrate_video_box.setVisibility(View.GONE);
                video_dialog_video_bitrate_text.setText("0");
            }

            video_dialog_video_format = rootView.findViewById(R.id.video_dialog_video_format);
            video_dialog_video_format.setText(videoStream.getVideoFormat().getValue(),false);
            video_dialog_video_format.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.dropdown_list_item,VideoFormats.getEnableVideoFormats()));
        }
        if(audioStream == null){
            show_audio_stream_button.setVisibility(View.GONE);
        }else{
            final TextInputLayout video_dialog_audio_bitrate_box = rootView.findViewById(R.id.video_dialog_audio_bitrate_box);
            video_dialog_audio_bitrate_text = rootView.findViewById(R.id.video_dialog_audio_bitrate_text);
            if(audioStream.getBitrate()!=0) {
                video_dialog_audio_bitrate_text.addTextChangedListener(new AudioTextFieldTemplate(video_dialog_audio_bitrate_box, 0));
                video_dialog_audio_bitrate_text.setText(String.valueOf(audioStream.getBitrate()));
            }else{
                video_dialog_audio_bitrate_box.setVisibility(View.GONE);
                video_dialog_audio_bitrate_text.setText("0");
            }

            final TextInputLayout video_dialog_sample_rate_box = rootView.findViewById(R.id.video_dialog_sample_rate_box);
            video_dialog_sample_rate_text = rootView.findViewById(R.id.video_dialog_sample_rate_text);
            video_dialog_sample_rate_text.addTextChangedListener(new AudioTextFieldTemplate(video_dialog_sample_rate_box,1));
            video_dialog_sample_rate_text.setText(String.valueOf(audioStream.getSampleRate()));

            video_dialog_channels = rootView.findViewById(R.id.video_dialog_channels);
            video_dialog_channels.setText(audioStream.getChannel().getValue(),false);
            video_dialog_channels.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, AudioChannels.getEnableChannels()));

            video_dialog_audio_format = rootView.findViewById(R.id.video_dialog_audio_format);
            video_dialog_audio_format.setText(AudioFormats.getEnableAudioFormats()[0],false);
            video_dialog_audio_format.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, AudioFormats.getEnableAudioFormats()));

            if(videoStream == null){
                video_dialog_audio_format_box.setVisibility(View.VISIBLE);
            }else{
                video_dialog_audio_format_box.setVisibility(View.GONE);
            }
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
                video_dialog_audio_format_box.setVisibility(View.VISIBLE);
            }else{
                showInnerVideoBlock();
                video_dialog_audio_format_box.setVisibility(View.GONE);
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
        AlertDialog alertDialog = dialog.show();
        alertDialog.setCanceledOnTouchOutside(false);

        int buttonPanelId = getResources().getIdentifier("buttonPanel","id","android");
        final View buttonPanel=alertDialog.findViewById(buttonPanelId);
        if (buttonPanel!=null){
            buttonPanel.setBackgroundColor(getResources().getColor(R.color.bg_color, requireActivity().getTheme()));
        }

        int topPanelId = getResources().getIdentifier("topPanel","id","android");
        final View topPanel =alertDialog.findViewById(topPanelId);
        if (topPanel!=null){
            topPanel.setBackgroundColor(getResources().getColor(R.color.primary_color,requireActivity().getTheme()));
        }

        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v->{
            if(video_converter_dialog_delete_video_checkbox.isChecked()){
                exportVideoStream = null;
            }else if(videoStream!= null){
                for(boolean error : videoStreamErrors){
                    if(error) return;
                }
                int bitrate = Integer.parseInt(Objects.requireNonNull(video_dialog_video_bitrate_text.getText()).toString());
                int width = Integer.parseInt(Objects.requireNonNull(video_dialog_width_text.getText()).toString());
                int height = Integer.parseInt(Objects.requireNonNull(video_dialog_height_text.getText()).toString());
                int fps = Integer.parseInt(Objects.requireNonNull(video_dialog_fps_text.getText()).toString());
                exportVideoStream = new VideoStream(
                        bitrate == videoStream.getBitrate()? 0 : bitrate,
                        width != videoStream.getWidth() || height != videoStream.getHeight()? width : 0,
                        width != videoStream.getWidth() || height != videoStream.getHeight()? height : 0,
                        fps == videoStream.getFps()? 0 : fps,
                        VideoFormatsMap.getVideoFormat(video_dialog_video_format.getText().toString()));
            }
            if(video_converter_dialog_delete_audio_checkbox.isChecked()){
                exportAudioStream = null;
            }else if(audioStream!= null){
                for(boolean error:audioStreamErrors){
                    if(error)return;
                }
                int bitrate = Integer.parseInt(Objects.requireNonNull(video_dialog_audio_bitrate_text.getText()).toString());
                int sampleRate = Integer.parseInt(Objects.requireNonNull(video_dialog_sample_rate_text.getText()).toString());
                AudioChannels audioChannel = AudioChannelsMap.getAudioChannel(video_dialog_channels.getText().toString());
                exportAudioStream = new AudioStream(
                        bitrate == audioStream.getBitrate()? 0 : bitrate,
                        sampleRate == audioStream.getSampleRate()? 0 : sampleRate,
                        audioChannel == audioStream.getChannel()?null:audioChannel,
                        AudioFormatMap.getAudioFormat(video_dialog_audio_format.getText().toString()));
            }
            dismiss();
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
        videoConvertingDialogCallback.onComplete(exportVideoStream,exportAudioStream);
    }

    public interface VideoConvertingDialogCallback{
        void onComplete(VideoStream videoStream, AudioStream audioStream);
    }
}
