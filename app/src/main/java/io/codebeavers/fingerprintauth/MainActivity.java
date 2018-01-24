package io.codebeavers.fingerprintauth;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

import io.codebeavers.fingerprintauth.dialogs.FingerprintDialog;
import io.codebeavers.fingerprintauth.fingerprint.FingerprintApi;
import io.codebeavers.fingerprintauth.network.SimpleAuthService;

/**
 * Created by Evgeny Eliseyev on 12/01/2018.
 */

public class MainActivity extends AppCompatActivity implements FingerprintDialog.Callback {
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

    @Override
    public void onSuccess(String publicKey) {
        Random random = new Random();
        String[] names = { "fake", "konair" };
        String login = String.format("%s@codebeavers.io", names[random.nextInt(2)]);
        // Simulate correct and incorrect input for testing our fake service.

        boolean result = SimpleAuthService.getInstance().auth(login, publicKey);
        statusView.setText(result ? R.string.auth_success : R.string.auth_failure);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        api.cancel();
        dialog.dismiss();
        statusView.setText(R.string.auth_canceled);
    }

    private boolean isFingerprintSupported() {
        // Check for availability and any additional requirements for the api.
        return (api = FingerprintApi.create(this)) != null && api.isFingerprintSupported();
    }

    private void checkFingerprintAvailability() {
        if (isFingerprintSupported()) {
            buttonView.setAlpha(1);
            buttonView.setEnabled(true);
            statusView.setText(R.string.auth_none);
            buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // FIXME: It can freeze the app while key is been generating. Need to call in different thread for a real app.
                    FingerprintDialog dialog = FingerprintDialog.getInstance(MainActivity.this);
                    dialog.show(getSupportFragmentManager(), FINGERPRINT_DIALOG);
                    api.start(dialog);
                }
            });
        } else {
            buttonView.setAlpha(.5f);
            buttonView.setEnabled(false);
            statusView.setText(R.string.auth_init_error);
        }
    }
}
