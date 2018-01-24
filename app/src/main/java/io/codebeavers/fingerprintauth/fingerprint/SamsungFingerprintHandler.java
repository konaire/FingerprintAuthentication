package io.codebeavers.fingerprintauth.fingerprint;

import android.content.Context;
import android.os.Handler;

import com.samsung.android.sdk.pass.SpassFingerprint;

/**
 * Created by Evgeny Eliseyev on 18/01/2018.
 */

class SamsungFingerprintHandler extends SpassFingerprint implements SpassFingerprint.IdentifyListener {
    private boolean isIdentifing; // true if auth is in progress
    private FingerprintApi.Callback callback;

    SamsungFingerprintHandler(Context context) {
        super(context);
    }

    @Override
    public void onReady() {}

    @Override
    public void onStarted() {}

    @Override
    public void onCompleted() {}

    @Override
    public void onFinished(int eventStatus) {
        if (callback != null) {
            switch (eventStatus) {
                case STATUS_AUTHENTIFICATION_SUCCESS:
                    callback.onSuccess(CryptoManager.getInstance().getPublicKey()); break;
                case STATUS_USER_CANCELLED:
                    break; // nothing to do
                case STATUS_QUALITY_FAILED:
                case STATUS_AUTHENTIFICATION_FAILED:
                    callback.onFailure(); break;
                default:
                    callback.onError(eventStatus); break;
            }
        }

        isIdentifing = false;
    }

    void setCallback(FingerprintApi.Callback callback) {
        this.callback = callback;
    }

    void start() {
        if (isIdentifing) {
            return;
        }

        try {
            // Create/get a key and check it for validity.
            boolean hasValidKey = CryptoManager.getInstance().getCipher() != null;
            
            if (hasValidKey) {
                isIdentifing = true;
                startIdentify(this);
            }
        } catch (Exception e) {
            // Sometimes auth is stuck. So we need to wait some time with patience.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            }, 2 * 1000);
            isIdentifing = false;
        }
    }

    void cancel() {
        if (isIdentifing) {
            cancelIdentify();
            isIdentifing = false;
        }
    }
}
