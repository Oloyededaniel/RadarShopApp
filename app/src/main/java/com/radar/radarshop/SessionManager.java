package com.radar.radarshop;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SessionManager {
    private static final String PREFS = "auth";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_SAVED_ACCOUNTS = "saved_accounts";
    private static final String KEY_ACCOUNT_NAMES = "account_names";

    private final SharedPreferences sp;

    public SessionManager(Context context) {
        sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void login(String email, String firstName, String lastName) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String normalizedFirst = firstName == null ? "" : firstName.trim();
        String normalizedLast = lastName == null ? "" : lastName.trim();
        
        sp.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_EMAIL, normalizedEmail)
                .putString(KEY_FIRST_NAME, normalizedFirst)
                .putString(KEY_LAST_NAME, normalizedLast)
                .apply();
        
        // Save this account to the list of saved accounts
        addSavedAccount(normalizedEmail, normalizedFirst, normalizedLast);
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

    /* MULTI-ACCOUNT SUPPORT */
    
    /**
     * Represents a saved account (without password for security)
     */
    public static class SavedAccount {
        public final String email;
        public final String firstName;
        public final String lastName;
        
        public SavedAccount(String email, String firstName, String lastName) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }
        
        public String getFullName() {
            String first = firstName == null ? "" : firstName.trim();
            String last = lastName == null ? "" : lastName.trim();
            return (first + " " + last).trim();
        }
        
        public String getDisplayName() {
            String fullName = getFullName();
            return fullName.isEmpty() ? email : fullName + " (" + email + ")";
        }
    }

    /**
     * Add an account to the saved accounts list
     */
    private void addSavedAccount(String email, String firstName, String lastName) {
        if (email == null || email.isEmpty()) return;
        
        Set<String> savedAccounts = sp.getStringSet(KEY_SAVED_ACCOUNTS, new HashSet<>());
        Set<String> accountNames = sp.getStringSet(KEY_ACCOUNT_NAMES, new HashSet<>());
        
        // Create account identifier (email:firstname:lastname)
        String accountId = email + ":" + (firstName == null ? "" : firstName) + ":" + (lastName == null ? "" : lastName);
        
        // Add to sets
        savedAccounts.add(accountId);
        accountNames.add(accountId);
        
        // Save back
        sp.edit()
                .putStringSet(KEY_SAVED_ACCOUNTS, savedAccounts)
                .putStringSet(KEY_ACCOUNT_NAMES, accountNames)
                .apply();
    }

    /**
     * Get all saved accounts
     */
    public List<SavedAccount> getSavedAccounts() {
        List<SavedAccount> accounts = new ArrayList<>();
        Set<String> savedAccounts = sp.getStringSet(KEY_SAVED_ACCOUNTS, new HashSet<>());
        
        for (String accountId : savedAccounts) {
            String[] parts = accountId.split(":", 3);
            if (parts.length >= 1) {
                String email = parts[0];
                String firstName = parts.length > 1 ? parts[1] : "";
                String lastName = parts.length > 2 ? parts[2] : "";
                accounts.add(new SavedAccount(email, firstName, lastName));
            }
        }
        
        return accounts;
    }

    /**
     * Remove a saved account
     */
    public void removeSavedAccount(String email) {
        if (email == null || email.isEmpty()) return;
        
        Set<String> savedAccounts = sp.getStringSet(KEY_SAVED_ACCOUNTS, new HashSet<>());
        Set<String> accountNames = sp.getStringSet(KEY_ACCOUNT_NAMES, new HashSet<>());
        
        // Find and remove matching account
        String toRemove = null;
        for (String accountId : savedAccounts) {
            if (accountId.startsWith(email.toLowerCase() + ":")) {
                toRemove = accountId;
                break;
            }
        }
        
        if (toRemove != null) {
            savedAccounts.remove(toRemove);
            accountNames.remove(toRemove);
            
            sp.edit()
                    .putStringSet(KEY_SAVED_ACCOUNTS, savedAccounts)
                    .putStringSet(KEY_ACCOUNT_NAMES, accountNames)
                    .apply();
        }
    }

    /**
     * Clear all saved accounts
     */
    public void clearSavedAccounts() {
        sp.edit()
                .remove(KEY_SAVED_ACCOUNTS)
                .remove(KEY_ACCOUNT_NAMES)
                .apply();
    }
}
