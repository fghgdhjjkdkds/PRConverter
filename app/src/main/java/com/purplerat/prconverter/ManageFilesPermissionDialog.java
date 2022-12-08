package com.purplerat.prconverter;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class ManageFilesPermissionDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        dialog.setTitle("Allow permission for storage access");
        dialog.setPositiveButton(android.R.string.ok,(d,i)->Objects.requireNonNull((MainActivity)getContext()).managePermissionRequest());
        dialog.setNegativeButton(android.R.string.cancel,(d,i)->{});
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
            topPanel.setBackgroundResource(R.drawable.dialog_background_shape);
        }
        return alertDialog;
    }
}
