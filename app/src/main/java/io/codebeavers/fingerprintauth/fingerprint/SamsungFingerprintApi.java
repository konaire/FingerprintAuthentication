package io.codebeavers.fingerprintauth.fingerprint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Evgeny Eliseyev on 16/01/2018.
 */

@SuppressLint("StaticFieldLeak")
final class SamsungFingerprintApi extends FingerprintApi {
    private static final String PERMISSION = "com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY";

    private static SamsungFingerprintApi instance;

    private final Activity activity;
    private final SamsungFingerprintHandler fingerprintHandler;

    static synchronized SamsungFingerprintApi getInstance(@NonNull Activity activity) {
        if (instance == null) {
            instance = new SamsungFingerprintApi(activity);
        }

        return instance;
    }

    private SamsungFingerprintApi(Activity activity) {
        this.activity = activity;
        this.fingerprintHandler = new SamsungFingerprintHandler(activity);
    }

    @Override
    public boolean isFingerprintSupported() {
        if (ContextCompat.checkSelfPermission(activity, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] { PERMISSION }, PERMISSION_FINGERPRINT);
            return false;
        } else {
            return fingerprintHandler.hasRegisteredFinger();
        }
    }

    @Override
    public void start(@NonNull Callback callback) {
        fingerprintHandler.setCallback(callback);
        fingerprintHandler.start();
    }

    @Override
    public void cancel() {
        fingerprintHandler.cancel();
    }
}
