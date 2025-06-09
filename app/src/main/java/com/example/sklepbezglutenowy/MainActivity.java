package com.example.sklepbezglutenowy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if user is already logged in
        boolean isLoggedIn = sharedPreferences.getBoolean("remember", false);
        String userEmail = sharedPreferences.getString("email", null);

        if (isLoggedIn && userEmail != null) {
            // User is logged in, go to Product Catalog
            startActivity(new Intent(this, ProductCatalogActivity.class));
        } else {
            // User is not logged in, go to Login
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish(); // Close MainActivity
    }
}