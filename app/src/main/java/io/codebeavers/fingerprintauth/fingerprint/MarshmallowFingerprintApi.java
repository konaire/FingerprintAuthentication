package io.codebeavers.fingerprintauth.fingerprint;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Evgeny Eliseyev on 16/01/2018.
 */

@SuppressLint("StaticFieldLeak")
public final class MarshmallowFingerprintApi implements FingerprintApi {
    private static MarshmallowFingerprintApi instance;

    private final Activity activity;

    public static synchronized MarshmallowFingerprintApi getInstance(Activity activity) {
        if (instance == null) {
            instance = new MarshmallowFingerprintApi(activity);
        }

        return instance;
    }

    private MarshmallowFingerprintApi(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean isFingerprintSupported() {
        KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(Activity.KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Activity.FINGERPRINT_SERVICE);
        boolean hasPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED;

        if (!hasPermission) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.USE_FINGERPRINT }, PERMISSION_FINGERPRINT);
        }

        return hasPermission && keyguardManager != null && fingerprintManager != null &&
            keyguardManager.isKeyguardSecure() && fingerprintManager.hasEnrolledFingerprints();
    }
}
