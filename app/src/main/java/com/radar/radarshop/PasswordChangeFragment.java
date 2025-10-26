package com.radar.radarshop;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PasswordChangeFragment extends Fragment {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnCancel, btnSavePassword;
    private ImageButton btnBack;
    
    private DatabaseHelper db;
    private SessionManager session;
    private OnPasswordChangeListener listener;

    public interface OnPasswordChangeListener {
        void onPasswordChanged(boolean success);
        void onBackPressed();
    }

    public static PasswordChangeFragment newInstance() {
        return new PasswordChangeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(getContext());
        session = new SessionManager(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("PasswordChangeFragment", "onCreateView called");
        return inflater.inflate(R.layout.fragment_password_change, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("PasswordChangeFragment", "onViewCreated called");
        
        // Initialize views
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSavePassword = view.findViewById(R.id.btnSavePassword);
        btnBack = view.findViewById(R.id.btnBack);

        // Set up click listeners
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBackPressed();
                }
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBackPressed();
                }
            });
        }

        if (btnSavePassword != null) {
            btnSavePassword.setOnClickListener(v -> changePassword());
        }
    }

    public void setOnPasswordChangeListener(OnPasswordChangeListener listener) {
        this.listener = listener;
    }

    private void changePassword() {
        String currentPassword = safeText(etCurrentPassword);
        String newPassword = safeText(etNewPassword);
        String confirmPassword = safeText(etConfirmPassword);

        // Validate inputs
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Current password is required");
            etCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your new password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 8) {
            etNewPassword.setError("Password must be at least 8 characters long");
            etNewPassword.requestFocus();
            return;
        }

        if (!isValidPassword(newPassword)) {
            etNewPassword.setError("Password must contain uppercase, lowercase, number, and special character");
            etNewPassword.requestFocus();
            return;
        }

        if (currentPassword.equals(newPassword)) {
            etNewPassword.setError("New password must be different from current password");
            etNewPassword.requestFocus();
            return;
        }

        // Verify current password
        String email = session.getEmail();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if current password is correct
        if (!db.verifyPassword(email, currentPassword)) {
            etCurrentPassword.setError("Current password is incorrect");
            etCurrentPassword.requestFocus();
            return;
        }

        // Update password
        boolean success = db.updatePassword(email, newPassword);
        
        if (success) {
            Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
            clearFields();
            if (listener != null) {
                listener.onPasswordChanged(true);
            }
        } else {
            Toast.makeText(getContext(), "Failed to change password. Please try again.", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onPasswordChanged(false);
            }
        }
    }

    private boolean isValidPassword(String password) {
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }

    private void clearFields() {
        if (etCurrentPassword != null) etCurrentPassword.setText("");
        if (etNewPassword != null) etNewPassword.setText("");
        if (etConfirmPassword != null) etConfirmPassword.setText("");
    }

    private String safeText(EditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }
}
