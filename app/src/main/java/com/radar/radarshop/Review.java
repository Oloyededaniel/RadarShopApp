package com.radar.radarshop;

import java.io.Serializable;

/** User review for a product. */
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;           // db row id
    private final String userEmail; // reviewer
    private final int productId;    // product FK
    private final int rating;       // 1..5
    private final String comment;   // optional

    public Review(int id, String userEmail, int productId, int rating, String comment) {
        this.id = id;
        this.userEmail = userEmail == null ? "" : userEmail;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment == null ? "" : comment;
    }

    public int getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public int getProductId() { return productId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}
