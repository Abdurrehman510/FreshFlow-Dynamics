# 🏛️ Architecture & Design: FreshFlow Dynamics

FreshFlow is built on **Clean Architecture** and **Domain-Driven Design (DDD)** principles. The goal is a system where business logic is isolated and protected from infrastructure changes.

## 🏗️ The Layered Approach

### 1. Domain Layer (`com.perishable.domain`)
The "Heart" of the system. It contains:
- **Entities**: `Product`, `Order`, `User` (Aggregate Roots).
- **Service Interfaces**: `PricingEngine`, `DemandForecaster`.
- **Value Objects**: `Money`, `ExpiryDate` (Immutable data types).

### 2. Application Layer (`com.perishable.application`)
Orchestrates use cases.
- **Use Cases**: `PlaceOrderUseCase`, `RepriceInventoryUseCase`.
- Handles transaction boundaries and converts between domain and DTO models.

### 3. Infrastructure Layer (`com.perishable.infrastructure`)
Concrete implementations of domain interfaces.
- **Persistence**: `MySqlProductRepository` using Spring Data JPA.
- **Security**: JWT provider and password hashing logic.
- **Scheduling**: The `NightlyPricingScheduler` that triggers batch jobs.

### 4. API / Interface Layer (`com.perishable.api`)
Adapters for external communication.
- **REST Controllers**: Versioned endpoints for UI consumption.

## 🧱 Design Patterns Used

- **Strategy Pattern**: Used in the `ExpiryPricingEngine` to allow for different discounting strategies (Tiered vs. Linear).
- **Repository Pattern**: Abstracts database access, allowing for easy testing with In-Memory implementations.
- **Composite DTOs**: Optimizes network traffic by bundling related entity data for dashboard views.
- **Factory Pattern**: Used for complex aggregate creation (`Product.createNew()`).

## 🔄 Core Data Flows

### The Nightly Repricing Flow
1. **Trigger**: `NightlyPricingScheduler` fires at 02:00 AM.
2. **Fetch**: `RepriceInventoryUseCase` loads all active products.
3. **Logic**: `ExpiryPricingEngine` calculates new prices based on `today - expiry_date`.
4. **Persist**: Batch update committed to MySQL database.
5. **Log**: Repricing activity logged to `perishable-platform.log`.
