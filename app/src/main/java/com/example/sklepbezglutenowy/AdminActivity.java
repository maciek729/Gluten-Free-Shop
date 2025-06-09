package com.example.sklepbezglutenowy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.sklepbezglutenowy.models.User;
import com.example.sklepbezglutenowy.models.Product;
import com.example.sklepbezglutenowy.ProductFileHelper;

import java.io.IOException;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LoginPrefs";

    private TextView tvUserName;
    private EditText etProductId, etProductName, etProductPrice, etProductDesc;
    private Button btnAddProduct, btnEditProduct, btnDeleteProduct, btnLogout;
    private LinearLayout llProductList;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize UI components
        tvUserName = findViewById(R.id.tvUserName);
        etProductId = findViewById(R.id.etProductId);
        etProductName = findViewById(R.id.etProductName);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductDesc = findViewById(R.id.etProductDesc);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnEditProduct = findViewById(R.id.btnEditProduct);
        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);
        btnLogout = findViewById(R.id.btnLogout);
        llProductList = findViewById(R.id.llProductList);

        displayUserInfo();
        displayProducts();

        btnLogout.setOnClickListener(v -> logoutUser());

        btnAddProduct.setOnClickListener(v -> {
            String name = etProductName.getText().toString().trim();
            String price = etProductPrice.getText().toString().trim();
            String desc = etProductDesc.getText().toString().trim();
            if (!name.isEmpty() && !price.isEmpty() && !desc.isEmpty()) {
                try {
                    ProductFileHelper.addProduct(this, name, price, desc);
                    Toast.makeText(this, "✅ Product added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    displayProducts(); // Refresh the list
                } catch (IOException e) {
                    Toast.makeText(this, "❌ Error adding product", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "⚠️ All fields required", Toast.LENGTH_SHORT).show();
            }
        });

        btnEditProduct.setOnClickListener(v -> {
            String idText = etProductId.getText().toString().trim();
            String name = etProductName.getText().toString().trim();
            String price = etProductPrice.getText().toString().trim();
            String desc = etProductDesc.getText().toString().trim();
            if (!idText.isEmpty() && !name.isEmpty() && !price.isEmpty() && !desc.isEmpty()) {
                try {
                    int id = Integer.parseInt(idText);
                    ProductFileHelper.updateProductById(this, id, name, price, desc);
                    Toast.makeText(this, "✅ Product updated successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    displayProducts(); // Refresh the list
                } catch (IOException e) {
                    Toast.makeText(this, "❌ Error updating product", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "⚠️ Invalid ID", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "⚠️ All fields required", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteProduct.setOnClickListener(v -> {
            String idText = etProductId.getText().toString().trim();
            if (!idText.isEmpty()) {
                try {
                    int id = Integer.parseInt(idText);
                    ProductFileHelper.deleteProductById(this, id);
                    Toast.makeText(this, "✅ Product deleted successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    displayProducts(); // Refresh the list
                } catch (IOException e) {
                    Toast.makeText(this, "❌ Error deleting product", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "⚠️ Invalid ID", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "⚠️ Enter product ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProducts() {
        llProductList.removeAllViews(); // Clear before displaying

        try {
            List<Product> productList = ProductFileHelper.getProductList(this);

            if (productList.isEmpty()) {
                // Show empty state
                TextView emptyView = new TextView(this);
                emptyView.setText("📦 No products found\n\nAdd your first gluten-free product above!");
                emptyView.setTextSize(16);
                emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                emptyView.setGravity(android.view.Gravity.CENTER);
                emptyView.setPadding(32, 64, 32, 64);
                llProductList.addView(emptyView);
                return;
            }

            for (Product product : productList) {
                CardView productCard = new CardView(this);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                cardParams.setMargins(0, 0, 0, 16);
                productCard.setLayoutParams(cardParams);
                productCard.setRadius(12f);
                productCard.setCardElevation(4f);
                productCard.setCardBackgroundColor(getResources().getColor(android.R.color.white));

                LinearLayout productLayout = new LinearLayout(this);
                productLayout.setOrientation(LinearLayout.VERTICAL);
                productLayout.setPadding(20, 20, 20, 20);

                // Header with ID and Price
                LinearLayout headerLayout = new LinearLayout(this);
                headerLayout.setOrientation(LinearLayout.HORIZONTAL);
                headerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                headerParams.setMargins(0, 0, 0, 12);
                headerLayout.setLayoutParams(headerParams);

                TextView idView = new TextView(this);
                idView.setText("ID: " + product.getId());
                idView.setTextSize(12);
                idView.setTextColor(getResources().getColor(R.color.primary_green));
                idView.setPadding(12, 6, 12, 6);
                idView.setBackground(getResources().getDrawable(R.drawable.badge_background));
                LinearLayout.LayoutParams idParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                idParams.setMargins(0, 0, 16, 0);
                idView.setLayoutParams(idParams);

                TextView priceView = new TextView(this);
                priceView.setText(String.format("%.2f zł", product.getPrice()));
                priceView.setTextSize(16);
                priceView.setTextColor(getResources().getColor(R.color.success_green));
                priceView.setTypeface(null, android.graphics.Typeface.BOLD);

                headerLayout.addView(idView);
                headerLayout.addView(priceView);

                // Product Name
                TextView nameView = new TextView(this);
                nameView.setText(product.getName());
                nameView.setTextSize(18);
                nameView.setTextColor(getResources().getColor(R.color.text_primary));
                nameView.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                nameParams.setMargins(0, 0, 0, 8);
                nameView.setLayoutParams(nameParams);

                // Product Description
                TextView descriptionView = new TextView(this);
                descriptionView.setText(product.getDescription());
                descriptionView.setTextSize(14);
                descriptionView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                descParams.setMargins(0, 0, 0, 16);
                descriptionView.setLayoutParams(descParams);

                // Divider
                View divider = new View(this);
                divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                dividerParams.setMargins(0, 0, 0, 12);
                divider.setLayoutParams(dividerParams);

                // Edit hint
                TextView editHint = new TextView(this);
                editHint.setText("✏️ Tap to edit this product");
                editHint.setTextSize(12);
                editHint.setTextColor(getResources().getColor(R.color.primary_green));
                editHint.setGravity(android.view.Gravity.CENTER);
                editHint.setAlpha(0.7f);

                // Add click listener to fill form for editing
                productCard.setOnClickListener(v -> {
                    etProductId.setText(String.valueOf(product.getId()));
                    etProductName.setText(product.getName());
                    etProductPrice.setText(String.valueOf(product.getPrice()));
                    etProductDesc.setText(product.getDescription());
                    Toast.makeText(this, "📝 Product loaded for editing", Toast.LENGTH_SHORT).show();
                });

                // Add all views to product layout
                productLayout.addView(headerLayout);
                productLayout.addView(nameView);
                productLayout.addView(descriptionView);
                productLayout.addView(divider);
                productLayout.addView(editHint);

                productCard.addView(productLayout);
                llProductList.addView(productCard);
            }

        } catch (IOException e) {
            Toast.makeText(this, "❌ Error loading products", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void clearFields() {
        etProductId.setText("");
        etProductName.setText("");
        etProductPrice.setText("");
        etProductDesc.setText("");
    }

    private void displayUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        if (email == null || email.isEmpty()) {
            Log.w("AdminActivity", "No email found in SharedPreferences");
            tvUserName.setText("🌾 Admin Panel");
            return;
        }

        try {
            User user = databaseHelper.getUser(email);
            if (user != null && user.getName() != null && !user.getName().trim().isEmpty()) {
                tvUserName.setText("🌾 Welcome, " + user.getName());
            } else {
                tvUserName.setText("🌾 Admin Panel");
            }
        } catch (Exception e) {
            Log.e("AdminActivity", "Error retrieving user info", e);
            tvUserName.setText("🌾 Admin Panel");
        }
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}