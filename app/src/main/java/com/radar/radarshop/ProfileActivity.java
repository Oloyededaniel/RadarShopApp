package com.radar.radarshop;

import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvAvatar, tvName, tvEmailHeader;
    private EditText etFirst, etLast, etEmail, etPhone;
    private EditText etStreet, etCity, etState, etZip;
    private Spinner spinnerCountry;
    private TextView tvEdit, tvEditAddress;
    private ImageView ivSave;
    private LinearLayout btnChangePassword, btnLogout, btnDeleteAccount;
    private FrameLayout fragmentContainer;

    private DatabaseHelper db;
    private SessionManager session;
    private AddressAutocompleteHelper addressAutocompleteHelper;
    
    private boolean personalEditing = false;
    private boolean addressEditing  = false;
    private boolean isPasswordFragmentVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_profile);
        } catch (Throwable t) {
            String tString = t.getMessage();
            Log.e("Profile activity", "unable to open profile activity: " + tString);
            Toast.makeText(this, tString, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        session = new SessionManager(this);
        db = new DatabaseHelper(this);

        // --- Get logged in email, or bounce back to Auth ---
        String email = session.getEmail();
        if (TextUtils.isEmpty(email)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        // --- Bind all views (null-tolerant) ---
        ImageButton btnBack = findViewById(R.id.btnBack);                // header back
        tvAvatar       = findViewById(R.id.tvAvatar);                    // avatar initials
        tvName         = findViewById(R.id.tvName);                      // name text
        tvEmailHeader  = findViewById(R.id.tvEmailHeader);               // header email

        ivSave         = findViewById(R.id.ivSave);                      // save icon (Personal card)
        tvEdit         = findViewById(R.id.tvEdit);                      // Edit/Cancel (Personal)
        etFirst        = findViewById(R.id.etFirst);
        etLast         = findViewById(R.id.etLast);
        etEmail        = findViewById(R.id.etEmail);
        etPhone        = findViewById(R.id.etPhone);

        tvEditAddress  = findViewById(R.id.tvEditAddress);               // Edit (Address)
        etStreet       = findViewById(R.id.etStreet);
        etCity         = findViewById(R.id.etCity);
        etState        = findViewById(R.id.etState);
        etZip          = findViewById(R.id.etZip);
        spinnerCountry = findViewById(R.id.spinnerCountry);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout         = findViewById(R.id.btnLogout);
        btnDeleteAccount  = findViewById(R.id.btnDeleteAccount);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        // --- Set defaults and render profile ---
        // Email is the key: keep disabled
        if (etEmail != null) etEmail.setEnabled(false);
        setPersonalEnabled(false);
        setAddressEnabled(false);
        renderProfile(email);
        
        // Initialize country spinner
        initializeCountrySpinner();
        
        // Add postal code formatting
        addPostalCodeFormatting();
        
        // Setup address autocomplete
        setupAddressAutocomplete();

        // --- Listeners (only attach if views exist) ---
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent homeIntent = new Intent(this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                finish();
            });
        }

        if (tvEdit != null) {
            tvEdit.setOnClickListener(v -> {
                if (!personalEditing) {
                    // Switch to edit mode
                    personalEditing = true;
                    setPersonalEnabled(true);
                    tvEdit.setText("Save");
                    Toast.makeText(this, "Editing personal information…", Toast.LENGTH_SHORT).show();
                } else {
                    // Save the personal data
                    String userEmail = session.getEmail();
                    if (!TextUtils.isEmpty(userEmail)) {
                        boolean ok = db.updateProfile(
                                userEmail,
                                safeText(etFirst), safeText(etLast), safeText(etPhone),
                                safeText(etStreet), safeText(etCity), safeText(etState),
                                safeText(etZip), getSelectedCountry()
                        );
                        Toast.makeText(this, ok ? "Personal information saved" : "Save failed", Toast.LENGTH_SHORT).show();
                        
                        if (ok) {
                            personalEditing = false;
                            setPersonalEnabled(false);
                            tvEdit.setText("Edit");
                            // refresh header bits
                            String full = (safeText(etFirst) + " " + safeText(etLast)).trim();
                            if (tvName != null) tvName.setText(TextUtils.isEmpty(full) ? "User" : full);
                            if (tvAvatar != null) tvAvatar.setText(makeInitials(full, userEmail));
                        }
                    }
                }
            });
        }

        if (ivSave != null) {
            // Save icon is no longer needed - functionality moved to edit button toggle
            ivSave.setVisibility(View.GONE);
        }

        if (tvEditAddress != null) {
            tvEditAddress.setOnClickListener(v -> {
                if (!addressEditing) {
                    // Switch to edit mode
                    addressEditing = true;
                    setAddressEnabled(true);
                    tvEditAddress.setText("Save");
                    Toast.makeText(this, "Editing address…", Toast.LENGTH_SHORT).show();
                } else {
                    // Save the address data
                    String userEmail = session.getEmail();
                    if (!TextUtils.isEmpty(userEmail)) {
                        boolean ok = db.updateProfile(
                                userEmail,
                                safeText(etFirst), safeText(etLast), safeText(etPhone),
                                safeText(etStreet), safeText(etCity), safeText(etState),
                                safeText(etZip), getSelectedCountry()
                        );
                        Toast.makeText(this, ok ? "Address saved" : "Save failed", Toast.LENGTH_SHORT).show();
                        
                        if (ok) {
                            addressEditing = false;
                            setAddressEnabled(false);
                            tvEditAddress.setText("Edit");
                        }
                    }
                }
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                Log.d("ProfileActivity", "Change Password button clicked");
                Toast.makeText(this, "Opening password change...", Toast.LENGTH_SHORT).show();
                showPasswordChangeFragment();
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                session.logout();
                startActivity(new Intent(this, AuthActivity.class));
                finish();
            });
        }

        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }
    }

    private void renderProfile(String email) {
        DatabaseHelper.UserProfile up = null;
        try {
            up = db.getUserProfile(email);
        } catch (Throwable t) {
            // keep null
        }

        // Header
        String fullName = (up != null) ? up.fullName() : "";
        String headerName = TextUtils.isEmpty(fullName) ? "User" : fullName;
        if (tvName != null) tvName.setText(headerName);
        if (tvEmailHeader != null) tvEmailHeader.setText(TextUtils.isEmpty(email) ? "" : email);
        if (tvAvatar != null) tvAvatar.setText(makeInitials(fullName, email));

        // Personal fields
        if (up != null) {
            setText(etFirst, up.first);
            setText(etLast,  up.last);
            setText(etEmail, up.email);
            setText(etPhone, up.phone);

            // Address fields
            setText(etStreet,  up.street);
            setText(etCity,    up.city);
            setText(etState,   up.state);
            setText(etZip,     up.zip);
            setCountrySpinner(up.country);
        } else {
            // Minimal fallback
            setText(etEmail, email);
        }
    }

    private void setPersonalEnabled(boolean enabled) {
        setEnabled(etFirst, enabled);
        setEnabled(etLast, enabled);
        setEnabled(etPhone, enabled);
        // etEmail should remain disabled (key)
        setEnabled(etEmail, false);
    }

    private void setAddressEnabled(boolean enabled) {
        setEnabled(etStreet, enabled);
        setEnabled(etCity, enabled);
        setEnabled(etState, enabled);
        setEnabled(etZip, enabled);
        if (spinnerCountry != null) spinnerCountry.setEnabled(enabled);
        
        // Update autocomplete when address editing is enabled
        if (enabled && etStreet != null) {
            // Ensure autocomplete helper is initialized
            if (addressAutocompleteHelper == null) {
                setupAddressAutocomplete();
            }
            // Re-attach autocomplete to the field
            if (addressAutocompleteHelper != null) {
                addressAutocompleteHelper.attachToEditText(etStreet);
            }
        }
    }


    private void setText(EditText et, String v) {
        if (et != null) et.setText(v == null ? "" : v);
    }

    private void setEnabled(EditText et, boolean enabled) {
        if (et != null) et.setEnabled(enabled);
    }

    private String safeText(EditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }

    private String makeInitials(String fullName, String email) {
        // Try full name
        if (!TextUtils.isEmpty(fullName)) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length >= 2) {
                return "" + Character.toUpperCase(parts[0].charAt(0))
                        + Character.toUpperCase(parts[1].charAt(0));
            } else if (parts.length == 1 && parts[0].length() > 0) {
                return "" + Character.toUpperCase(parts[0].charAt(0));
            }
        }
        // Fallback: email username
        if (!TextUtils.isEmpty(email)) {
            String[] split = email.split("@", 2);
            String user = split.length > 0 ? split[0] : "";
            if (!user.isEmpty()) {
                char c1 = Character.toUpperCase(user.charAt(0));
                char c2 = user.length() > 1 ? Character.toUpperCase(user.charAt(1)) : 0;
                return c2 == 0 ? String.valueOf(c1) : ("" + c1 + c2);
            }
        }
        return "U";
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently removed.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Show confirmation dialog
                    showFinalDeleteConfirmation();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showFinalDeleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Final Confirmation")
                .setMessage("This will permanently delete your account and all associated data. Type 'DELETE' to confirm.")
                .setView(createDeleteConfirmationView())
                .setPositiveButton("Confirm Delete", (dialog, which) -> {
                    // Check if user typed DELETE
                    View dialogView = ((AlertDialog) dialog).getWindow().getDecorView();
                    EditText confirmationInput = findEditTextInView(dialogView);
                    if (confirmationInput != null && "DELETE".equals(confirmationInput.getText().toString().trim())) {
                        deleteUserAccount();
                    } else {
                        Toast.makeText(this, "Please type 'DELETE' to confirm", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private View createDeleteConfirmationView() {
        EditText editText = new EditText(this);
        editText.setId(View.generateViewId()); // Generate a unique ID
        editText.setHint("Type DELETE to confirm");
        editText.setPadding(50, 20, 50, 20);
        return editText;
    }

    private EditText findEditTextInView(View view) {
        if (view instanceof EditText) {
            return (EditText) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                EditText result = findEditTextInView(group.getChildAt(i));
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void deleteUserAccount() {
        String email = session.getEmail();
        if (!TextUtils.isEmpty(email)) {
            // Delete user from database
            boolean deleted = db.deleteUser(email);
            if (deleted) {
                // Clear session and redirect to auth
                session.logout();
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AuthActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to delete account. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPasswordChangeFragment() {
        Log.d("ProfileActivity", "showPasswordChangeFragment called");
        if (fragmentContainer == null) {
            Log.e("ProfileActivity", "fragmentContainer is null!");
            return;
        }
        
        Log.d("ProfileActivity", "Creating PasswordChangeFragment");
        PasswordChangeFragment fragment = PasswordChangeFragment.newInstance();
        fragment.setOnPasswordChangeListener(new PasswordChangeFragment.OnPasswordChangeListener() {
            @Override
            public void onPasswordChanged(boolean success) {
                hidePasswordChangeFragment();
            }

            @Override
            public void onBackPressed() {
                hidePasswordChangeFragment();
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();

        // Show fragment container and hide main content
        Log.d("ProfileActivity", "Setting fragment container visibility to VISIBLE");
        fragmentContainer.setVisibility(View.VISIBLE);
        isPasswordFragmentVisible = true;
    }

    private void hidePasswordChangeFragment() {
        if (fragmentContainer == null) return;
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragment);
            transaction.commit();
        }

        // Hide fragment container and show main content
        fragmentContainer.setVisibility(View.GONE);
        isPasswordFragmentVisible = false;
    }

    @Override
    public void onBackPressed() {
        if (isPasswordFragmentVisible) {
            hidePasswordChangeFragment();
        } else {
            super.onBackPressed();
        }
    }
    
    private void initializeCountrySpinner() {
        if (spinnerCountry == null) return;
        
        String[] countries = {
            "Canada", "United States", "United Kingdom", "Australia", 
            "Germany", "France", "Japan", "Brazil", "India", "Mexico"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(adapter);
        
        // Set default to Canada
        spinnerCountry.setSelection(0);
    }
    
    private void setCountrySpinner(String country) {
        if (spinnerCountry == null || country == null) return;
        
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCountry.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(country);
            if (position >= 0) {
                spinnerCountry.setSelection(position);
            }
        }
    }
    
    private String getSelectedCountry() {
        if (spinnerCountry == null) return "";
        return (String) spinnerCountry.getSelectedItem();
    }
    
    private void addPostalCodeFormatting() {
        if (etZip == null) return;
        
        etZip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String formatted = formatPostalCode(s.toString());
                if (!formatted.equals(s.toString())) {
                    etZip.setText(formatted);
                    etZip.setSelection(formatted.length());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private String formatPostalCode(String input) {
        String cleaned = input.replaceAll("\\s", "").toUpperCase();
        if (cleaned.length() >= 3) {
            return cleaned.substring(0, 3) + " " + cleaned.substring(3, Math.min(6, cleaned.length()));
        }
        return cleaned;
    }
    
    private void setupAddressAutocomplete() {
        try {
            // Initialize the autocomplete helper with address, city, state, zip code, and country fields
            addressAutocompleteHelper = new AddressAutocompleteHelper(
                    this,
                    etStreet,
                    etCity,
                    etState,
                    etZip,
                    spinnerCountry
            );
            
            // Attach autocomplete to the street address field - shows inline dropdown as user types
            if (etStreet != null) {
                addressAutocompleteHelper.attachToEditText(etStreet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Silently fail - autocomplete is optional
        }
    }
}
