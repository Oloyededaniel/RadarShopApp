package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignInFragment extends Fragment {

    private TextInputEditText etEmailLogin, etPasswordLogin;
    private MaterialButton btnSignIn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etEmailLogin = v.findViewById(R.id.etEmailLogin);
        etPasswordLogin = v.findViewById(R.id.etPasswordLogin);
        btnSignIn = v.findViewById(R.id.btnSignIn);
        
        // Prefill email if provided
        if (getArguments() != null) {
            String prefillEmail = getArguments().getString("prefill_email");
            if (prefillEmail != null && etEmailLogin != null) {
                etEmailLogin.setText(prefillEmail);
                // Focus on password field for better UX
                if (etPasswordLogin != null) {
                    etPasswordLogin.requestFocus();
                }
            }
        }

        btnSignIn.setOnClickListener(view -> {
            String email = etEmailLogin.getText() == null ? "" : etEmailLogin.getText().toString().trim();
            String pass  = etPasswordLogin.getText() == null ? "" : etPasswordLogin.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(requireContext(), "Enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = new DatabaseHelper(requireContext());
            boolean ok = db.validateUser(email, pass); // assumes your helper provides this

            if (ok) {
                // Get user profile to retrieve first and last name
                DatabaseHelper.UserProfile profile = db.getUserProfile(email);
                String firstName = profile != null ? profile.first : "";
                String lastName = profile != null ? profile.last : "";
                
                new SessionManager(requireContext()).login(email, firstName, lastName);
                Intent intent = new Intent(requireContext(), HomeActivity.class);
                startActivity(intent);
                requireActivity().finish();
            } else {
                Toast.makeText(requireContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
