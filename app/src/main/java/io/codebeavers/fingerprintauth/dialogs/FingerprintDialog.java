package io.codebeavers.fingerprintauth.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import io.codebeavers.fingerprintauth.R;

/**
 * Created by Evgeny Eliseyev on 12/01/2018.
 */

public class FingerprintDialog extends DialogFragment {
    private DialogInterface.OnClickListener listener;

    public static FingerprintDialog getInstance(DialogInterface.OnClickListener listener) {
        FingerprintDialog dialog = new FingerprintDialog();
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    @SuppressWarnings("ConstantConditions")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_fingerprint, null, false);

        builder.setView(view)
            .setTitle(R.string.app_name)
            .setNegativeButton(android.R.string.cancel, listener);

        setCancelable(false);
        return builder.create();
    }
}
