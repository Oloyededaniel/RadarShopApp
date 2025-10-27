package com.radar.radarshop;

import java.io.Serializable;

/** Category model for organizing products. */
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String description;
    private final String imageUrl;

    public Category(int id, String name, String description) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.description = description == null ? "" : description;
        this.imageUrl = "";
    }
    
    public Category(int id, String name, String description, String imageUrl) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.description = description == null ? "" : description;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }

    @Override public String toString() {
        return name;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return id == category.id;
    }

    @Override public int hashCode() {
        return Integer.hashCode(id);
    }
}
