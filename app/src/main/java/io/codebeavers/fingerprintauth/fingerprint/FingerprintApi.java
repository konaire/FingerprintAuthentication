package io.codebeavers.fingerprintauth.fingerprint;

import android.app.Activity;
import android.hardware.fingerprint.FingerprintManager;
import android.support.annotation.NonNull;

import com.samsung.android.sdk.pass.Spass;

/**
 * Created by Evgeny Eliseyev on 16/01/2018.
 */

public abstract class FingerprintApi {
    /**
     * General callback for handling different results of authorisation.
     */
    public interface Callback {
        /**
         * The method is called if auth was successful.
         * @param publicKey is base64 representation of public key.
         */
        void onSuccess(String publicKey);

        /**
         * The method is called if auth wasn't successful.
         * For example: fingerprint was incorrect.
         */
        void onFailure();

        /**
         * The method is called if auth was finished with error.
         * For example: The API is locked out due to too many attempts.
         * @param errorCode is an integer identifying the error message.
         */
        void onError(int errorCode);
    }

    /**
     * Create concrete implementation of fingerprint api.
     * @param activity where api is called.
     * @return concrete implementation of api.
     */
    public static FingerprintApi create(@NonNull Activity activity) {
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

    public static final int PERMISSION_FINGERPRINT = 100500; // used for asking permissions

    /**
     * Check full availability of auth by fingerprint.
     * @return availability of auth by fingerprint.
     */
    public abstract boolean isFingerprintSupported();

    /**
     * Start authentication by fingerprint.
     * @param callback is called if scanner has gotten a result.
     */
    public abstract void start(@NonNull Callback callback);

    /**
     * Cancel authentication.
     */
    public abstract void cancel();
}
