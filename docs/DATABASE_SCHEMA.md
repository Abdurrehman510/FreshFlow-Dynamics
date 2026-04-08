# 🗄️ Database Schema: FreshFlow Dynamics

FreshFlow uses a MySQL 8.0 relational database with normalized structures to maintain data integrity and support complex analytics.

## 📐 Entity Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ ORDERS : places
    SUPPLIERS ||--o{ PRODUCTS : supplies
    PRODUCTS ||--o{ ORDER_LINES : contains
    ORDERS ||--o{ ORDER_LINES : contains
    PRODUCTS ||--o{ WASTAGE_RECORDS : logs

    USERS {
        int id PK
        string username
        string password_hash
        enum role
        decimal wallet_balance
    }

    PRODUCTS {
        int id PK
        string name
        decimal base_price
        decimal dynamic_price
        enum category
        date expiry_date
        int stock_quantity
        int supplier_id FK
    }

    ORDERS {
        int id PK
        int user_id FK
        decimal total_amount
        enum status
        datetime placed_at
    }

    WASTAGE_RECORDS {
        int id PK
        int product_id FK
        int units_wasted
        decimal value_wasted
        date recorded_on
    }
```

## 📋 Table Definitions

### `products`
The core table. Note the distinction between `base_price` (admin set) and `dynamic_price` (engine set).
- **Index**: `idx_products_expiry` on `expiry_date` is critical for the nightly repricing job.
- **Search**: `FULLTEXT` index on `name` and `description`.

### `users`
Supports RBAC (Role-Based Access Control) and virtual wallet calculations.
- **Constraints**: `username` is unique.

### `wastage_records`
A snapshot-based table. It stores `product_name` and `value_wasted` at the time of incident to ensure reporting remains valid even if a product is deleted or its price changes.

## 🚀 Migrations
Schema changes are managed via **Flyway**.
- Scripts are located in: `backend/src/main/resources/db/migration`
- Baseline: `V1__initial_schema.sql`
