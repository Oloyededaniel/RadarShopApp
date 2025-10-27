package com.radar.radarshop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "radarshop.db";
    private static final int DB_VERSION = 4;

    // Users
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_FIRST = "firstname";
    public static final String COL_LAST = "lastname";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password_hash";
    public static final String COL_PHONE = "phone";
    public static final String COL_STREET = "street";
    public static final String COL_CITY = "city";
    public static final String COL_STATE = "state";
    public static final String COL_ZIP = "zip";
    public static final String COL_COUNTRY = "country";

    // Products
    public static final String TABLE_PRODUCTS = "products";
    public static final String COL_PRODUCT_ID = "id";
    public static final String COL_PRODUCT_NAME = "name";
    public static final String COL_PRODUCT_DESC = "description";
    public static final String COL_PRODUCT_PRICE = "price";
    public static final String COL_PRODUCT_CATEGORY = "category";

    // Wishlist
    public static final String TABLE_WISHLIST = "wishlist";
    public static final String COL_WISH_ID = "id";
    public static final String COL_WISH_EMAIL = "user_email";
    public static final String COL_WISH_PRODUCT_ID = "product_id";

    // Cart
    public static final String TABLE_CART = "cart";
    public static final String COL_CART_ID = "id";
    public static final String COL_CART_EMAIL = "user_email";
    public static final String COL_CART_PRODUCT_ID = "product_id";
    public static final String COL_CART_QUANTITY = "quantity";

    // Reviews
    public static final String TABLE_REVIEWS = "reviews";
    public static final String COL_REVIEW_ID = "id";
    public static final String COL_REVIEW_EMAIL = "user_email";
    public static final String COL_REVIEW_PRODUCT_ID = "product_id";
    public static final String COL_REVIEW_RATING = "rating";
    public static final String COL_REVIEW_COMMENT = "comment";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users
        db.execSQL(
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_FIRST + " TEXT, " +
                        COL_LAST + " TEXT, " +
                        COL_EMAIL + " TEXT UNIQUE COLLATE NOCASE, " +
                        COL_PASSWORD + " TEXT, " +
                        COL_PHONE + " TEXT, " +
                        COL_STREET + " TEXT, " +
                        COL_CITY + " TEXT, " +
                        COL_STATE + " TEXT, " +
                        COL_ZIP + " TEXT, " +
                        COL_COUNTRY + " TEXT" +
                        ");"
        );
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_email ON " + TABLE_USERS + "(" + COL_EMAIL + ");");

        // Products
        db.execSQL(
                "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                        COL_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_PRODUCT_NAME + " TEXT, " +
                        COL_PRODUCT_DESC + " TEXT, " +
                        COL_PRODUCT_PRICE + " REAL, " +
                        COL_PRODUCT_CATEGORY + " TEXT" +
                        ");"
        );

        // Wishlist
        db.execSQL(
                "CREATE TABLE " + TABLE_WISHLIST + " (" +
                        COL_WISH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_WISH_EMAIL + " TEXT, " +
                        COL_WISH_PRODUCT_ID + " INTEGER, " +
                        "UNIQUE(" + COL_WISH_EMAIL + ", " + COL_WISH_PRODUCT_ID + ")" +
                        ");"
        );

        // Cart
        db.execSQL(
                "CREATE TABLE " + TABLE_CART + " (" +
                        COL_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_CART_EMAIL + " TEXT, " +
                        COL_CART_PRODUCT_ID + " INTEGER, " +
                        COL_CART_QUANTITY + " INTEGER, " +
                        "UNIQUE(" + COL_CART_EMAIL + ", " + COL_CART_PRODUCT_ID + ")" +
                        ");"
        );

        // Reviews
        db.execSQL(
                "CREATE TABLE " + TABLE_REVIEWS + " (" +
                        COL_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_REVIEW_EMAIL + " TEXT, " +
                        COL_REVIEW_PRODUCT_ID + " INTEGER, " +
                        COL_REVIEW_RATING + " INTEGER, " +
                        COL_REVIEW_COMMENT + " TEXT" +
                        ");"
        );

        // Seed products (5 base)
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY +
                ") VALUES ('Gadget Pro','A high-tech gadget',199.99,'Electronics');");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY +
                ") VALUES ('Novel Book','An interesting novel',9.99,'Books');");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY +
                ") VALUES ('Smartphone X','Latest smartphone model',999.99,'Electronics');");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY +
                ") VALUES ('Running Shoes','Comfortable running shoes',59.99,'Sportswear');");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY +
                ") VALUES ('Coffee Mug','Ceramic coffee mug',12.99,'Kitchen');");

        // Seed reviews
        db.execSQL("INSERT INTO " + TABLE_REVIEWS + " (" +
                COL_REVIEW_EMAIL + "," + COL_REVIEW_PRODUCT_ID + "," + COL_REVIEW_RATING + "," + COL_REVIEW_COMMENT +
                ") VALUES ('user@example.com',1,4,'Great gadget with many features.');");
        db.execSQL("INSERT INTO " + TABLE_REVIEWS + " (" +
                COL_REVIEW_EMAIL + "," + COL_REVIEW_PRODUCT_ID + "," + COL_REVIEW_RATING + "," + COL_REVIEW_COMMENT +
                ") VALUES ('jane@example.com',3,5,'Loved this smartphone!');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WISHLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        onCreate(db);
    }

    /* AUTH */

    public boolean insertUser(String first, String last, String email, String plainPassword) {
        String normEmail = normalizeEmail(email);
        if (normEmail.isEmpty() || plainPassword == null) return false;
        if (userExists(normEmail)) return false;

        String hashed = hashPassword(plainPassword);
        if (hashed == null) return false;

        ContentValues cv = new ContentValues();
        cv.put(COL_FIRST, nonNull(first));
        cv.put(COL_LAST, nonNull(last));
        cv.put(COL_EMAIL, normEmail);
        cv.put(COL_PASSWORD, hashed);

        long row = getWritableDatabase().insert(TABLE_USERS, null, cv);
        return row != -1;
    }

    public boolean validateUser(String email, String plainPassword) {
        String normEmail = normalizeEmail(email);
        if (normEmail.isEmpty() || plainPassword == null) return false;

        String hashed = hashPassword(plainPassword);
        if (hashed == null) return false;

        try (Cursor c = getReadableDatabase().query(
                TABLE_USERS,
                new String[]{COL_PASSWORD},
                COL_EMAIL + "=?",
                new String[]{normEmail},
                null, null, null)) {
            if (c.moveToFirst()) {
                String stored = c.getString(0);
                return hashed.equals(stored);
            }
        }
        return false;
    }

    public boolean userExists(String email) {
        String normEmail = normalizeEmail(email);
        try (Cursor c = getReadableDatabase().query(
                TABLE_USERS, new String[]{COL_ID}, COL_EMAIL + "=?",
                new String[]{normEmail}, null, null, null)) {
            return c.moveToFirst();
        }
    }

    /* PROFILE */

    public static class UserProfile {
        public final String first, last, email, phone, street, city, state, zip, country;
        public UserProfile(String first, String last, String email,
                           String phone, String street, String city,
                           String state, String zip, String country) {
            this.first = first; this.last = last; this.email = email;
            this.phone = phone; this.street = street; this.city = city;
            this.state = state; this.zip = zip; this.country = country;
        }
        public String fullName() {
            String f = first == null ? "" : first.trim();
            String l = last == null ? "" : last.trim();
            return (f + " " + l).trim();
        }
    }

    public UserProfile getUserProfile(String email) {
        String normEmail = normalizeEmail(email);
        try (Cursor c = getReadableDatabase().query(
                TABLE_USERS,
                new String[]{COL_FIRST, COL_LAST, COL_EMAIL, COL_PHONE, COL_STREET, COL_CITY, COL_STATE, COL_ZIP, COL_COUNTRY},
                COL_EMAIL + "=?",
                new String[]{normEmail}, null, null, null)) {
            if (c.moveToFirst()) {
                return new UserProfile(
                        c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5),
                        c.getString(6), c.getString(7), c.getString(8)
                );
            }
        }
        return null;
    }

    public boolean updateProfile(String email, String first, String last, String phone,
                                 String street, String city, String state, String zip, String country) {
        String normEmail = normalizeEmail(email);
        ContentValues cv = new ContentValues();
        cv.put(COL_FIRST, nonNull(first));
        cv.put(COL_LAST, nonNull(last));
        cv.put(COL_PHONE, nonNull(phone));
        cv.put(COL_STREET, nonNull(street));
        cv.put(COL_CITY, nonNull(city));
        cv.put(COL_STATE, nonNull(state));
        cv.put(COL_ZIP, nonNull(zip));
        cv.put(COL_COUNTRY, nonNull(country));
        int rows = getWritableDatabase().update(TABLE_USERS, cv, COL_EMAIL + "=?", new String[]{normEmail});
        return rows > 0;
    }

    public boolean changePassword(String email, String newPlainPassword) {
        String normEmail = normalizeEmail(email);
        String hashed = hashPassword(newPlainPassword);
        if (normEmail.isEmpty() || hashed == null) return false;
        ContentValues cv = new ContentValues();
        cv.put(COL_PASSWORD, hashed);
        int rows = getWritableDatabase().update(TABLE_USERS, cv, COL_EMAIL + "=?", new String[]{normEmail});
        return rows > 0;
    }

    public boolean verifyPassword(String email, String plainPassword) {
        String normEmail = normalizeEmail(email);
        if (normEmail.isEmpty() || plainPassword == null) return false;

        String hashed = hashPassword(plainPassword);
        if (hashed == null) return false;

        try (Cursor c = getReadableDatabase().query(
                TABLE_USERS,
                new String[]{COL_PASSWORD},
                COL_EMAIL + "=?",
                new String[]{normEmail},
                null, null, null)) {
            if (c.moveToFirst()) {
                String stored = c.getString(0);
                return hashed.equals(stored);
            }
        }
        return false;
    }

    public boolean updatePassword(String email, String newPlainPassword) {
        return changePassword(email, newPlainPassword);
    }

    public boolean deleteUser(String email) {
        String normEmail = normalizeEmail(email);
        if (normEmail.isEmpty()) return false;
        
        SQLiteDatabase db = getWritableDatabase();
        try {
            // Delete user's wishlist items
            db.delete(TABLE_WISHLIST, COL_WISH_EMAIL + "=?", new String[]{normEmail});
            
            // Delete user's reviews
            db.delete(TABLE_REVIEWS, COL_REVIEW_EMAIL + "=?", new String[]{normEmail});
            
            // Delete user account
            int rows = db.delete(TABLE_USERS, COL_EMAIL + "=?", new String[]{normEmail});
            return rows > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /* PRODUCTS */

    public long insertProduct(String name, String desc, double price, String category) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PRODUCT_NAME, name);
        cv.put(COL_PRODUCT_DESC, desc);
        cv.put(COL_PRODUCT_PRICE, price);
        cv.put(COL_PRODUCT_CATEGORY, category);
        return getWritableDatabase().insert(TABLE_PRODUCTS, null, cv);
    }

    public List<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE_PRODUCTS,
                new String[]{COL_PRODUCT_ID, COL_PRODUCT_NAME, COL_PRODUCT_DESC, COL_PRODUCT_PRICE, COL_PRODUCT_CATEGORY},
                null, null, null, null, null)) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                double price = c.getDouble(3);
                String category = c.getString(4);
                list.add(new Product(id, name, desc, price, category));
            }
        }
        return list;
    }

    public List<Product> getProducts(String nameFilter, String categoryFilter, Double minPrice, Double maxPrice) {
        ArrayList<Product> list = new ArrayList<>();
        StringBuilder where = new StringBuilder();
        ArrayList<String> args = new ArrayList<>();

        if (nameFilter != null && !nameFilter.isEmpty()) {
            where.append(COL_PRODUCT_NAME).append(" LIKE ?");
            args.add("%" + nameFilter + "%");
        }
        if (categoryFilter != null) {
            if (where.length() > 0) where.append(" AND ");
            where.append(COL_PRODUCT_CATEGORY).append("=?");
            args.add(categoryFilter);
        }
        if (minPrice != null) {
            if (where.length() > 0) where.append(" AND ");
            where.append(COL_PRODUCT_PRICE).append(">=?");
            args.add(String.valueOf(minPrice));
        }
        if (maxPrice != null) {
            if (where.length() > 0) where.append(" AND ");
            where.append(COL_PRODUCT_PRICE).append("<=?");
            args.add(String.valueOf(maxPrice));
        }

        try (Cursor c = getReadableDatabase().query(
                TABLE_PRODUCTS,
                new String[]{COL_PRODUCT_ID, COL_PRODUCT_NAME, COL_PRODUCT_DESC, COL_PRODUCT_PRICE, COL_PRODUCT_CATEGORY},
                where.length() == 0 ? null : where.toString(),
                args.isEmpty() ? null : args.toArray(new String[0]),
                null, null, null)) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                double price = c.getDouble(3);
                String category = c.getString(4);
                list.add(new Product(id, name, desc, price, category));
            }
        }
        return list;
    }

    public List<Product> getFilteredProducts(String category, String nameQuery, double minPrice, double maxPrice) {
        String catFilter = (category == null || category.equalsIgnoreCase("All")) ? null : category;
        String nameFilter = (nameQuery == null || nameQuery.isEmpty()) ? null : nameQuery;
        return getProducts(nameFilter, catFilter, minPrice, maxPrice);
    }

    public List<String> getAllCategories() {
        ArrayList<String> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(true,
                TABLE_PRODUCTS,
                new String[]{COL_PRODUCT_CATEGORY},
                null, null, null, null, null, null)) {
            while (c.moveToNext()) out.add(c.getString(0));
        }
        return out;
    }

    /* WISHLIST */

    public boolean addToWishlist(String userEmail, int productId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_WISH_EMAIL, userEmail);
        cv.put(COL_WISH_PRODUCT_ID, productId);
        long row = getWritableDatabase().insert(TABLE_WISHLIST, null, cv);
        return row != -1;
    }

    public boolean removeFromWishlist(String userEmail, int productId) {
        int rows = getWritableDatabase().delete(
                TABLE_WISHLIST,
                COL_WISH_EMAIL + "=? AND " + COL_WISH_PRODUCT_ID + "=?",
                new String[]{userEmail, String.valueOf(productId)});
        return rows > 0;
    }

    public List<Product> getWishlist(String userEmail) {
        ArrayList<Product> list = new ArrayList<>();
        String sql =
                "SELECT p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_DESC + ", " +
                        "p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_CATEGORY +
                        " FROM " + TABLE_PRODUCTS + " p " +
                        " JOIN " + TABLE_WISHLIST + " w ON p." + COL_PRODUCT_ID + " = w." + COL_WISH_PRODUCT_ID +
                        " WHERE w." + COL_WISH_EMAIL + " = ?";

        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{userEmail})) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                double price = c.getDouble(3);
                String category = c.getString(4);
                list.add(new Product(id, name, desc, price, category));
            }
        }
        return list;
    }

    /* CART */

    public boolean addToCart(String userEmail, int productId, int itemQuantity) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CART_EMAIL, userEmail);
        cv.put(COL_CART_PRODUCT_ID, productId);
        cv.put(COL_CART_QUANTITY, itemQuantity);
        long row = getWritableDatabase().insert(TABLE_CART, null, cv);
        return row != -1;
    }

    public boolean removeFromCart(String userEmail, int productId) {
        int rows = getWritableDatabase().delete(
                TABLE_CART,
                COL_CART_EMAIL + "=? AND " + COL_CART_PRODUCT_ID + "=?",
                new String[]{userEmail, String.valueOf(productId)});
        return rows > 0;
    }

    // TODO: add quantity to returned list
    public List<Product> getCart(String userEmail) {
        ArrayList<Product> list = new ArrayList<>();
        String sql =
                "SELECT p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_DESC + ", " +
                        "p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_CATEGORY +
                        " FROM " + TABLE_PRODUCTS + " p " +
                        " JOIN " + TABLE_CART + " w ON p." + COL_PRODUCT_ID + " = w." + COL_CART_PRODUCT_ID +
                        " WHERE w." + COL_CART_EMAIL + " = ?";

        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{userEmail})) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                double price = c.getDouble(3);
                String category = c.getString(4);
                list.add(new Product(id, name, desc, price, category));
            }
        }
        return list;
    }

    /* REVIEWS */

    public boolean addReview(String userEmail, int productId, int rating, String comment) {
        ContentValues cv = new ContentValues();
        cv.put(COL_REVIEW_EMAIL, userEmail);
        cv.put(COL_REVIEW_PRODUCT_ID, productId);
        cv.put(COL_REVIEW_RATING, rating);
        cv.put(COL_REVIEW_COMMENT, comment);
        long row = getWritableDatabase().insert(TABLE_REVIEWS, null, cv);
        return row != -1;
    }

    public List<Review> getReviews(int productId) {
        ArrayList<Review> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE_REVIEWS,
                new String[]{COL_REVIEW_ID, COL_REVIEW_EMAIL, COL_REVIEW_PRODUCT_ID, COL_REVIEW_RATING, COL_REVIEW_COMMENT},
                COL_REVIEW_PRODUCT_ID + "=?",
                new String[]{String.valueOf(productId)},
                null, null, null)) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String email = c.getString(1);
                int prodId = c.getInt(2);
                int rating = c.getInt(3);
                String comment = c.getString(4);
                out.add(new Review(id, email, prodId, rating, comment));
            }
        }
        return out;
    }

    /* HELPERS */

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }

    private static String nonNull(String s) {
        return s == null ? "" : s.trim();
    }

    private static String hashPassword(String plain) {
        if (plain == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes());
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /* Seed top-up without reinstall */
    public void ensureSeedProducts(int minCount) {
        int count = 0;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null)) {
            if (c.moveToFirst()) count = c.getInt(0);
        }
        if (count >= minCount) return;

        insertProduct("Wireless Earbuds","Bluetooth 5.3, noise isolation",49.99,"Electronics");
        insertProduct("Gaming Keyboard","Mechanical switches, RGB",79.99,"Electronics");
        insertProduct("Non-Stick Pan","28cm aluminum skillet",24.99,"Kitchen");
        insertProduct("Chef Knife","8-inch stainless steel",34.50,"Kitchen");
        insertProduct("Yoga Mat","6mm thick, non-slip",19.99,"Sportswear");
        insertProduct("Basketball","Official size & weight",17.49,"Sportswear");
        insertProduct("Desk Lamp","LED, touch dimmer, USB port",22.99,"Home");
        insertProduct("Throw Pillow","18x18, soft cover",12.49,"Home");
        insertProduct("Fantasy Novel","Hardcover, 420 pages",14.99,"Books");
        insertProduct("Cookbook","100 easy recipes",18.99,"Books");
    }
}
