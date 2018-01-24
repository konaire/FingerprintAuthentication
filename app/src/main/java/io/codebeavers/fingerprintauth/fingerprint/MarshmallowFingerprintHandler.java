package io.codebeavers.fingerprintauth.fingerprint;

import android.hardware.fingerprint.FingerprintManager;

/**
 * Created by Evgeny Eliseyev on 18/01/2018.
 */

@SuppressWarnings("unused")
class MarshmallowFingerprintHandler extends FingerprintManager.AuthenticationCallback {
    private final FingerprintApi.Callback callback;

    MarshmallowFingerprintHandler(FingerprintApi.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        callback.onSuccess(CryptoManager.getInstance().getPublicKey());
    }

    @Override
    public void onAuthenticationFailed() {
        callback.onFailure();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errorString) {
        if (errorCode != FingerprintManager.FINGERPRINT_ERROR_USER_CANCELED) {
            callback.onError(errorCode);
        }
    }
}
