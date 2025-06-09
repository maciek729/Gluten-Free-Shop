package com.example.sklepbezglutenowy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import com.example.sklepbezglutenowy.models.CartItem;
import com.example.sklepbezglutenowy.models.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "GlutenFreeApp.db";
    private static final int DATABASE_VERSION = 2;

    // Table and column constants
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_NAME = "user_name";
    public static final String COLUMN_USER_EMAIL = "user_email";
    public static final String COLUMN_USER_PASSWORD = "user_password";
    public static final String COLUMN_SALT = "salt";
    public static final String COLUMN_TERMS_ACCEPTED = "terms_accepted";
    public static final String COLUMN_USER_TYPE = "user_type";

    // Cart table
    public static final String TABLE_CART = "cart";
    public static final String COLUMN_CART_ID = "id";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_PRODUCT_NAME = "product_name";
    public static final String COLUMN_PRODUCT_PRICE = "price";
    public static final String COLUMN_CART_USER_ID = "user_id";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_NAME + " TEXT, " +
                    COLUMN_USER_EMAIL + " TEXT, " +
                    COLUMN_USER_PASSWORD + " TEXT, " +
                    COLUMN_SALT + " TEXT, " +
                    COLUMN_TERMS_ACCEPTED + " INTEGER, " +
                    COLUMN_USER_TYPE + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_CART =
            "CREATE TABLE " + TABLE_CART + " (" +
                    COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCT_ID + " INTEGER, " +
                    COLUMN_PRODUCT_NAME + " TEXT, " +
                    COLUMN_PRODUCT_PRICE + " REAL, " +
                    COLUMN_CART_USER_ID + " INTEGER" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL(CREATE_TABLE_CART);
            Log.d(TAG, "Database created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        try {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_TYPE + " TEXT");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
        }
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.decode(salt, Base64.DEFAULT));
            byte[] hashedBytes = digest.digest(password.getBytes());
            return Base64.encodeToString(hashedBytes, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to hash password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    // Cart functionality
    public boolean addToCart(int productId, String productName, double price, int userId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_PRODUCT_ID, productId);
            values.put(COLUMN_PRODUCT_NAME, productName);
            values.put(COLUMN_PRODUCT_PRICE, price);
            values.put(COLUMN_CART_USER_ID, userId);
            long result = db.insert(TABLE_CART, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart", e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_CART, null,
                    COLUMN_CART_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(COLUMN_CART_ID);
                int productIdIndex = cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID);
                int productNameIndex = cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME);
                int priceIndex = cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE);

                do {
                    int id = cursor.getInt(idIndex);
                    int productId = cursor.getInt(productIdIndex);
                    String productName = cursor.getString(productNameIndex);
                    double price = cursor.getDouble(priceIndex);
                    cartItems.add(new CartItem(id, productId, productName, price, userId));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting cart items", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return cartItems;
    }
    public boolean removeCartItem(int cartItemId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int result = db.delete(TABLE_CART, COLUMN_CART_ID + " = ?", new String[]{String.valueOf(cartItemId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error removing cart item", e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }


    // Register user (unchanged)
    public boolean registerUser(String name, String email, String password, boolean termsAccepted, String userType) {
        if (checkEmailExists(email)) {
            Log.w(TAG, "Registration failed - email already exists");
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);

            values.put(COLUMN_USER_NAME, name);
            values.put(COLUMN_USER_EMAIL, email);
            values.put(COLUMN_USER_PASSWORD, hashedPassword);
            values.put(COLUMN_SALT, salt);
            values.put(COLUMN_TERMS_ACCEPTED, termsAccepted ? 1 : 0);
            values.put(COLUMN_USER_TYPE, userType);  // Store user type

            long result = db.insert(TABLE_USERS, null, values);
            if (result != -1) {
                Log.d(TAG, "User registered successfully: " + email);
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error registering user", e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    // Authenticate user by email (unchanged)
    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_USERS, null,
                    COLUMN_USER_EMAIL + " = ?",
                    new String[]{email}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndex(COLUMN_USER_PASSWORD);
                int saltIndex = cursor.getColumnIndex(COLUMN_SALT);

                if (passwordIndex >= 0 && saltIndex >= 0) {
                    String storedHash = cursor.getString(passwordIndex);
                    String salt = cursor.getString(saltIndex);
                    String inputHash = hashPassword(password, salt);

                    if (storedHash != null && storedHash.equals(inputHash)) {
                        return User.fromCursor(cursor);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Authentication error", e);
            return null;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    // New method to authenticate user by username
    public User authenticateUserByUsername(String username, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_USERS, null,
                    COLUMN_USER_NAME + " = ?",
                    new String[]{username}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndex(COLUMN_USER_PASSWORD);
                int saltIndex = cursor.getColumnIndex(COLUMN_SALT);

                if (passwordIndex >= 0 && saltIndex >= 0) {
                    String storedHash = cursor.getString(passwordIndex);
                    String salt = cursor.getString(saltIndex);
                    String inputHash = hashPassword(password, salt);

                    if (storedHash != null && storedHash.equals(inputHash)) {
                        return User.fromCursor(cursor);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Authentication error by username", e);
            return null;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    // Fetch user by email (unchanged)
    public User getUser(String email) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_USERS, null,
                    COLUMN_USER_EMAIL + " = ?",
                    new String[]{email}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return User.fromCursor(cursor);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user", e);
            return null;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    // Fetch user type by email (unchanged)
    public String getUserType(String email) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_TYPE},
                    COLUMN_USER_EMAIL + " = ?",
                    new String[]{email}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(COLUMN_USER_TYPE));
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching user type", e);
            return null;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    // Check if email exists (unchanged)
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                    COLUMN_USER_EMAIL + " = ?",
                    new String[]{email}, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking email existence", e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }
}
