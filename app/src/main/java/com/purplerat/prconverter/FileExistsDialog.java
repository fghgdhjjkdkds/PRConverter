package com.purplerat.prconverter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class FileExistsDialog extends DialogFragment {
    private final FileExistsDialogCallback fileExistsDialogCallback;
    public enum FileExistsActions{RENAME,OVERWRITE,CANCEL}
    private FileExistsActions action = FileExistsActions.OVERWRITE;
    private static final String[] BAN_SYMBOLS = {"/","<",">","*","\"",":","?","\\","|"};
    private TextInputLayout f_e_d_name_box;
    private boolean error = false;
    private String fileName;
    public FileExistsDialog(String name,FileExistsDialogCallback fileExistsDialogCallback){
        this.fileExistsDialogCallback = fileExistsDialogCallback;
        this.fileName = name;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.MyDialogTheme);
        builder.setTitle("The file already exists");
        View view = getLayoutInflater().inflate(R.layout.file_exists_dialog_view, (ViewGroup) getView(), false);
        RadioGroup menu_file_exists_dialog_group = view.findViewById(R.id.menu_file_exists_dialog_group);
        f_e_d_name_box = view.findViewById(R.id.f_e_d_name_box);
        TextInputEditText f_e_d_name_text = view.findViewById(R.id.f_e_d_name_text);
        f_e_d_name_text.addTextChangedListener(nameTextWatcher);
        menu_file_exists_dialog_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.f_e_d_overwrite) {
                    f_e_d_name_box.setVisibility(View.GONE);
                    action = FileExistsActions.OVERWRITE;
                }else if (i == R.id.f_e_d_rename){
                    f_e_d_name_box.setVisibility(View.VISIBLE);
                    f_e_d_name_text.setText(fileName);
                    action = FileExistsActions.RENAME;
                }
            }
        });
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, (d,i)->{});
        builder.setNegativeButton(android.R.string.cancel,(d,i)->{action = FileExistsActions.CANCEL;fileName=null;});
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface->{
            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            //Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            positiveButton.setOnClickListener(v->{
                switch (action){
                    case RENAME:
                        if(!error){
                            fileName = f_e_d_name_text.getText().toString();
                            dismiss();
                        }
                        break;
                    case OVERWRITE:
                        dismiss();
                        break;
                }
            });
            alertDialog.setCanceledOnTouchOutside(false);
        });
        return alertDialog;
    }
    private final TextWatcher nameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            if(text.startsWith(".")){
                f_e_d_name_box.setError("File name cannot starts with terms");
                f_e_d_name_box.setErrorEnabled(true);
                error = true;
                return;
            }
            if(text.endsWith(".")){
                f_e_d_name_box.setError("File name cannot ends with terms");
                f_e_d_name_box.setErrorEnabled(true);
                error = true;
                return;
            }
            for(String symbol : BAN_SYMBOLS){
                if(text.contains(symbol)){
                    f_e_d_name_box.setError("File name cannot contain /,<,>,*,\",:,?,\\,|");
                    f_e_d_name_box.setErrorEnabled(true);
                    error = true;
                    return;
                }
            }
            error = false;
            f_e_d_name_box.setErrorEnabled(false);
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fileExistsDialogCallback.onComplete(action,fileName);
    }

    public interface FileExistsDialogCallback{
        void onComplete(FileExistsActions fileExistsActions,String fileName);
    }
}
