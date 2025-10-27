package com.radar.radarshop;

import java.io.Serializable;

/** Product image model for storing multiple images per product. */
public class ProductImage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final int productId;
    private final String imageUrl;
    private final String imageType; // 'main', 'thumbnail', 'gallery'
    private final int displayOrder;

    public ProductImage(int id, int productId, String imageUrl, String imageType, int displayOrder) {
        this.id = id;
        this.productId = productId;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.imageType = imageType == null ? "gallery" : imageType;
        this.displayOrder = displayOrder;
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getImageUrl() { return imageUrl; }
    public String getImageType() { return imageType; }
    public int getDisplayOrder() { return displayOrder; }

    public boolean isMainImage() { return "main".equals(imageType); }
    public boolean isThumbnail() { return "thumbnail".equals(imageType); }
    public boolean isGalleryImage() { return "gallery".equals(imageType); }

    @Override public String toString() {
        return "ProductImage{" +
                "id=" + id +
                ", productId=" + productId +
                ", imageType='" + imageType + '\'' +
                ", order=" + displayOrder +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductImage)) return false;
        ProductImage that = (ProductImage) o;
        return id == that.id;
    }

    @Override public int hashCode() {
        return Integer.hashCode(id);
    }
}
