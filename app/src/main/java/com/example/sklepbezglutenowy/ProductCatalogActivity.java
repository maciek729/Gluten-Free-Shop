package com.example.sklepbezglutenowy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.sklepbezglutenowy.models.Product;
import com.example.sklepbezglutenowy.models.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductCatalogActivity extends AppCompatActivity {
    private static final String TAG = "ProductCatalogActivity";
    private static final String PREFS_NAME = "LoginPrefs";

    private TextView tvWelcome;
    private Button btnLogout;
    private Button btnCart;
    private LinearLayout llProductCatalog;
    private Spinner spinnerSort;

    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_catalog);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        btnCart = findViewById(R.id.btnCart);
        llProductCatalog = findViewById(R.id.llProductCatalog);
        spinnerSort = findViewById(R.id.spinnerSort);

        displayUserInfo();

        btnLogout.setOnClickListener(v -> logout());

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductCatalogActivity.this, CartActivity.class);
            startActivity(intent);
        });

        setupSpinner();

        loadProductsFromFile();
    }

    private void displayUserInfo() {
        String email = sharedPreferences.getString("email", null);
        if (email == null || email.isEmpty()) {
            tvWelcome.setText(getString(R.string.welcome_default));
            return;
        }

        try {
            User user = databaseHelper.getUser(email);
            String name = user != null ? user.getName() : null;

            if (name != null && !name.trim().isEmpty()) {
                tvWelcome.setText(getString(R.string.welcome_message, name));
            } else {
                tvWelcome.setText(getString(R.string.welcome_default));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving user info", e);
            tvWelcome.setText(getString(R.string.welcome_default));
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSpinner() {
        // Define sort options
        String[] sortOptions = {
                "Sort by Name (A-Z)",
                "Sort by Name (Z-A)",
                "Sort by Price (Low to High)",
                "Sort by Price (High to Low)"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                sortOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);

        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (productList != null && !productList.isEmpty()) {
                    sortProductList(position);
                    displayProducts();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void sortProductList(int sortOption) {
        switch (sortOption) {
            case 0: // Name Ascending
                Collections.sort(productList, Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case 1: // Name Descending
                Collections.sort(productList, (p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
                break;
            case 2: // Price Low to High
                Collections.sort(productList, Comparator.comparingDouble(Product::getPrice));
                break;
            case 3: // Price High to Low
                Collections.sort(productList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                break;
            default:
                // No sorting or default sorting if needed
                break;
        }
    }

    private void loadProductsFromFile() {
        File file = new File(getFilesDir(), "products.txt");
        if (!file.exists()) {
            Toast.makeText(this, "No products found.", Toast.LENGTH_SHORT).show();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            productList = new ArrayList<>();
            llProductCatalog.removeAllViews();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    double price = Double.parseDouble(parts[2].trim());
                    String description = parts[3].trim();

                    productList.add(new Product(id, name, price, description));
                }
            }

            // Sort by default option (e.g., Name Ascending)
            sortProductList(0);
            displayProducts();
        } catch (Exception e) {
            Log.e(TAG, "Error reading products from file", e);
            Toast.makeText(this, "Failed to load products.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayProducts() {
        llProductCatalog.removeAllViews(); // Clear before displaying

        for (Product product : productList) {
            CardView productCard = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(16, 16, 16, 16);
            productCard.setLayoutParams(cardParams);
            productCard.setRadius(12f);
            productCard.setCardElevation(8f);
            productCard.setCardBackgroundColor(getResources().getColor(R.color.white));

            LinearLayout productLayout = new LinearLayout(this);
            productLayout.setOrientation(LinearLayout.VERTICAL);
            productLayout.setPadding(16, 16, 16, 16);

            TextView nameView = new TextView(this);
            nameView.setText("Name: " + product.getName());
            nameView.setTextSize(18);
            nameView.setTextColor(getResources().getColor(R.color.primary_text));

            TextView priceView = new TextView(this);
            priceView.setText("Price: " + String.format("%.2f", product.getPrice()) + " zł");
            priceView.setTextSize(16);
            priceView.setTextColor(getResources().getColor(R.color.secondary_text));

            TextView descriptionView = new TextView(this);
            descriptionView.setText("Description: " + product.getDescription());
            descriptionView.setTextSize(14);
            descriptionView.setTextColor(getResources().getColor(R.color.secondary_text));

            Button btnAddToCart = new Button(this);
            btnAddToCart.setText("Add to Cart");

            btnAddToCart.setOnClickListener(v -> {
                String email = sharedPreferences.getString("email", null);
                if (email != null) {
                    User user = databaseHelper.getUser(email);
                    if (user != null) {
                        int userId = user.getId();
                        boolean success = databaseHelper.addToCart(
                                product.getId(),
                                product.getName(),
                                product.getPrice(),
                                userId
                        );
                        if (success) {
                            Toast.makeText(this, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "You must be logged in to add to cart", Toast.LENGTH_SHORT).show();
                }
            });

            productLayout.addView(nameView);
            productLayout.addView(priceView);
            productLayout.addView(descriptionView);
            productLayout.addView(btnAddToCart);

            productCard.addView(productLayout);
            llProductCatalog.addView(productCard);
        }
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        databaseHelper.close();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}
