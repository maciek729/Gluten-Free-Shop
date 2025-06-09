package com.example.sklepbezglutenowy.models;

public class Product {
    private int id;
    private String name;
    private double price;  // Use double for price
    private String description;

    // Constructor
    public Product(int id, String name, double price, String description) {
        this.id = id;
        this.name = name;
        this.price = price; // Store price as double
        this.description = description;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
