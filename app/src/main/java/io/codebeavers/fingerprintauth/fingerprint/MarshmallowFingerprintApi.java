package io.codebeavers.fingerprintauth.fingerprint;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Evgeny Eliseyev on 16/01/2018.
 */

@SuppressLint("StaticFieldLeak")
final class MarshmallowFingerprintApi extends FingerprintApi {
    private static MarshmallowFingerprintApi instance;

    private final Activity activity;
    private CancellationSignal cancellationSignal; // used to cancel authorisation

    static synchronized MarshmallowFingerprintApi getInstance(@NonNull Activity activity) {
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

    @Override
    public void start(@NonNull Callback callback) {
        cancellationSignal = new CancellationSignal();
        FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Activity.FINGERPRINT_SERVICE);
        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(CryptoManager.getInstance().getCipher());
        // Create/get a key, check it and return CryptoObject which can be used for encrypt/decrypt some password or pin.

        if (fingerprintManager != null) {
            // Start authentication. new MarshmallowFingerprintHandler() creates object for receiving a callback.
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, new MarshmallowFingerprintHandler(callback), null);
        }
    }

    @Override
    public void cancel() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }
}
