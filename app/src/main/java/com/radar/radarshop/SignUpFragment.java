package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpFragment extends Fragment {

    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etConfirm;
    private TextInputLayout tilConfirmPassword;
    private CheckBox cbTerms;
    private MaterialButton btnCreateAccount;
    private TextView tvTermsCombined;
    private LinearLayout layoutPasswordRequirements;
    private TextView tvReqLength, tvReqUppercase, tvReqLowercase, tvReqNumber, tvReqSpecial;
    private TextView tvPasswordMatch;
    
    // Password validation states
    private boolean isValidLength = false;
    private boolean hasUppercase = false;
    private boolean hasLowercase = false;
    private boolean hasNumber = false;
    private boolean hasSpecial = false;
    private boolean passwordsMatch = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etFirstName = v.findViewById(R.id.etFirstName);
        etLastName  = v.findViewById(R.id.etLastName);
        etEmail     = v.findViewById(R.id.etEmail);
        etPassword  = v.findViewById(R.id.etPassword);
        etConfirm   = v.findViewById(R.id.etConfirm);
        tilConfirmPassword = v.findViewById(R.id.tilConfirmPassword);
        cbTerms     = v.findViewById(R.id.cbTerms);
        btnCreateAccount = v.findViewById(R.id.btnCreateAccount);
        tvTermsCombined  = v.findViewById(R.id.tvTermsCombined);
        
        // Password requirements views
        layoutPasswordRequirements = v.findViewById(R.id.layoutPasswordRequirements);
        tvReqLength = v.findViewById(R.id.tvReqLength);
        tvReqUppercase = v.findViewById(R.id.tvReqUppercase);
        tvReqLowercase = v.findViewById(R.id.tvReqLowercase);
        tvReqNumber = v.findViewById(R.id.tvReqNumber);
        tvReqSpecial = v.findViewById(R.id.tvReqSpecial);
        tvPasswordMatch = v.findViewById(R.id.tvPasswordMatch);

        // Safety: ensure password fields are obscured even if XML changes later.
        if (etPassword != null) etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        if (etConfirm  != null) etConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
        
        // Set up TextWatchers for real-time validation
        setupTextWatchers();
        
        // Initially disable button
        btnCreateAccount.setEnabled(false);

        btnCreateAccount.setOnClickListener(view -> {
            String first = val(etFirstName);
            String last  = val(etLastName);
            String email = val(etEmail);
            String pass  = val(etPassword);
            String conf  = val(etConfirm);

            DatabaseHelper db = new DatabaseHelper(requireContext());
            boolean created = db.insertUser(first, last, email, pass);

            if (created) {
                new SessionManager(requireContext()).login(email, first, last);
                Intent intent = new Intent(requireContext(), HomeActivity.class);
                startActivity(intent);
                requireActivity().finish();
            } else {
                toast("Email already exists");
            }
        });
        
        // Terms checkbox listener
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCreateAccountButton();
        });
    }

    private void setupTextWatchers() {
        // First name watcher
        etFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateCreateAccountButton();
            }
        });
        
        // Last name watcher
        etLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateCreateAccountButton();
            }
        });
        
        // Email watcher
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateCreateAccountButton();
            }
        });
        
        // Password watcher
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
                // Also check password match when password changes
                if (etConfirm.getText() != null) {
                    checkPasswordMatch(etPassword.getText().toString(), etConfirm.getText().toString());
                }
                updateCreateAccountButton();
            }
        });
        
        // Confirm password watcher
        etConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (etPassword.getText() != null) {
                    checkPasswordMatch(etPassword.getText().toString(), s.toString());
                }
                updateCreateAccountButton();
            }
        });
    }
    
    private void validatePassword(String password) {
        if (password == null) password = "";
        
        // Show/hide requirements based on whether user has started typing
        if (password.length() > 0) {
            layoutPasswordRequirements.setVisibility(View.VISIBLE);
        } else {
            layoutPasswordRequirements.setVisibility(View.GONE);
        }
        
        // Check each requirement
        isValidLength = password.length() >= 8;
        hasUppercase = password.matches(".*[A-Z].*");
        hasLowercase = password.matches(".*[a-z].*");
        hasNumber = password.matches(".*[0-9].*");
        hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        // Update UI colors
        updateRequirementView(tvReqLength, isValidLength);
        updateRequirementView(tvReqUppercase, hasUppercase);
        updateRequirementView(tvReqLowercase, hasLowercase);
        updateRequirementView(tvReqNumber, hasNumber);
        updateRequirementView(tvReqSpecial, hasSpecial);
    }
    
    private void updateRequirementView(TextView textView, boolean isValid) {
        if (textView == null) return;
        if (isValid) {
            textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
        } else {
            textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        }
    }
    
    private void checkPasswordMatch(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            tvPasswordMatch.setVisibility(View.GONE);
            passwordsMatch = false;
            if (tilConfirmPassword != null) {
                tilConfirmPassword.setError(null);
            }
            return;
        }
        
        passwordsMatch = password.equals(confirmPassword);
        
        if (passwordsMatch) {
            tvPasswordMatch.setText("✓ Passwords match");
            tvPasswordMatch.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
            tvPasswordMatch.setVisibility(View.VISIBLE);
            if (tilConfirmPassword != null) {
                tilConfirmPassword.setError(null);
            }
        } else {
            tvPasswordMatch.setText("✗ Passwords do not match");
            tvPasswordMatch.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            tvPasswordMatch.setVisibility(View.VISIBLE);
            if (tilConfirmPassword != null) {
                tilConfirmPassword.setError("Passwords do not match");
            }
        }
    }
    
    private void updateCreateAccountButton() {
        String first = val(etFirstName);
        String last = val(etLastName);
        String email = val(etEmail);
        String pass = val(etPassword);
        String conf = val(etConfirm);
        
        // Check if all fields are filled
        boolean allFieldsFilled = !TextUtils.isEmpty(first) && 
                                 !TextUtils.isEmpty(last) && 
                                 !TextUtils.isEmpty(email) && 
                                 !TextUtils.isEmpty(pass) && 
                                 !TextUtils.isEmpty(conf);
        
        // Check if email is valid
        boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        
        // Check if password meets all requirements
        boolean passwordValid = isValidLength && hasUppercase && hasLowercase && hasNumber && hasSpecial;
        
        // Check if passwords match
        boolean passwordsMatchCheck = passwordsMatch && !pass.isEmpty() && !conf.isEmpty();
        
        // Check if terms are accepted
        boolean termsAccepted = cbTerms.isChecked();
        
        // Enable button only if all conditions are met
        btnCreateAccount.setEnabled(allFieldsFilled && emailValid && passwordValid && passwordsMatchCheck && termsAccepted);
    }
    
    private String val(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
