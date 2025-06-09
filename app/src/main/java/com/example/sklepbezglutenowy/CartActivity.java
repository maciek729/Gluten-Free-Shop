package com.example.sklepbezglutenowy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.sklepbezglutenowy.models.CartItem;
import com.example.sklepbezglutenowy.models.User;

import java.util.List;
import java.util.Random;

public class CartActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "LoginPrefs";

    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private LinearLayout llCartItems;
    private TextView tvTotalPrice;
    private TextView tvCouponInfo; // TextView to show coupon info
    private Button btnBackToCatalog;
    private Button btnChangeCoupon; // New button for changing coupon

    // Define some coupons: code and discount percentage (e.g. 10 means 10%)
    private static final Coupon[] COUPONS = {
            new Coupon("SAVE10", 10),
            new Coupon("SAVE15", 15),
            new Coupon("SAVE20", 20),
            new Coupon("HALFOFF", 50)
    };

    private Coupon appliedCoupon; // currently applied coupon
    private double currentTotalPrice = 0.0; // total price before discount

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        llCartItems = findViewById(R.id.llCartItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvCouponInfo = findViewById(R.id.tvCouponInfo);
        btnBackToCatalog = findViewById(R.id.btnBackToCatalog);
        btnChangeCoupon = findViewById(R.id.btnChangeCoupon); // Initialize the new button

        btnBackToCatalog.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, ProductCatalogActivity.class);
            startActivity(intent);
            finish();
        });

        // Initially, no coupon applied yet
        appliedCoupon = null;

        btnChangeCoupon.setOnClickListener(v -> {
            if (currentTotalPrice <= 0) {
                Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            changeCoupon();
        });

        loadCartItems();
    }

    private void loadCartItems() {
        String email = sharedPreferences.getString("email", null);
        if (email == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = databaseHelper.getUser(email);
        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> cartItems = databaseHelper.getCartItems(user.getId());
        llCartItems.removeAllViews();

        if (cartItems.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Your cart is empty.");
            emptyView.setTextSize(18);
            llCartItems.addView(emptyView);
            tvTotalPrice.setText(""); // Clear total price if empty
            tvCouponInfo.setText("Coupon: None");
            appliedCoupon = null;
            currentTotalPrice = 0.0;
            return;
        }

        double totalPrice = 0;

        for (CartItem item : cartItems) {
            totalPrice += item.getPrice();

            CardView card = new CardView(this);
            card.setCardElevation(6f);
            card.setRadius(12f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 16, 16, 16);
            card.setLayoutParams(params);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(16, 16, 16, 16);

            TextView nameView = new TextView(this);
            nameView.setText("Name: " + item.getProductName());
            nameView.setTextSize(18);

            TextView priceView = new TextView(this);
            priceView.setText("Price: " + String.format("%.2f", item.getPrice()) + " zł");

            Button btnDelete = new Button(this);
            btnDelete.setText("Remove");
            btnDelete.setOnClickListener(v -> {
                boolean deleted = databaseHelper.removeCartItem(item.getId());
                if (deleted) {
                    Toast.makeText(this, item.getProductName() + " removed from cart", Toast.LENGTH_SHORT).show();
                    loadCartItems(); // Reload items to refresh UI & total
                } else {
                    Toast.makeText(this, "Failed to remove item", Toast.LENGTH_SHORT).show();
                }
            });

            layout.addView(nameView);
            layout.addView(priceView);
            layout.addView(btnDelete);

            card.addView(layout);
            llCartItems.addView(card);
        }

        currentTotalPrice = totalPrice;

        // If no coupon applied yet, pick a random one initially
        if (appliedCoupon == null) {
            appliedCoupon = getRandomCoupon();
        }

        updatePriceWithCoupon();
    }

    // Update UI after applying coupon and calculating discounted price
    private void updatePriceWithCoupon() {
        double discountedPrice = currentTotalPrice;
        if (appliedCoupon != null) {
            discountedPrice = currentTotalPrice - (currentTotalPrice * appliedCoupon.discountPercent / 100.0);
            tvCouponInfo.setText("Coupon applied: " + appliedCoupon.code + " (" + appliedCoupon.discountPercent + "% off)");
        } else {
            tvCouponInfo.setText("Coupon: None");
        }
        tvTotalPrice.setText("Total after discount: " + String.format("%.2f", discountedPrice) + " zł");
    }

    // Method to change coupon on button click
    private void changeCoupon() {
        Coupon newCoupon;
        do {
            newCoupon = getRandomCoupon();
        } while (newCoupon == appliedCoupon); // Ensure a different coupon

        appliedCoupon = newCoupon;
        Toast.makeText(this, "New coupon applied: " + appliedCoupon.code, Toast.LENGTH_SHORT).show();
        updatePriceWithCoupon();
    }

    private Coupon getRandomCoupon() {
        Random random = new Random();
        int index = random.nextInt(COUPONS.length);
        return COUPONS[index];
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    // Coupon class to hold coupon data
    private static class Coupon {
        String code;
        int discountPercent;

        Coupon(String code, int discountPercent) {
            this.code = code;
            this.discountPercent = discountPercent;
        }
    }
}
