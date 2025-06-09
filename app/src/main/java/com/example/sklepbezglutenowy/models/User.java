package com.example.sklepbezglutenowy.models;

import android.database.Cursor;
import android.util.Log;

import com.example.sklepbezglutenowy.DatabaseHelper;

public class User {
    private final int id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final String salt;
    private final boolean termsAccepted;
    private final String userType;  // New field for user type

    // Updated constructor to include userType
    public User(int id, String name, String email,
                String passwordHash, String salt, boolean termsAccepted, String userType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.termsAccepted = termsAccepted;
        this.userType = userType;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    String getPasswordHash() { return passwordHash; }
    String getSalt() { return salt; }
    public boolean isTermsAccepted() { return termsAccepted; }
    public String getUserType() { return userType; }  // Getter for user type

    public static User fromCursor(Cursor cursor) {
        try {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
            int emailIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL);
            int passwordIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PASSWORD);
            int saltIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SALT);
            int termsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TERMS_ACCEPTED);
            int userTypeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_TYPE);  // Fetching userType

            if (idIndex >= 0 && nameIndex >= 0 && emailIndex >= 0 &&
                    passwordIndex >= 0 && saltIndex >= 0 && termsIndex >= 0 && userTypeIndex >= 0) {
                return new User(
                        cursor.getInt(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(emailIndex),
                        cursor.getString(passwordIndex),
                        cursor.getString(saltIndex),
                        cursor.getInt(termsIndex) == 1,
                        cursor.getString(userTypeIndex)  // Setting userType
                );
            }
            return null;
        } catch (Exception e) {
            Log.e("UserModel", "Error creating User from cursor", e);
            return null;
        }
    }
}
