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

public class FingerprintDialog extends DialogFragment implements DialogInterface.OnClickListener {
    public static FingerprintDialog getInstance() {
        return new FingerprintDialog();
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    @SuppressWarnings("ConstantConditions")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_fingerprint, null, false);

        builder.setTitle(R.string.app_name)
            .setCancelable(true)
            .setView(view)
            .setNegativeButton(android.R.string.cancel, this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dismiss();
    }
}
