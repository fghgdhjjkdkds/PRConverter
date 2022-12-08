package com.purplerat.prconverter.audio;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.purplerat.prconverter.R;

import java.util.Objects;

public class AudioConverterDialog extends DialogFragment {
    private final boolean[] errors = new boolean[]{false,false};
    private int bitrate;
    private int sampleRate;
    private AudioChannels channel;
    private AudioFormats audioFormat;
    private final AudioConverterDialogCallback audioConverterDialogCallback;
    public AudioConverterDialog(int bitrate,int sampleRate,AudioChannels channel,AudioFormats audioFormat,AudioConverterDialogCallback audioConverterDialogCallback){
        this.bitrate = bitrate;
        this.sampleRate =  sampleRate;
        this.channel = channel;
        this.audioFormat = audioFormat;
        this.audioConverterDialogCallback = audioConverterDialogCallback;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP);
        View rootView = getLayoutInflater().inflate(R.layout.audio_converter_dialog_view,(ViewGroup) getView(),false);

        rootView.findViewById(R.id.audio_converter_focus_view).setOnTouchListener((view, motionEvent) -> {
            if(inputMethodManager!=null&&inputMethodManager.isActive())inputMethodManager.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
            return false;
        });

        TextInputLayout audio_dialog_bitrate_box = rootView.findViewById(R.id.audio_dialog_bitrate_box);
        TextInputEditText audio_dialog_bitrate_text = rootView.findViewById(R.id.audio_dialog_bitrate_text);
        TextInputLayout audio_dialog_sample_rate_box = rootView.findViewById(R.id.audio_dialog_sample_rate_box);
        TextInputEditText audio_dialog_sample_rate_text = rootView.findViewById(R.id.audio_dialog_sample_rate_text);
        AutoCompleteTextView audio_dialog_channels = rootView.findViewById(R.id.audio_dialog_channels);
        AutoCompleteTextView audio_dialog_format = rootView.findViewById(R.id.audio_dialog_format);

        audio_dialog_bitrate_text.addTextChangedListener(new TW(audio_dialog_bitrate_box,0));
        audio_dialog_sample_rate_text.addTextChangedListener(new TW(audio_dialog_sample_rate_box,1));

        audio_dialog_bitrate_text.setText(String.valueOf(bitrate));
        audio_dialog_sample_rate_text.setText(String.valueOf(sampleRate));

        audio_dialog_channels.setText(channel.getValue());
        audio_dialog_format.setText(audioFormat.getValue());

        audio_dialog_channels.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,AudioChannels.getEnableChannels()));
        audio_dialog_format.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,AudioFormats.getEnableAudioFormats()));

        dialog.setTitle("Convert audio");
        dialog.setView(rootView);

        dialog.setPositiveButton(android.R.string.ok,(v,i)->{});
        dialog.setNegativeButton(android.R.string.cancel,(v,i)->audioFormat = null);

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
        positiveButton.setOnClickListener(view -> {
            for(boolean e : errors){
                if(e){
                    return;
                }
            }
            int newBitrate = Integer.parseInt(Objects.requireNonNull(audio_dialog_bitrate_text.getText()).toString());
            int newSampleRate = Integer.parseInt(Objects.requireNonNull(audio_dialog_sample_rate_text.getText()).toString());
            AudioChannels newAudioChannel = AudioChannelsMap.getAudioChannel(audio_dialog_channels.getText().toString());
            bitrate = newBitrate == bitrate? 0 : newBitrate;
            sampleRate = newSampleRate == sampleRate? 0 : newSampleRate;
            channel = newAudioChannel == channel?null:newAudioChannel;
            audioFormat = AudioFormatMap.getAudioFormat(audio_dialog_format.getText().toString());
            dismiss();
        });

        return alertDialog;
    }
    private class TW implements TextWatcher {
        private final TextInputLayout layout;
        private final int error;
        public TW(TextInputLayout layout,int error){
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
                errors[error]= true;
                return;
            }
            int value = Integer.parseInt(editable.toString());
            if(value == 0){
                layout.setError("Invalid value");
                layout.setErrorEnabled(true);
                errors[error]= true;
                return;
            }
            errors[error] = false;
            layout.setErrorEnabled(false);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        audioConverterDialogCallback.onComplete(bitrate,sampleRate,channel,audioFormat);
    }

    public interface AudioConverterDialogCallback{
        void onComplete(int bitrate,int sampleRate,AudioChannels channel,AudioFormats audioFormat);
    }
}

