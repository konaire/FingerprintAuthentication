package io.codebeavers.fingerprintauth.fingerprint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.samsung.android.sdk.pass.SpassFingerprint;

/**
 * Created by Evgeny Eliseyev on 16/01/2018.
 */

@SuppressLint("StaticFieldLeak")
public final class SamsungFingerprintApi implements FingerprintApi {
    private final class SamsungFingerprintHandler extends SpassFingerprint {
        SamsungFingerprintHandler(Context context) {
            super(context);
        }
    }

    private static final String PERMISSION = "com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY";

    private static SamsungFingerprintApi instance;

    private final Activity activity;
    private final SamsungFingerprintHandler fingerprintHandler;

    public static synchronized SamsungFingerprintApi getInstance(Activity activity) {
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
}
