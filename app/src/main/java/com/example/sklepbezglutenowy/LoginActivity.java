package com.example.sklepbezglutenowy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sklepbezglutenowy.models.User;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        // Pre-fill email if coming from registration
        String registeredEmail = getIntent().getStringExtra("email");
        if (registeredEmail != null) {
            etEmail.setText(registeredEmail);
            etPassword.requestFocus(); // Move focus to password field
        }

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });
    }

    private void loginUser() {
        String input = etEmail.getText().toString().trim(); // Now using 'input' to check for either username or email
        String password = etPassword.getText().toString().trim();

        // Input validation
        if (input.isEmpty()) {
            etEmail.setError("Email or Username is required");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        User authenticatedUser = null;

        // If input matches email pattern, authenticate using email
        if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            authenticatedUser = databaseHelper.authenticateUser(input, password);
        } else {
            // Otherwise, treat input as username
            authenticatedUser = databaseHelper.authenticateUserByUsername(input, password);
        }

        // Check if user is authenticated
        if (authenticatedUser != null) {
            // Get the user type (e.g., "admin" or "user")
            String userType = databaseHelper.getUserType(authenticatedUser.getEmail());

            // Save user email and type in preferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", authenticatedUser.getEmail());
            editor.putString("user_type", userType); // Save the user type
            editor.putString("user_name", authenticatedUser.getName()); // Save the user name
            editor.apply();

            // Redirect based on user type
            Intent intent;
            if ("Admin".equals(userType)) {
                // If the user is an admin, redirect to the AdminActivity
                intent = new Intent(LoginActivity.this, AdminActivity.class); // Replace with your AdminActivity
            } else {
                // If the user is a regular user, redirect to the ProductCatalogActivity
                intent = new Intent(LoginActivity.this, ProductCatalogActivity.class);
            }

            intent.putExtra("user_name", authenticatedUser.getName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(this, "Welcome back, " + authenticatedUser.getName() + "!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid email/username or password", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}
