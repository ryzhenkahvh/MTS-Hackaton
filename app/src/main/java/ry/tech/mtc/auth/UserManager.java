package ry.tech.mtc.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    private static final String PREF_NAME = "UserData";
    private static final String KEY_USERS = "users";
    private static final String KEY_CURRENT_USER = "current_user";
    private static UserManager instance;
    private final SharedPreferences preferences;
    private final Gson gson;

    private UserManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }

    public boolean registerUser(String username, String password, String email) {
        username = username.trim();
        password = password.trim();
        email = email.trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            return false;
        }

        try {
            Map<String, User> users = getAllUsers();
            if (users.containsKey(username)) {
                Log.d("UserManager", "User already exists: " + username);
                return false;
            }

            String hashedPassword = hashPassword(password);
            User newUser = new User(
                    UUID.randomUUID().toString(),
                    username,
                    hashedPassword,
                    email
            );

            users.put(username, newUser);
            saveUsers(users);
            Log.d("UserManager", "User registered successfully: " + username);
            return true;
        } catch (Exception e) {
            Log.d("UserManager", "Registration error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        username = username.trim();
        password = password.trim();

        try {
            Map<String, User> users = getAllUsers();
            Log.d("UserManager", "Users loaded: " + users.keySet());

            User user = users.get(username);
            if (user != null) {
                String inputHashedPassword = hashPassword(password);
                Log.d("UserManager", "Comparing hashes: input=" + inputHashedPassword + ", stored=" + user.getPasswordHash());
                if (inputHashedPassword.equals(user.getPasswordHash())) {
                    saveCurrentUser(user);
                    Log.d("UserManager", "Login successful for: " + username);
                    return true;
                }
            }
            Log.d("UserManager", "Login failed for: " + username);
        } catch (Exception e) {
            Log.d("UserManager", "Login error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public void logoutUser() {
        preferences.edit().remove(KEY_CURRENT_USER).apply();
        Log.d("UserManager", "User logged out");
    }

    public User getCurrentUser() {
        String userJson = preferences.getString(KEY_CURRENT_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    private Map<String, User> getAllUsers() {
        String usersJson = preferences.getString(KEY_USERS, "{}");
        Log.d("UserManager", "Getting all users, JSON: " + usersJson);
        Type type = new TypeToken<Map<String, User>>() {}.getType();
        return gson.fromJson(usersJson, type);
    }

    private void saveUsers(Map<String, User> users) {
        String usersJson = gson.toJson(users);
        preferences.edit().putString(KEY_USERS, usersJson).apply();
        Log.d("UserManager", "Saving users, JSON: " + usersJson);
    }

    private void saveCurrentUser(User user) {
        String userJson = gson.toJson(user);
        preferences.edit().putString(KEY_CURRENT_USER, userJson).apply();
        Log.d("UserManager", "Saving current user, JSON: " + userJson);
    }

    public static class User {
        private final String id;
        private final String username;
        private final String passwordHash;
        private final String email;

        public User(String id, String username, String passwordHash, String email) {
            this.id = id;
            this.username = username;
            this.passwordHash = passwordHash;
            this.email = email;
        }

        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getPasswordHash() { return passwordHash; }
        public String getEmail() { return email; }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        public static User fromJson(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json, User.class);
        }
    }
}