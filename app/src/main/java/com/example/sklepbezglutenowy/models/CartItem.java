package com.example.sklepbezglutenowy.models;

public class CartItem {
    private int id; // Auto-increment ID from DB
    private int productId;
    private String productName;
    private double price;
    private int userId; // Replaces userEmail

    // Constructor without DB ID
    public CartItem(int productId, String productName, double price, int userId) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.userId = userId;
    }

    // Constructor with DB ID
    public CartItem(int id, int productId, String productName, double price, int userId) {
        this(productId, productName, price, userId);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public int getUserId() {
        return userId;
    }
}
