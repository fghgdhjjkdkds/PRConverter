package com.purplerat.prconverter;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ManageFilesPermissionDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        dialog.setTitle("Allow permission for storage access");
        dialog.setPositiveButton(android.R.string.ok,(d,i)->((MainActivity)getContext()).managePermissionRequest());
        dialog.setNegativeButton(android.R.string.cancel,(d,i)->{});
        AlertDialog alertDialog = dialog.create();
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }
}
