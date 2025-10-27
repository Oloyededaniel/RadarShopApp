package com.radar.radarshop;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "auth";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";

    private final SharedPreferences sp;

    public SessionManager(Context context) {
        sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void login(String email, String firstName, String lastName) {
        sp.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_EMAIL, email == null ? "" : email.trim())
                .putString(KEY_FIRST_NAME, firstName == null ? "" : firstName.trim())
                .putString(KEY_LAST_NAME, lastName == null ? "" : lastName.trim())
                .apply();
    }

    public void logout() {
        sp.edit()
                .putBoolean(KEY_LOGGED_IN, false)
                .remove(KEY_EMAIL)
                .remove(KEY_FIRST_NAME)
                .remove(KEY_LAST_NAME)
                .apply();
    }

    public boolean isLoggedIn() {
        return sp.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getEmail() {
        return sp.getString(KEY_EMAIL, "");
    }

    public String getFirstName() {
        return sp.getString(KEY_FIRST_NAME, "");
    }

    public String getLastName() {
        return sp.getString(KEY_LAST_NAME, "");
    }

    public String getFullName() {
        String first = getFirstName();
        String last = getLastName();
        if (first.isEmpty() && last.isEmpty()) {
            return "";
        }
        return (first + " " + last).trim();
    }

    public String getUserInitials() {
        String first = getFirstName();
        String last = getLastName();
        String initials = "";
        if (!first.isEmpty()) {
            initials += first.charAt(0);
        }
        if (!last.isEmpty()) {
            initials += last.charAt(0);
        }
        return initials.toUpperCase();
    }

    // Debug method to check session status
    public String getSessionInfo() {
        return "LoggedIn: " + isLoggedIn() + 
               ", Email: " + getEmail() + 
               ", Name: " + getFullName() + 
               ", Initials: " + getUserInitials();
    }
}
