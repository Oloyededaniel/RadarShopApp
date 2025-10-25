/*package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpFragment extends Fragment {

    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etConfirm;
    private CheckBox cbTerms;
    private MaterialButton btnCreateAccount;
    private TextView tvTermsCombined; // optional: set combined styled text if you want

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
        cbTerms     = v.findViewById(R.id.cbTerms);
        btnCreateAccount = v.findViewById(R.id.btnCreateAccount);
        tvTermsCombined  = v.findViewById(R.id.tvTermsCombined); // already present in XML

        // Ensure password fields are obscured (safety if XML styles change later)
        if (etPassword != null) etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        if (etConfirm  != null) etConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());

        btnCreateAccount.setOnClickListener(view -> {
            String first = val(etFirstName);
            String last  = val(etLastName);
            String email = val(etEmail);
            String pass  = val(etPassword);
            String conf  = val(etConfirm);

            if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last) ||
                    TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(conf)) {
                Toast.makeText(requireContext(), "Please complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.length() < 8) {
                Toast.makeText(requireContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(conf)) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbTerms.isChecked()) {
                Toast.makeText(requireContext(), "Accept the Terms to continue", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = new DatabaseHelper(requireContext());
            boolean created = db.insertUser(first, last, email, pass); // assumes you have this

            if (created) {
                new SessionManager(requireContext()).login(email);
                Intent intent = new Intent(requireContext(), HomeActivity.class);
                startActivity(intent);
                requireActivity().finish();
            } else {
                Toast.makeText(requireContext(), "Email already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String val(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
*/
package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpFragment extends Fragment {

    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etConfirm;
    private CheckBox cbTerms;
    private MaterialButton btnCreateAccount;
    private TextView tvTermsCombined;

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
        cbTerms     = v.findViewById(R.id.cbTerms);
        btnCreateAccount = v.findViewById(R.id.btnCreateAccount);
        tvTermsCombined  = v.findViewById(R.id.tvTermsCombined);

        // Safety: ensure password fields are obscured even if XML changes later.
        if (etPassword != null) etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        if (etConfirm  != null) etConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());

        btnCreateAccount.setOnClickListener(view -> {
            String first = val(etFirstName);
            String last  = val(etLastName);
            String email = val(etEmail);
            String pass  = val(etPassword);
            String conf  = val(etConfirm);

            btnCreateAccount.setEnabled(false);
            cbTerms.setOnCheckedChangeListener((b, checked) -> btnCreateAccount.setEnabled(checked));


            if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last) ||
                    TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(conf)) {
                toast("Please complete all fields");
                return;
            }

            if (pass.length() < 8) {
                toast("Password must be at least 8 characters");
                return;
            }

            if (!pass.equals(conf)) {
                toast("Passwords do not match");
                return;
            }

            if (!cbTerms.isChecked()) {
                toast("Accept the Terms to continue");
                return;
            }

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
    }

    private String val(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
