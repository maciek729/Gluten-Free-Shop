package com.example.sklepbezglutenowy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private CheckBox cbTerms;
    private ImageView ivPasswordToggle, ivConfirmPasswordToggle;
    private TextView tvLoginLink;
    private DatabaseHelper databaseHelper;

    // New UI components for user type selection
    private RadioButton rbUser, rbAdmin; // Radio buttons for User or Admin selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Setup password visibility toggles
        setupPasswordToggle(ivPasswordToggle, etPassword);
        setupPasswordToggle(ivConfirmPasswordToggle, etConfirmPassword);

        // Set click listeners
        btnRegister.setOnClickListener(v -> validateAndRegister());
        tvLoginLink.setOnClickListener(v -> navigateToLogin());
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        cbTerms = findViewById(R.id.cbTerms);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        ivConfirmPasswordToggle = findViewById(R.id.ivConfirmPasswordToggle);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Initialize new radio buttons for user role selection
        rbUser = findViewById(R.id.rbUser);
        rbAdmin = findViewById(R.id.rbAdmin);
    }

    private void setupPasswordToggle(ImageView toggle, EditText editText) {
        toggle.setOnClickListener(v -> {
            if (editText.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggle.setImageResource(R.drawable.ic_visibility_off);
            } else {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggle.setImageResource(R.drawable.ic_visibility);
            }
            editText.setSelection(editText.getText().length());
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void validateAndRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInputs(name, email, password, confirmPassword)) {
            return;
        }

        if (databaseHelper.checkEmailExists(email)) {
            etEmail.setError("Email already registered");
            return;
        }

        // Get the user type (Admin or User)
        String userType = rbAdmin.isChecked() ? "Admin" : "User";

        if (databaseHelper.registerUser(name, email, password, cbTerms.isChecked(), userType)) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            etPassword.setError("Password must contain at least 1 number");
            return false;
        }

        if (!password.matches(".*[!@#$%^&*].*")) {
            etPassword.setError("Password must contain at least 1 special character");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            return false;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "You must accept the terms", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Ensure a user role (either Admin or User) is selected
        if (!rbUser.isChecked() && !rbAdmin.isChecked()) {
            Toast.makeText(this, "You must select a user role", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
