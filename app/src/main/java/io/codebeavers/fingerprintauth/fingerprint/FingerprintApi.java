package io.codebeavers.fingerprintauth.fingerprint;

/**
 * Created by Evgeny Eliseyev on 16/01/2018.
 */

public interface FingerprintApi {
    int PERMISSION_FINGERPRINT = 100500; // Used for asking permissions

    boolean isFingerprintSupported(); // Check full availability of auth by fingerprint
}
