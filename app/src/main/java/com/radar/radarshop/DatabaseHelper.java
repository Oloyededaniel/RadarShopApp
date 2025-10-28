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
    private static final int DB_VERSION = 7;

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

    // Categories
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CATEGORY_ID = "id";
    public static final String COL_CATEGORY_NAME = "name";
    public static final String COL_CATEGORY_DESCRIPTION = "description";
    public static final String COL_CATEGORY_IMAGE_URL = "image_url";

    // Products
    public static final String TABLE_PRODUCTS = "products";
    public static final String COL_PRODUCT_ID = "id";
    public static final String COL_PRODUCT_NAME = "name";
    public static final String COL_PRODUCT_DESC = "description";
    public static final String COL_PRODUCT_DETAILED_DESC = "detailed_description";
    public static final String COL_PRODUCT_PRICE = "price";
    public static final String COL_PRODUCT_CATEGORY_ID = "category_id";
    public static final String COL_PRODUCT_STOCK = "stock_quantity";
    public static final String COL_PRODUCT_AVERAGE_RATING = "average_rating";
    public static final String COL_PRODUCT_TOTAL_REVIEWS = "total_reviews";
    public static final String COL_PRODUCT_SKU = "sku";
    public static final String COL_PRODUCT_BRAND = "brand";
    public static final String COL_PRODUCT_WEIGHT = "weight";
    public static final String COL_PRODUCT_DIMENSIONS = "dimensions";
    public static final String COL_PRODUCT_CREATED_AT = "created_at";
    public static final String COL_PRODUCT_UPDATED_AT = "updated_at";

    // Product Images
    public static final String TABLE_PRODUCT_IMAGES = "product_images";
    public static final String COL_IMAGE_ID = "id";
    public static final String COL_IMAGE_PRODUCT_ID = "product_id";
    public static final String COL_IMAGE_URL = "image_url";
    public static final String COL_IMAGE_TYPE = "image_type"; // 'main', 'thumbnail', 'gallery'
    public static final String COL_IMAGE_ORDER = "display_order";

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

        // Categories
        db.execSQL(
                "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                        COL_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_CATEGORY_NAME + " TEXT UNIQUE NOT NULL, " +
                        COL_CATEGORY_DESCRIPTION + " TEXT, " +
                        COL_CATEGORY_IMAGE_URL + " TEXT" +
                        ");"
        );

        // Products
        db.execSQL(
                "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                        COL_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_PRODUCT_NAME + " TEXT NOT NULL, " +
                        COL_PRODUCT_DESC + " TEXT, " +
                        COL_PRODUCT_DETAILED_DESC + " TEXT, " +
                        COL_PRODUCT_PRICE + " REAL NOT NULL, " +
                        COL_PRODUCT_CATEGORY_ID + " INTEGER, " +
                        COL_PRODUCT_STOCK + " INTEGER DEFAULT 0, " +
                        COL_PRODUCT_AVERAGE_RATING + " REAL DEFAULT 0.0, " +
                        COL_PRODUCT_TOTAL_REVIEWS + " INTEGER DEFAULT 0, " +
                        COL_PRODUCT_SKU + " TEXT UNIQUE, " +
                        COL_PRODUCT_BRAND + " TEXT, " +
                        COL_PRODUCT_WEIGHT + " REAL, " +
                        COL_PRODUCT_DIMENSIONS + " TEXT, " +
                        COL_PRODUCT_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        COL_PRODUCT_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(" + COL_PRODUCT_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_CATEGORY_ID + ")" +
                        ");"
        );

        // Product Images
        db.execSQL(
                "CREATE TABLE " + TABLE_PRODUCT_IMAGES + " (" +
                        COL_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_IMAGE_PRODUCT_ID + " INTEGER NOT NULL, " +
                        COL_IMAGE_URL + " TEXT NOT NULL, " +
                        COL_IMAGE_TYPE + " TEXT DEFAULT 'gallery', " +
                        COL_IMAGE_ORDER + " INTEGER DEFAULT 0, " +
                        "FOREIGN KEY(" + COL_IMAGE_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COL_PRODUCT_ID + ") ON DELETE CASCADE" +
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

        // Seed categories
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Electronics','Electronic devices and gadgets','https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Books','Books and literature','https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Sportswear','Sports and fitness clothing','https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Kitchen','Kitchen appliances and accessories','https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Home & Garden','Home improvement and garden supplies','https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Home','Home decor and furniture','https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Appliances','Home appliances and electronics','https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Cooking','Cooking tools and utensils','https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Fashion','Clothing and accessories','https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Beauty','Beauty and personal care products','https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Toys','Toys and games for all ages','https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=400&fit=crop');");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" +
                COL_CATEGORY_NAME + "," + COL_CATEGORY_DESCRIPTION + "," + COL_CATEGORY_IMAGE_URL +
                ") VALUES ('Automotive','Car accessories and parts','https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=400&fit=crop');");

        // Seed products with comprehensive data
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Gadget Pro','A high-tech gadget','Advanced multi-functional gadget with cutting-edge technology, perfect for tech enthusiasts. Features include wireless connectivity, long battery life, and sleek design.',199.99,1,25,4.2,15,'GADGET-PRO-001','TechCorp',0.5,'10x5x2 cm');");
        
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Novel Book','An interesting novel','A captivating novel that takes readers on an unforgettable journey through mystery and adventure. Perfect for book lovers and literature enthusiasts.',9.99,2,50,4.5,8,'BOOK-NOVEL-001','Literary Press',0.3,'15x10x2 cm');");
        
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Smartphone X','Latest smartphone model','The newest flagship smartphone with revolutionary features including advanced camera system, 5G connectivity, and all-day battery life. Premium build quality and stunning display.',999.99,1,15,4.7,32,'PHONE-X-001','MobileTech',0.2,'15x7x0.8 cm');");
        
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Running Shoes','Comfortable running shoes','High-performance running shoes designed for comfort and durability. Features advanced cushioning technology, breathable materials, and excellent traction for all running conditions.',59.99,3,40,4.3,22,'SHOE-RUN-001','SportMax',0.8,'30x20x10 cm');");
        
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Coffee Mug','Ceramic coffee mug','Beautiful ceramic coffee mug perfect for your morning brew. Microwave and dishwasher safe with ergonomic handle design for comfortable grip.',12.99,4,100,4.1,18,'MUG-COFFEE-001','HomeStyle',0.3,'10x8x8 cm');");
        
        // Additional products for new categories
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Modern Sofa','Comfortable 3-seater sofa','Elegant modern sofa with premium fabric upholstery. Features comfortable seating for 3 people with removable cushions and sturdy wooden frame.',899.99,6,15,4.6,32,'SOFA-MODERN-001','FurnitureCo',45.0,'200x90x85 cm');");
        
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Smart TV','55-inch 4K Smart TV','Ultra HD 4K Smart TV with built-in streaming apps. Features HDR support, voice control, and multiple connectivity options.',699.99,7,20,4.4,28,'TV-SMART-001','TechVision',18.5,'123x71x8 cm');");
        
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Chef Knife Set','Professional 8-piece knife set','High-quality stainless steel chef knife set with wooden block. Includes chef knife, bread knife, paring knife, and utility knives.',89.99,8,35,4.7,45,'KNIFE-SET-001','ChefPro',2.1,'40x15x8 cm');");
        
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Designer Dress','Elegant evening dress','Beautiful designer evening dress made from premium silk blend. Features elegant cut, comfortable fit, and timeless design.',149.99,9,25,4.3,18,'DRESS-EVENING-001','FashionHouse',0.4,'Size M');");
        
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Skincare Set','Complete skincare routine','Premium skincare set including cleanser, toner, serum, and moisturizer. Made with natural ingredients for all skin types.',79.99,10,40,4.5,67,'SKINCARE-SET-001','BeautyCare',0.8,'Various sizes');");
        
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('LEGO Building Set','Creative construction toy','Educational LEGO building set with 500+ pieces. Encourages creativity and problem-solving skills for children and adults.',49.99,11,60,4.8,89,'LEGO-CREATIVE-001','LEGO',1.2,'30x20x10 cm');");
        
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_NAME + "," + COL_PRODUCT_DESC + "," + COL_PRODUCT_DETAILED_DESC + "," +
                COL_PRODUCT_PRICE + "," + COL_PRODUCT_CATEGORY_ID + "," + COL_PRODUCT_STOCK + "," +
                COL_PRODUCT_AVERAGE_RATING + "," + COL_PRODUCT_TOTAL_REVIEWS + "," + COL_PRODUCT_SKU + "," +
                COL_PRODUCT_BRAND + "," + COL_PRODUCT_WEIGHT + "," + COL_PRODUCT_DIMENSIONS +
                ") VALUES ('Car Phone Mount','Universal dashboard mount','Universal car phone mount with strong suction cup and adjustable arm. Compatible with all smartphone sizes and models.',24.99,12,80,4.2,156,'MOUNT-CAR-001','AutoTech',0.3,'15x10x5 cm');");

        // Seed product images with actual image URLs
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (1,'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=400&h=400&fit=crop','main',1);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (1,'https://images.unsplash.com/photo-1518717752-7c84d45d49d5?w=400&h=400&fit=crop','gallery',2);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (2,'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&h=400&fit=crop','main',1);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (3,'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop','main',1);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (3,'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop','gallery',2);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (4,'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop','main',1);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (5,'https://images.unsplash.com/photo-1514228742587-6b1558fcf93a?w=400&h=400&fit=crop','main',1);");
        
        // Images for new products (6-12)
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (6,'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=400&h=400&fit=crop','main',1);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (7,'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=400&h=400&fit=crop','main',1);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (8,'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop','main',1);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (9,'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=400&h=400&fit=crop','main',1);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (10,'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=400&h=400&fit=crop','main',1);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (11,'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=400&fit=crop','main',1);");
        db.execSQL("INSERT INTO " + TABLE_PRODUCT_IMAGES + " (" +
                COL_IMAGE_PRODUCT_ID + "," + COL_IMAGE_URL + "," + COL_IMAGE_TYPE + "," + COL_IMAGE_ORDER +
                ") VALUES (12,'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=400&fit=crop','main',1);");

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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT_IMAGES);
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
        // Find category ID by name
        Category categoryObj = getCategoryByName(category);
        int categoryId = categoryObj != null ? categoryObj.getId() : 1; // Default to Electronics if not found
        
        ContentValues cv = new ContentValues();
        cv.put(COL_PRODUCT_NAME, name);
        cv.put(COL_PRODUCT_DESC, desc);
        cv.put(COL_PRODUCT_PRICE, price);
        cv.put(COL_PRODUCT_CATEGORY_ID, categoryId);
        cv.put(COL_PRODUCT_STOCK, 0); // Default stock
        cv.put(COL_PRODUCT_CREATED_AT, "CURRENT_TIMESTAMP");
        cv.put(COL_PRODUCT_UPDATED_AT, "CURRENT_TIMESTAMP");
        return getWritableDatabase().insert(TABLE_PRODUCTS, null, cv);
    }

    public List<Product> getAllProducts() {
        return getAllEnhancedProducts();
    }

    public List<Product> getProducts(String nameFilter, String categoryFilter, Double minPrice, Double maxPrice) {
        ArrayList<Product> list = new ArrayList<>();
        StringBuilder where = new StringBuilder();
        ArrayList<String> args = new ArrayList<>();

        if (nameFilter != null && !nameFilter.isEmpty()) {
            where.append(COL_PRODUCT_NAME).append(" LIKE ?");
            args.add("%" + nameFilter + "%");
        }
        if (categoryFilter != null && !categoryFilter.equals("All")) {
            if (where.length() > 0) where.append(" AND ");
            // Find category ID by name
            Category category = getCategoryByName(categoryFilter);
            if (category != null) {
                where.append(COL_PRODUCT_CATEGORY_ID).append("=?");
                args.add(String.valueOf(category.getId()));
            }
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

        // Use enhanced product query with category join
        String query = "SELECT p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_DESC + 
                      ", p." + COL_PRODUCT_DETAILED_DESC + ", p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_CATEGORY_ID +
                      ", c." + COL_CATEGORY_NAME + ", p." + COL_PRODUCT_STOCK + ", p." + COL_PRODUCT_AVERAGE_RATING +
                      ", p." + COL_PRODUCT_TOTAL_REVIEWS + ", p." + COL_PRODUCT_SKU + ", p." + COL_PRODUCT_BRAND +
                      ", p." + COL_PRODUCT_WEIGHT + ", p." + COL_PRODUCT_DIMENSIONS + ", p." + COL_PRODUCT_CREATED_AT +
                      ", p." + COL_PRODUCT_UPDATED_AT +
                      " FROM " + TABLE_PRODUCTS + " p LEFT JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PRODUCT_CATEGORY_ID + " = c." + COL_CATEGORY_ID;
        
        if (where.length() > 0) {
            query += " WHERE " + where.toString();
        }
        
        query += " ORDER BY p." + COL_PRODUCT_NAME + " ASC";

        try (Cursor c = getReadableDatabase().rawQuery(query, args.isEmpty() ? null : args.toArray(new String[0]))) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                String detailedDesc = c.getString(3);
                double price = c.getDouble(4);
                int categoryId = c.getInt(5);
                String categoryName = c.getString(6);
                int stock = c.getInt(7);
                double rating = c.getDouble(8);
                int reviews = c.getInt(9);
                String sku = c.getString(10);
                String brand = c.getString(11);
                double weight = c.getDouble(12);
                String dimensions = c.getString(13);
                String createdAt = c.getString(14);
                String updatedAt = c.getString(15);
                
                list.add(new Product(id, name, desc, detailedDesc, price, categoryId, categoryName,
                                   stock, rating, reviews, sku, brand, weight, dimensions, createdAt, updatedAt));
            }
        }
        return list;
    }

    public List<Product> getFilteredProducts(String category, String nameQuery, double minPrice, double maxPrice) {
        String catFilter = (category == null || category.equalsIgnoreCase("All")) ? null : category;
        String nameFilter = (nameQuery == null || nameQuery.isEmpty()) ? null : nameQuery;
        return getProducts(nameFilter, catFilter, minPrice, maxPrice);
    }

    public List<Product> searchProducts(String searchQuery) {
        ArrayList<Product> list = new ArrayList<>();
        
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return list;
        }
        
        String query = "SELECT p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_DESC + 
                      ", p." + COL_PRODUCT_DETAILED_DESC + ", p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_CATEGORY_ID +
                      ", c." + COL_CATEGORY_NAME + ", p." + COL_PRODUCT_STOCK + ", p." + COL_PRODUCT_AVERAGE_RATING +
                      ", p." + COL_PRODUCT_TOTAL_REVIEWS + ", p." + COL_PRODUCT_SKU + ", p." + COL_PRODUCT_BRAND +
                      ", p." + COL_PRODUCT_WEIGHT + ", p." + COL_PRODUCT_DIMENSIONS + ", p." + COL_PRODUCT_CREATED_AT +
                      ", p." + COL_PRODUCT_UPDATED_AT +
                      " FROM " + TABLE_PRODUCTS + " p LEFT JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PRODUCT_CATEGORY_ID + " = c." + COL_CATEGORY_ID +
                      " WHERE p." + COL_PRODUCT_NAME + " LIKE ? OR p." + COL_PRODUCT_DESC + " LIKE ? OR p." + COL_PRODUCT_BRAND + " LIKE ? OR c." + COL_CATEGORY_NAME + " LIKE ?" +
                      " ORDER BY p." + COL_PRODUCT_NAME + " ASC";
        
        String searchPattern = "%" + searchQuery.toLowerCase() + "%";
        
        try (Cursor c = getReadableDatabase().rawQuery(query, new String[]{searchPattern, searchPattern, searchPattern, searchPattern})) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                String detailedDesc = c.getString(3);
                double price = c.getDouble(4);
                int categoryId = c.getInt(5);
                String categoryName = c.getString(6);
                int stock = c.getInt(7);
                double rating = c.getDouble(8);
                int reviews = c.getInt(9);
                String sku = c.getString(10);
                String brand = c.getString(11);
                double weight = c.getDouble(12);
                String dimensions = c.getString(13);
                String createdAt = c.getString(14);
                String updatedAt = c.getString(15);
                
                list.add(new Product(id, name, desc, detailedDesc, price, categoryId, categoryName,
                                   stock, rating, reviews, sku, brand, weight, dimensions, createdAt, updatedAt));
            }
        }
        return list;
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

    public boolean isInWishlist(String userEmail, int productId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_WISHLIST +
                " WHERE " + COL_WISH_EMAIL + "=? AND " + COL_WISH_PRODUCT_ID + "=?";
        
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{userEmail, String.valueOf(productId)})) {
            if (c.moveToFirst()) {
                return c.getInt(0) > 0;
            }
        }
        return false;
    }

    public List<Product> getWishlist(String userEmail) {
        ArrayList<Product> list = new ArrayList<>();
        String sql =
                "SELECT p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_DESC + 
                ", p." + COL_PRODUCT_DETAILED_DESC + ", p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_CATEGORY_ID +
                ", c." + COL_CATEGORY_NAME + ", p." + COL_PRODUCT_STOCK + ", p." + COL_PRODUCT_AVERAGE_RATING +
                ", p." + COL_PRODUCT_TOTAL_REVIEWS + ", p." + COL_PRODUCT_SKU + ", p." + COL_PRODUCT_BRAND +
                ", p." + COL_PRODUCT_WEIGHT + ", p." + COL_PRODUCT_DIMENSIONS + ", p." + COL_PRODUCT_CREATED_AT +
                ", p." + COL_PRODUCT_UPDATED_AT +
                " FROM " + TABLE_PRODUCTS + " p " +
                " LEFT JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PRODUCT_CATEGORY_ID + " = c." + COL_CATEGORY_ID +
                " JOIN " + TABLE_WISHLIST + " w ON p." + COL_PRODUCT_ID + " = w." + COL_WISH_PRODUCT_ID +
                " WHERE w." + COL_WISH_EMAIL + " = ?";

        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{userEmail})) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                String detailedDesc = c.getString(3);
                double price = c.getDouble(4);
                int categoryId = c.getInt(5);
                String categoryName = c.getString(6);
                int stock = c.getInt(7);
                double rating = c.getDouble(8);
                int reviews = c.getInt(9);
                String sku = c.getString(10);
                String brand = c.getString(11);
                double weight = c.getDouble(12);
                String dimensions = c.getString(13);
                String createdAt = c.getString(14);
                String updatedAt = c.getString(15);
                
                list.add(new Product(id, name, desc, detailedDesc, price, categoryId, categoryName,
                                   stock, rating, reviews, sku, brand, weight, dimensions, createdAt, updatedAt));
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

    public boolean updateCartQuantity(String userEmail, int productId, int newQuantity) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CART_QUANTITY, newQuantity);
        
        int rows = getWritableDatabase().update(TABLE_CART, cv,
                COL_CART_EMAIL + "=? AND " + COL_CART_PRODUCT_ID + "=?",
                new String[]{userEmail, String.valueOf(productId)});
        return rows > 0;
    }

    public int getCartQuantity(String userEmail, int productId) {
        String sql = "SELECT " + COL_CART_QUANTITY + " FROM " + TABLE_CART +
                " WHERE " + COL_CART_EMAIL + "=? AND " + COL_CART_PRODUCT_ID + "=?";
        
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{userEmail, String.valueOf(productId)})) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    public int getCartItemCount(String userEmail) {
        String sql = "SELECT SUM(" + COL_CART_QUANTITY + ") FROM " + TABLE_CART +
                " WHERE " + COL_CART_EMAIL + "=?";
        
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{userEmail})) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    public boolean isInCart(String userEmail, int productId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_CART +
                " WHERE " + COL_CART_EMAIL + "=? AND " + COL_CART_PRODUCT_ID + "=?";
        
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{userEmail, String.valueOf(productId)})) {
            if (c.moveToFirst()) {
                return c.getInt(0) > 0;
            }
        }
        return false;
    }

    // TODO: add quantity to returned list
    public List<Product> getCart(String userEmail) {
        ArrayList<Product> list = new ArrayList<>();
        String sql =
                "SELECT p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_DESC + ", " +
                        "p." + COL_PRODUCT_DETAILED_DESC + ", p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_CATEGORY_ID + ", " +
                        "p." + COL_PRODUCT_STOCK + ", p." + COL_PRODUCT_AVERAGE_RATING + ", p." + COL_PRODUCT_TOTAL_REVIEWS + ", " +
                        "p." + COL_PRODUCT_SKU + ", p." + COL_PRODUCT_BRAND + ", p." + COL_PRODUCT_WEIGHT + ", " +
                        "p." + COL_PRODUCT_DIMENSIONS + ", p." + COL_PRODUCT_CREATED_AT + ", p." + COL_PRODUCT_UPDATED_AT + ", " +
                        "c." + COL_CATEGORY_NAME +
                        " FROM " + TABLE_PRODUCTS + " p " +
                        " JOIN " + TABLE_CART + " w ON p." + COL_PRODUCT_ID + " = w." + COL_CART_PRODUCT_ID +
                        " JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PRODUCT_CATEGORY_ID + " = c." + COL_CATEGORY_ID +
                        " WHERE w." + COL_CART_EMAIL + " = ?";

        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{userEmail})) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                String detailedDesc = c.getString(3);
                double price = c.getDouble(4);
                int categoryId = c.getInt(5);
                int stock = c.getInt(6);
                double rating = c.getDouble(7);
                int reviews = c.getInt(8);
                String sku = c.getString(9);
                String brand = c.getString(10);
                double weight = c.getDouble(11);
                String dimensions = c.getString(12);
                String createdAt = c.getString(13);
                String updatedAt = c.getString(14);
                String categoryName = c.getString(15);
                
                list.add(new Product(id, name, desc, detailedDesc, price, categoryId, categoryName, 
                                   stock, rating, reviews, sku, brand, weight, dimensions, createdAt, updatedAt));
            }
        }
        return list;
    }

    public List<CartItem> getCartItems(String userEmail) {
        ArrayList<CartItem> list = new ArrayList<>();
        String sql =
                "SELECT p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_DESC + ", " +
                        "p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_BRAND + ", " +
                        "c." + COL_CATEGORY_NAME + ", cart." + COL_CART_QUANTITY +
                        " FROM " + TABLE_PRODUCTS + " p " +
                        " JOIN " + TABLE_CART + " cart ON p." + COL_PRODUCT_ID + " = cart." + COL_CART_PRODUCT_ID +
                        " JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PRODUCT_CATEGORY_ID + " = c." + COL_CATEGORY_ID +
                        " WHERE cart." + COL_CART_EMAIL + " = ?";

        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{userEmail})) {
            while (c.moveToNext()) {
                int productId = c.getInt(0);
                String productName = c.getString(1);
                String productDesc = c.getString(2);
                double price = c.getDouble(3);
                String brand = c.getString(4);
                String category = c.getString(5);
                int quantity = c.getInt(6);
                
                // For now, use placeholder image
                String productImage = "";
                
                list.add(new CartItem(productId, productName, productImage, price, quantity, brand, category));
            }
        }
        return list;
    }

    public boolean updateCartItemQuantity(String userEmail, int productId, int newQuantity) {
        return updateCartQuantity(userEmail, productId, newQuantity);
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

    /* CATEGORIES */

    public List<Category> getAllCategories() {
        ArrayList<Category> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE_CATEGORIES,
                new String[]{COL_CATEGORY_ID, COL_CATEGORY_NAME, COL_CATEGORY_DESCRIPTION, COL_CATEGORY_IMAGE_URL},
                null, null, null, null, COL_CATEGORY_NAME + " ASC")) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String description = c.getString(2);
                String imageUrl = c.getString(3);
                list.add(new Category(id, name, description, imageUrl));
            }
        }
        return list;
    }

    public Category getCategoryById(int categoryId) {
        try (Cursor c = getReadableDatabase().query(
                TABLE_CATEGORIES,
                new String[]{COL_CATEGORY_ID, COL_CATEGORY_NAME, COL_CATEGORY_DESCRIPTION},
                COL_CATEGORY_ID + "=?", new String[]{String.valueOf(categoryId)},
                null, null, null)) {
            if (c.moveToFirst()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String description = c.getString(2);
                return new Category(id, name, description);
            }
        }
        return null;
    }

    public Category getCategoryByName(String categoryName) {
        try (Cursor c = getReadableDatabase().query(
                TABLE_CATEGORIES,
                new String[]{COL_CATEGORY_ID, COL_CATEGORY_NAME, COL_CATEGORY_DESCRIPTION},
                COL_CATEGORY_NAME + "=?", new String[]{categoryName},
                null, null, null)) {
            if (c.moveToFirst()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String description = c.getString(2);
                return new Category(id, name, description);
            }
        }
        return null;
    }

    public long insertCategory(String name, String description) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CATEGORY_NAME, name);
        cv.put(COL_CATEGORY_DESCRIPTION, description);
        return getWritableDatabase().insert(TABLE_CATEGORIES, null, cv);
    }

    /* ENHANCED PRODUCTS */

    public long insertEnhancedProduct(String name, String description, String detailedDescription,
                                     double price, int categoryId, int stockQuantity, String sku,
                                     String brand, double weight, String dimensions) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PRODUCT_NAME, name);
        cv.put(COL_PRODUCT_DESC, description);
        cv.put(COL_PRODUCT_DETAILED_DESC, detailedDescription);
        cv.put(COL_PRODUCT_PRICE, price);
        cv.put(COL_PRODUCT_CATEGORY_ID, categoryId);
        cv.put(COL_PRODUCT_STOCK, stockQuantity);
        cv.put(COL_PRODUCT_SKU, sku);
        cv.put(COL_PRODUCT_BRAND, brand);
        cv.put(COL_PRODUCT_WEIGHT, weight);
        cv.put(COL_PRODUCT_DIMENSIONS, dimensions);
        cv.put(COL_PRODUCT_CREATED_AT, "CURRENT_TIMESTAMP");
        cv.put(COL_PRODUCT_UPDATED_AT, "CURRENT_TIMESTAMP");
        return getWritableDatabase().insert(TABLE_PRODUCTS, null, cv);
    }

    public List<Product> getAllEnhancedProducts() {
        ArrayList<Product> list = new ArrayList<>();
        String query = "SELECT p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_DESC + 
                      ", p." + COL_PRODUCT_DETAILED_DESC + ", p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_CATEGORY_ID +
                      ", c." + COL_CATEGORY_NAME + ", p." + COL_PRODUCT_STOCK + ", p." + COL_PRODUCT_AVERAGE_RATING +
                      ", p." + COL_PRODUCT_TOTAL_REVIEWS + ", p." + COL_PRODUCT_SKU + ", p." + COL_PRODUCT_BRAND +
                      ", p." + COL_PRODUCT_WEIGHT + ", p." + COL_PRODUCT_DIMENSIONS + ", p." + COL_PRODUCT_CREATED_AT +
                      ", p." + COL_PRODUCT_UPDATED_AT +
                      " FROM " + TABLE_PRODUCTS + " p LEFT JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PRODUCT_CATEGORY_ID + " = c." + COL_CATEGORY_ID +
                      " ORDER BY p." + COL_PRODUCT_NAME + " ASC";
        
        try (Cursor c = getReadableDatabase().rawQuery(query, null)) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                String detailedDesc = c.getString(3);
                double price = c.getDouble(4);
                int categoryId = c.getInt(5);
                String categoryName = c.getString(6);
                int stock = c.getInt(7);
                double rating = c.getDouble(8);
                int reviews = c.getInt(9);
                String sku = c.getString(10);
                String brand = c.getString(11);
                double weight = c.getDouble(12);
                String dimensions = c.getString(13);
                String createdAt = c.getString(14);
                String updatedAt = c.getString(15);
                
                list.add(new Product(id, name, desc, detailedDesc, price, categoryId, categoryName,
                                   stock, rating, reviews, sku, brand, weight, dimensions, createdAt, updatedAt));
            }
        }
        return list;
    }

    public Product getEnhancedProductById(int productId) {
        String query = "SELECT p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_DESC + 
                      ", p." + COL_PRODUCT_DETAILED_DESC + ", p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_CATEGORY_ID +
                      ", c." + COL_CATEGORY_NAME + ", p." + COL_PRODUCT_STOCK + ", p." + COL_PRODUCT_AVERAGE_RATING +
                      ", p." + COL_PRODUCT_TOTAL_REVIEWS + ", p." + COL_PRODUCT_SKU + ", p." + COL_PRODUCT_BRAND +
                      ", p." + COL_PRODUCT_WEIGHT + ", p." + COL_PRODUCT_DIMENSIONS + ", p." + COL_PRODUCT_CREATED_AT +
                      ", p." + COL_PRODUCT_UPDATED_AT +
                      " FROM " + TABLE_PRODUCTS + " p LEFT JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PRODUCT_CATEGORY_ID + " = c." + COL_CATEGORY_ID +
                      " WHERE p." + COL_PRODUCT_ID + " = ?";
        
        try (Cursor c = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(productId)})) {
            if (c.moveToFirst()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                String detailedDesc = c.getString(3);
                double price = c.getDouble(4);
                int categoryId = c.getInt(5);
                String categoryName = c.getString(6);
                int stock = c.getInt(7);
                double rating = c.getDouble(8);
                int reviews = c.getInt(9);
                String sku = c.getString(10);
                String brand = c.getString(11);
                double weight = c.getDouble(12);
                String dimensions = c.getString(13);
                String createdAt = c.getString(14);
                String updatedAt = c.getString(15);
                
                return new Product(id, name, desc, detailedDesc, price, categoryId, categoryName,
                                 stock, rating, reviews, sku, brand, weight, dimensions, createdAt, updatedAt);
            }
        }
        return null;
    }

    public List<Product> getProductsByCategory(int categoryId) {
        ArrayList<Product> list = new ArrayList<>();
        String query = "SELECT p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_DESC + 
                      ", p." + COL_PRODUCT_DETAILED_DESC + ", p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_CATEGORY_ID +
                      ", c." + COL_CATEGORY_NAME + ", p." + COL_PRODUCT_STOCK + ", p." + COL_PRODUCT_AVERAGE_RATING +
                      ", p." + COL_PRODUCT_TOTAL_REVIEWS + ", p." + COL_PRODUCT_SKU + ", p." + COL_PRODUCT_BRAND +
                      ", p." + COL_PRODUCT_WEIGHT + ", p." + COL_PRODUCT_DIMENSIONS + ", p." + COL_PRODUCT_CREATED_AT +
                      ", p." + COL_PRODUCT_UPDATED_AT +
                      " FROM " + TABLE_PRODUCTS + " p LEFT JOIN " + TABLE_CATEGORIES + " c ON p." + COL_PRODUCT_CATEGORY_ID + " = c." + COL_CATEGORY_ID +
                      " WHERE p." + COL_PRODUCT_CATEGORY_ID + " = ? ORDER BY p." + COL_PRODUCT_NAME + " ASC";
        
        try (Cursor c = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(categoryId)})) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String desc = c.getString(2);
                String detailedDesc = c.getString(3);
                double price = c.getDouble(4);
                int catId = c.getInt(5);
                String categoryName = c.getString(6);
                int stock = c.getInt(7);
                double rating = c.getDouble(8);
                int reviews = c.getInt(9);
                String sku = c.getString(10);
                String brand = c.getString(11);
                double weight = c.getDouble(12);
                String dimensions = c.getString(13);
                String createdAt = c.getString(14);
                String updatedAt = c.getString(15);
                
                list.add(new Product(id, name, desc, detailedDesc, price, catId, categoryName,
                                   stock, rating, reviews, sku, brand, weight, dimensions, createdAt, updatedAt));
            }
        }
        return list;
    }

    public boolean updateProductStock(int productId, int newStockQuantity) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PRODUCT_STOCK, newStockQuantity);
        cv.put(COL_PRODUCT_UPDATED_AT, "CURRENT_TIMESTAMP");
        
        int rows = getWritableDatabase().update(TABLE_PRODUCTS, cv, 
                                               COL_PRODUCT_ID + "=?", 
                                               new String[]{String.valueOf(productId)});
        return rows > 0;
    }

    public boolean updateProductRating(int productId, double newAverageRating, int newTotalReviews) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PRODUCT_AVERAGE_RATING, newAverageRating);
        cv.put(COL_PRODUCT_TOTAL_REVIEWS, newTotalReviews);
        cv.put(COL_PRODUCT_UPDATED_AT, "CURRENT_TIMESTAMP");
        
        int rows = getWritableDatabase().update(TABLE_PRODUCTS, cv, 
                                               COL_PRODUCT_ID + "=?", 
                                               new String[]{String.valueOf(productId)});
        return rows > 0;
    }

    /* PRODUCT IMAGES */

    public long insertProductImage(int productId, String imageUrl, String imageType, int displayOrder) {
        ContentValues cv = new ContentValues();
        cv.put(COL_IMAGE_PRODUCT_ID, productId);
        cv.put(COL_IMAGE_URL, imageUrl);
        cv.put(COL_IMAGE_TYPE, imageType);
        cv.put(COL_IMAGE_ORDER, displayOrder);
        return getWritableDatabase().insert(TABLE_PRODUCT_IMAGES, null, cv);
    }

    public List<ProductImage> getProductImages(int productId) {
        ArrayList<ProductImage> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE_PRODUCT_IMAGES,
                new String[]{COL_IMAGE_ID, COL_IMAGE_PRODUCT_ID, COL_IMAGE_URL, COL_IMAGE_TYPE, COL_IMAGE_ORDER},
                COL_IMAGE_PRODUCT_ID + "=?", new String[]{String.valueOf(productId)},
                null, null, COL_IMAGE_ORDER + " ASC")) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                int prodId = c.getInt(1);
                String url = c.getString(2);
                String type = c.getString(3);
                int order = c.getInt(4);
                list.add(new ProductImage(id, prodId, url, type, order));
            }
        }
        return list;
    }

    public ProductImage getMainProductImage(int productId) {
        try (Cursor c = getReadableDatabase().query(
                TABLE_PRODUCT_IMAGES,
                new String[]{COL_IMAGE_ID, COL_IMAGE_PRODUCT_ID, COL_IMAGE_URL, COL_IMAGE_TYPE, COL_IMAGE_ORDER},
                COL_IMAGE_PRODUCT_ID + "=? AND " + COL_IMAGE_TYPE + "=?",
                new String[]{String.valueOf(productId), "main"},
                null, null, null)) {
            if (c.moveToFirst()) {
                int id = c.getInt(0);
                int prodId = c.getInt(1);
                String url = c.getString(2);
                String type = c.getString(3);
                int order = c.getInt(4);
                return new ProductImage(id, prodId, url, type, order);
            }
        }
        return null;
    }

    public boolean deleteProductImage(int imageId) {
        int rows = getWritableDatabase().delete(TABLE_PRODUCT_IMAGES, 
                                               COL_IMAGE_ID + "=?", 
                                               new String[]{String.valueOf(imageId)});
        return rows > 0;
    }

    /* IMAGE HELPER METHODS */

    /**
     * Checks if the image URL is a local drawable resource
     * @param imageUrl Image URL from database
     * @return true if it's a local drawable resource
     */
    public static boolean isLocalDrawable(String imageUrl) {
        return imageUrl != null && imageUrl.startsWith("@drawable/");
    }

    /**
     * Converts drawable resource name to resource ID
     * @param context Application context
     * @param drawableName Name of the drawable resource (e.g., "@drawable/gadget_pro_main")
     * @return Resource ID or 0 if not found
     */
    public static int getDrawableResourceId(Context context, String drawableName) {
        if (drawableName == null || !drawableName.startsWith("@drawable/")) {
            return 0;
        }
        
        String resourceName = drawableName.substring(10); // Remove "@drawable/"
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }

    /**
     * Gets drawable resource ID for a product image (if it's a local resource)
     * @param context Application context
     * @param imageUrl Image URL from database
     * @return Resource ID or 0 if not found or not a local resource
     */
    public static int getImageResourceId(Context context, String imageUrl) {
        if (isLocalDrawable(imageUrl)) {
            return getDrawableResourceId(context, imageUrl);
        }
        return 0; // Not a local resource
    }

    /* Seed top-up without reinstall */
    public void ensureSeedProducts(int minCount) {
        int count = 0;
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null)) {
            if (c.moveToFirst()) count = c.getInt(0);
        }
        if (count >= minCount) return;

        // Insert additional products with enhanced data
        long earbudsId = insertEnhancedProduct("Wireless Earbuds", "Bluetooth 5.3, noise isolation", 
            "Premium wireless earbuds with advanced noise cancellation technology. Features Bluetooth 5.3 connectivity, 8-hour battery life, and comfortable fit.", 
            49.99, 1, 30, "EARBUDS-WIRELESS-001", "AudioTech", 0.1, "6x4x3 cm");
        
        long keyboardId = insertEnhancedProduct("Gaming Keyboard", "Mechanical switches, RGB", 
            "High-performance gaming keyboard with mechanical switches and customizable RGB lighting. Features anti-ghosting technology and programmable keys.", 
            79.99, 1, 20, "KEYBOARD-GAMING-001", "GameGear", 1.2, "45x15x3 cm");
        
        long panId = insertEnhancedProduct("Non-Stick Pan", "28cm aluminum skillet", 
            "Professional-grade non-stick aluminum skillet perfect for cooking. Features even heat distribution and easy cleaning.", 
            24.99, 4, 50, "PAN-NONSTICK-001", "CookPro", 0.8, "28cm diameter");
        
        long yogaId = insertEnhancedProduct("Yoga Mat", "6mm thick, non-slip", 
            "Premium yoga mat with excellent grip and cushioning. Made from eco-friendly materials with non-slip surface.", 
            19.99, 3, 25, "MAT-YOGA-001", "FitLife", 1.5, "180x60x0.6 cm");

        // Insert images for additional products
        insertProductImage((int)earbudsId, "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400&h=400&fit=crop", "main", 1);
        insertProductImage((int)keyboardId, "https://images.unsplash.com/photo-1541140532154-b024d705b90a?w=400&h=400&fit=crop", "main", 1);
        insertProductImage((int)panId, "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop", "main", 1);
        insertProductImage((int)yogaId, "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=400&fit=crop", "main", 1);

        // Insert remaining products using the old method for compatibility
        insertProduct("Chef Knife","8-inch stainless steel",34.50,"Kitchen");
        insertProduct("Basketball","Official size & weight",17.49,"Sportswear");
        insertProduct("Desk Lamp","LED, touch dimmer, USB port",22.99,"Home");
        insertProduct("Throw Pillow","18x18, soft cover",12.49,"Home");
        insertProduct("Fantasy Novel","Hardcover, 420 pages",14.99,"Books");
        insertProduct("Cookbook","100 easy recipes",18.99,"Books");
    }
}
