package io.codebeavers.fingerprintauth.fingerprint;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * Created by Evgeny Eliseyev on 18/01/2018.
 */

class CryptoManager {
    private static final String KEY_ALIAS = "codebeavers_fingerprint";
    private static CryptoManager instance;

    private boolean isKeyValid;
    private KeyStore keyStore;
    private Cipher cipher;

    static synchronized CryptoManager getInstance() {
        if (instance == null) {
            instance = new CryptoManager();
        }

        return instance;
    }

    private CryptoManager() {}

    /***
     * The method goes all steps for creating a valid cipher. It creates or recreates key if needed.
     * @return valid cipher for authentication.
     */
    Cipher getCipher() {
        return isKeyValid() ? cipher : null;
    }

    /***
     * The method returns a valid public key. It creates or recreates key if needed.
     * @return base64 representation of public key.
     */
    String getPublicKey() {
        if (isKeyValid()) {
            PublicKey key;

            try {
                keyStore.load(null);
                key = keyStore.getCertificate(KEY_ALIAS).getPublicKey();
                return new String(Base64.encode(key.getEncoded(), 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private boolean isKeyValid() {
        // Check that key was created and wasn't checked for validity.
        if (!isKeyValid && isKeyReady()) {
            try {
                keyStore.load(null);

                // Create cipher with the same algorithm and parameters as for key.
                cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA + "/" + KeyProperties.BLOCK_MODE_ECB + "/" + KeyProperties.ENCRYPTION_PADDING_RSA_OAEP);
                OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
                PrivateKey key = (PrivateKey) keyStore.getKey(KEY_ALIAS, null);

                // Initialize cipher with key. The method also checks key for validity.
                cipher.init(Cipher.DECRYPT_MODE, key, spec);
                isKeyValid = true;
            } catch (Exception e) {
                // KeyPermanentlyInvalidatedException is throwed in case of key's invalidity. So we need to generate it.
                if (e instanceof KeyPermanentlyInvalidatedException && generateKey()) {
                    return isKeyValid(); // And check it again.
                } else {
                    e.printStackTrace();
                }
            }
        }

        return isKeyValid;
    }

    // Check for availability of a key in android default keystore.
    // And create one if there is no key.
    private boolean isKeyReady() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            keyStore.load(null);
            return keyStore.containsAlias(KEY_ALIAS) || generateKey();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean generateKey() {
        try {
            // "AndroidKeyStore" is default keystore for android which allows to protect key from unauthorized access.
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");

            // KEY_ALIAS is your unique key identificator.
            // KeyProperties.PURPOSE_DECRYPT because we'll use key only for checking that auth have been successful.
            // We willn't actually ecnrypt/decrypt something using the key. So setUserAuthenticationRequired(true) used for the same reasons.
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .setUserAuthenticationRequired(true);

            keyGenerator.initialize(builder.build());
            keyGenerator.generateKeyPair();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
