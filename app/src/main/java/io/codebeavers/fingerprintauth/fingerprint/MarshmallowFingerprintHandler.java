package io.codebeavers.fingerprintauth.fingerprint;

import android.hardware.fingerprint.FingerprintManager;

/**
 * Created by Evgeny Eliseyev on 18/01/2018.
 */

@SuppressWarnings("unused")
class MarshmallowFingerprintHandler extends FingerprintManager.AuthenticationCallback {
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        // TODO: Correct fingerprint
    }

    @Override
    public void onAuthenticationFailed() {
        // TODO: Wrong fingerprint
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errorString) {
        // TODO: Error at authorisation
    }
}
