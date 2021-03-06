package io.codebeavers.fingerprintauth.network;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Evgeny Eliseyev on 12/01/2018.
 */

@SuppressLint("StaticFieldLeak")
public class SimpleAuthService {
    private static SimpleAuthService instance;
    private final Map<String, String> users;

    // Implemented singleton pattern. We don't need wrong states here, do we?
    public static synchronized SimpleAuthService getInstance() {
        if (instance == null) {
            instance = new SimpleAuthService();
        }

        return instance;
    }

    private SimpleAuthService() {
        this.users = new HashMap<>();
        users.put("fake@codebeavers.io", "test");
    }

    // The method fakes a real auth.
    public boolean auth(String login, String password) {
        Log.d("111222333", String.format("Data: %s - %s", login, password));

        if (!users.containsKey(login)) {
            users.put(login, password);
            return true;
        } else {
            return users.get(login).equals(password);
        }
    }
}
