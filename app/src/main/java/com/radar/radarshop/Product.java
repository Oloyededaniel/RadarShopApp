package com.radar.radarshop;

import java.io.Serializable;
import java.util.Objects;

/** Enhanced product model with comprehensive product information. */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String description;
    private final String detailedDescription;
    private final double price;
    private final int categoryId;
    private final String categoryName;
    private final int stockQuantity;
    private final double averageRating;
    private final int totalReviews;
    private final String sku;
    private final String brand;
    private final double weight;
    private final String dimensions;
    private final String createdAt;
    private final String updatedAt;

    public Product(int id, String name, String description, String detailedDescription, 
                  double price, int categoryId, String categoryName, int stockQuantity,
                  double averageRating, int totalReviews, String sku, String brand,
                  double weight, String dimensions, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.description = description == null ? "" : description;
        this.detailedDescription = detailedDescription == null ? "" : detailedDescription;
        this.price = price;
        this.categoryId = categoryId;
        this.categoryName = categoryName == null ? "" : categoryName;
        this.stockQuantity = stockQuantity;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.sku = sku == null ? "" : sku;
        this.brand = brand == null ? "" : brand;
        this.weight = weight;
        this.dimensions = dimensions == null ? "" : dimensions;
        this.createdAt = createdAt == null ? "" : createdAt;
        this.updatedAt = updatedAt == null ? "" : updatedAt;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDetailedDescription() { return detailedDescription; }
    public double getPrice() { return price; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public int getStockQuantity() { return stockQuantity; }
    public double getAverageRating() { return averageRating; }
    public int getTotalReviews() { return totalReviews; }
    public String getSku() { return sku; }
    public String getBrand() { return brand; }
    public double getWeight() { return weight; }
    public String getDimensions() { return dimensions; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Helper methods
    public boolean isInStock() { return stockQuantity > 0; }
    public String getFormattedPrice() { return String.format("$%.2f", price); }
    public String getFormattedRating() { return String.format("%.1f", averageRating); }

    @Override public String toString() {
        return name + " - " + getFormattedPrice() + " (" + stockQuantity + " in stock)";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }
}
