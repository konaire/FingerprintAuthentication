package io.codebeavers.fingerprintauth.fingerprint;

import android.app.Activity;
import android.hardware.fingerprint.FingerprintManager;

import com.samsung.android.sdk.pass.Spass;

/**
 * Created by Evgeny Eliseyev on 16/01/2018.
 */

public abstract class FingerprintApi {
    /***
     * Create concrete implementation of fingerprint api.
     * @param activity where api is called
     * @return concrete implementation of api
     */
    public static FingerprintApi create(Activity activity) {
        FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Activity.FINGERPRINT_SERVICE);
        Spass spassInstance = new Spass();
        FingerprintApi api = null;

        try {
            // Check availability of android api, then check for samsung api.
            if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
                api = MarshmallowFingerprintApi.getInstance(activity);
            } else {
                spassInstance.initialize(activity);
                if (spassInstance.isFeatureEnabled(Spass.DEVICE_FINGERPRINT)) {
                    api = SamsungFingerprintApi.getInstance(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return api;
    }

    public static final int PERMISSION_FINGERPRINT = 100500; // Used for asking permissions

    /***
     * Check full availability of auth by fingerprint.
     * @return availability of auth by fingerprint
     */
    public abstract boolean isFingerprintSupported();

    /***
     * Start authentication by fingerprint.
     */
    public abstract void start();

    /***
     * Cancel authentication.
     */
    public abstract void cancel();
}
