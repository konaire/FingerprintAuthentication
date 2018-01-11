package io.codebeavers.fingerprintauth.network;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Evgeny Eliseyev on 12/01/2018.
 */

public class SimpleAuthService {
    private static SimpleAuthService instance;
    private final Map<String, String> users;

    // Implemented singleton pattern. We don't need wrong states here, do we?
    public synchronized SimpleAuthService getInstance() {
        if (instance == null) {
            instance = new SimpleAuthService();
        }

        return instance;
    }

    private SimpleAuthService() {
        this.users = new HashMap<>();
    }

    // The method fakes a real auth.
    public boolean auth(String login, String password) {
        if (!users.containsKey(login)) {
            users.put(login, password);
            return true;
        } else {
            return users.get(login).equals(password);
        }
    }
}
