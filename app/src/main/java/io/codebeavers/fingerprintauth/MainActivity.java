package io.codebeavers.fingerprintauth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.codebeavers.fingerprintauth.dialogs.FingerprintDialog;

/**
 * Created by Evgeny Eliseyev on 12/01/2018.
 */

public class MainActivity extends AppCompatActivity {
    private static final String FINGERPRINT_DIALOG = "fingerprint_dialog";

    private TextView statusView;
    private Button buttonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusView = findViewById(R.id.auth_status);
        buttonView = findViewById(R.id.auth_button);

        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerprintDialog.getInstance().show(getSupportFragmentManager(), FINGERPRINT_DIALOG);
            }
        });
    }
}
