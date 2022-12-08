package com.purplerat.prconverter.image;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
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

public class ImageConverterDialog extends DialogFragment {
    private final boolean[] errors = new boolean[]{false,false};
    private final static String[] FORMATS = new String[]{"png","jpeg","webp"};
    private int x,y;
    private ImageFormats imageFormat;
    private final ImageConverterDialogCallback imageConverterDialogCallback;
    public ImageConverterDialog(int x,int y,ImageFormats imageFormat,ImageConverterDialogCallback imageConverterDialogCallback){
        this.x = x;
        this.y = y;
        this.imageFormat = imageFormat;
        this.imageConverterDialogCallback = imageConverterDialogCallback;
    }
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP);
        View rootView = getLayoutInflater().inflate(R.layout.image_converter_dialog_view,(ViewGroup) getView(),false);
        rootView.findViewById(R.id.image_converter_focus_view).setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if(inputMethodManager!=null&&inputMethodManager.isActive())inputMethodManager.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
            return false;
        });
        TextInputLayout image_dialog_width_box = rootView.findViewById(R.id.image_dialog_width_box);
        TextInputEditText image_dialog_width_text = rootView.findViewById(R.id.image_dialog_width_text);
        TextInputLayout image_dialog_height_box = rootView.findViewById(R.id.image_dialog_height_box);
        TextInputEditText image_dialog_height_text = rootView.findViewById(R.id.image_dialog_height_text);
        AutoCompleteTextView image_dialog_format = rootView.findViewById(R.id.image_dialog_format);

        image_dialog_format.setText(imageFormat.getExtension());
        image_dialog_format.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,FORMATS));

        image_dialog_width_text.addTextChangedListener(new TW(image_dialog_width_box,0));
        image_dialog_height_text.addTextChangedListener(new TW(image_dialog_height_box,1));

        image_dialog_width_text.setText(String.valueOf(x));
        image_dialog_height_text.setText(String.valueOf(y));

        dialog.setTitle("Convert image");
        dialog.setView(rootView);
        dialog.setPositiveButton(android.R.string.ok,(v,i)->{});
        dialog.setNegativeButton(android.R.string.cancel,(v,i)->imageFormat = null);

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
            x = Integer.parseInt(Objects.requireNonNull(image_dialog_width_text.getText()).toString());
            y = Integer.parseInt(Objects.requireNonNull(image_dialog_height_text.getText()).toString());
            switch (image_dialog_format.getText().toString()){
                case "webp":
                    imageFormat = ImageFormats.WEBP;
                    break;
                case"png":
                    imageFormat = ImageFormats.PNG;
                    break;
                case "jpeg":
                    imageFormat = ImageFormats.JPEG;
                    break;
                default:
                    imageFormat = null;
            }
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
            String text = editable.toString();
            if(text.equals("")){
                layout.setError("Please enter the value");
                layout.setErrorEnabled(true);
                errors[error] = true;
                return;
            }
            int value = Integer.parseInt(text);
            if(value <= 0){
                layout.setError("Invalid value");
                layout.setErrorEnabled(true);
                errors[error] = true;
                return;
            }
            layout.setErrorEnabled(false);
            errors[error] = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageConverterDialogCallback.onComplete(x,y,imageFormat);
    }

    public interface ImageConverterDialogCallback{
        void onComplete(int x,int y,ImageFormats imageFormat);
    }
}
