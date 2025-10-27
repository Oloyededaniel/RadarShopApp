# RadarShop Database Structure

## Overview
The RadarShop Android app now features a comprehensive database structure with **2 main database tables** plus supporting tables for enhanced product management, categorization, and image handling.

## Database Tables

### 1. Categories Table (`categories`)
**Purpose**: Organize products into categories for better searching and navigation.

| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER PRIMARY KEY | Unique category identifier |
| `name` | TEXT UNIQUE NOT NULL | Category name (e.g., "Electronics", "Books") |
| `description` | TEXT | Category description |

**Sample Data**:
- Electronics: Electronic devices and gadgets
- Books: Books and literature  
- Sportswear: Sports and fitness clothing
- Kitchen: Kitchen appliances and accessories
- Home & Garden: Home improvement and garden supplies

### 2. Products Table (`products`) - **Main Database**
**Purpose**: Store comprehensive product information including pricing, stock, ratings, and detailed descriptions.

| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER PRIMARY KEY | Unique product identifier |
| `name` | TEXT NOT NULL | Product name |
| `description` | TEXT | Short product description |
| `detailed_description` | TEXT | Comprehensive product details |
| `price` | REAL NOT NULL | Product price |
| `category_id` | INTEGER | Foreign key to categories table |
| `stock_quantity` | INTEGER DEFAULT 0 | Available stock count |
| `average_rating` | REAL DEFAULT 0.0 | Average customer rating (0-5) |
| `total_reviews` | INTEGER DEFAULT 0 | Total number of reviews |
| `sku` | TEXT UNIQUE | Stock Keeping Unit identifier |
| `brand` | TEXT | Product brand/manufacturer |
| `weight` | REAL | Product weight |
| `dimensions` | TEXT | Product dimensions |
| `created_at` | DATETIME | Product creation timestamp |
| `updated_at` | DATETIME | Last update timestamp |

**Foreign Key**: `category_id` references `categories(id)`

### 3. Product Images Table (`product_images`) - **Supporting Database**
**Purpose**: Store multiple images per product with different types and display order.

| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER PRIMARY KEY | Unique image identifier |
| `product_id` | INTEGER NOT NULL | Foreign key to products table |
| `image_url` | TEXT NOT NULL | URL/path to image file |
| `image_type` | TEXT DEFAULT 'gallery' | Image type: 'main', 'thumbnail', 'gallery' |
| `display_order` | INTEGER DEFAULT 0 | Display order for sorting |

**Foreign Key**: `product_id` references `products(id)` ON DELETE CASCADE

### 4. Supporting Tables (Existing)
- **Users Table**: User account information
- **Wishlist Table**: User's saved products
- **Reviews Table**: Product reviews and ratings

## Key Features

### Enhanced Product Information
- **Price Management**: Real-time pricing with currency formatting
- **Stock Tracking**: Inventory management with stock quantity
- **Rating System**: Average ratings and review counts
- **Detailed Descriptions**: Both short and comprehensive product details
- **Product Specifications**: SKU, brand, weight, dimensions
- **Timestamps**: Creation and update tracking

### Image Management
- **Multiple Images**: Support for multiple images per product
- **Image Types**: Main image, thumbnails, and gallery images
- **Display Order**: Customizable image ordering
- **Cascade Deletion**: Images automatically deleted when product is removed

### Category Organization
- **Hierarchical Structure**: Products organized by categories
- **Search Enhancement**: Category-based filtering and searching
- **Navigation**: Easy category browsing

## Database Methods

### Category Operations
- `getAllCategories()`: Retrieve all categories
- `getCategoryById(int categoryId)`: Get specific category
- `insertCategory(String name, String description)`: Add new category

### Enhanced Product Operations
- `getAllEnhancedProducts()`: Get all products with category information
- `getEnhancedProductById(int productId)`: Get specific product details
- `getProductsByCategory(int categoryId)`: Filter products by category
- `insertEnhancedProduct(...)`: Add new product with full details
- `updateProductStock(int productId, int newStockQuantity)`: Update inventory
- `updateProductRating(int productId, double rating, int reviews)`: Update ratings

### Image Operations
- `getProductImages(int productId)`: Get all images for a product
- `getMainProductImage(int productId)`: Get primary product image
- `insertProductImage(int productId, String url, String type, int order)`: Add image
- `deleteProductImage(int imageId)`: Remove image

## Sample Data

The database includes comprehensive sample data:

### Products with Full Details
1. **Gadget Pro** - Electronics category
   - Price: $199.99, Stock: 25, Rating: 4.2/5 (15 reviews)
   - SKU: GADGET-PRO-001, Brand: TechCorp
   - Weight: 0.5kg, Dimensions: 10x5x2 cm

2. **Smartphone X** - Electronics category  
   - Price: $999.99, Stock: 15, Rating: 4.7/5 (32 reviews)
   - SKU: PHONE-X-001, Brand: MobileTech
   - Weight: 0.2kg, Dimensions: 15x7x0.8 cm

3. **Running Shoes** - Sportswear category
   - Price: $59.99, Stock: 40, Rating: 4.3/5 (22 reviews)
   - SKU: SHOE-RUN-001, Brand: SportMax
   - Weight: 0.8kg, Dimensions: 30x20x10 cm

### Image Examples
- Main product images for each product
- Gallery images for products with multiple views
- Properly ordered display sequence

## Usage Examples

### Getting Products by Category
```java
DatabaseHelper db = new DatabaseHelper(context);
List<Product> electronics = db.getProductsByCategory(1); // Electronics
```

### Getting Product with Images
```java
Product product = db.getEnhancedProductById(1);
List<ProductImage> images = db.getProductImages(1);
ProductImage mainImage = db.getMainProductImage(1);
```

### Updating Stock
```java
boolean success = db.updateProductStock(1, 20); // Update product 1 to 20 in stock
```

## Database Version
- **Current Version**: 5
- **Previous Version**: 4 (upgraded with new structure)
- **Upgrade Behavior**: Drops and recreates all tables with new structure

This enhanced database structure provides a robust foundation for a comprehensive e-commerce application with advanced product management capabilities.
