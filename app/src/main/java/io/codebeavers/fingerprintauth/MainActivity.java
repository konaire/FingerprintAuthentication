package io.codebeavers.fingerprintauth;

import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.samsung.android.sdk.pass.Spass;

import io.codebeavers.fingerprintauth.dialogs.FingerprintDialog;
import io.codebeavers.fingerprintauth.fingerprint.*;

/**
 * Created by Evgeny Eliseyev on 12/01/2018.
 */

public class MainActivity extends AppCompatActivity {
    private static final String FINGERPRINT_DIALOG = "fingerprint_dialog";

    private FingerprintApi api;

    private TextView statusView;
    private Button buttonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusView = findViewById(R.id.auth_status);
        buttonView = findViewById(R.id.auth_button);

        checkFingerprintAvailability();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FingerprintApi.PERMISSION_FINGERPRINT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkFingerprintAvailability();
            }
        }
    }

    private boolean isFingerprintSupported() {
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        Spass spassInstance = new Spass();

        try {
            // Firstly check availability of android api, secondly check for samsung api
            if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
                api = MarshmallowFingerprintApi.getInstance(this);
            } else if (spassInstance.isFeatureEnabled(Spass.DEVICE_FINGERPRINT)) {
                api = SamsungFingerprintApi.getInstance(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Finally check for any additional requirements of the api
        return api != null && api.isFingerprintSupported();
    }

    private void checkFingerprintAvailability() {
        if (isFingerprintSupported()) {
            buttonView.setAlpha(1);
            buttonView.setEnabled(true);
            statusView.setText(R.string.auth_none);
            buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FingerprintDialog.getInstance().show(getSupportFragmentManager(), FINGERPRINT_DIALOG);
                }
            });
        } else {
            buttonView.setAlpha(.5f);
            buttonView.setEnabled(false);
            statusView.setText(R.string.auth_init_error);
        }
    }
}
