package com.radar.radarshop;

import java.io.Serializable;
import java.util.Objects;

/** Simple product model used by DatabaseHelper, adapters, and intents. */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String description;
    private final double price;
    private final String category;

    public Product(int id, String name, String description, double price, String category) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.description = description == null ? "" : description;
        this.price = price;
        this.category = category == null ? "" : category;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }

    @Override public String toString() {
        // Helpful for ListView fallback / debugging
        return name + " - $" + String.format("%.2f", price);
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
