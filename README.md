# FreshFlow Dynamics: Intelligent Perishable Goods Management

[![Stack: Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Stack: React](https://img.shields.io/badge/Frontend-React%2018-61DAFB?logo=react&logoColor=white)](https://react.dev/)
[![Arch: Clean Architecture](https://img.shields.io/badge/Architecture-Clean%20/%20DDD-blue)](#architecture)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**FreshFlow Dynamics** is an enterprise-grade full-stack platform designed to eliminate wastage in the perishable goods supply chain. By integrating real-time expiry tracking with a proprietary **Dynamic Pricing Engine**, the system automatically adjusts product prices to maximize sell-through before products expire.

## 🚀 Key Features

- **Dynamic Pricing Engine**: Automated nightly repricing based on shelf-life and proximity to expiry.
- **Expiry Tracking & Analytics**: Granular monitoring of product life cycles from procurement to sale.
- **Wastage Management**: Automated logging of expired products with detailed loss analytics.
- **Enterprise dashboard**: High-fidelity React dashboard featuring Recharts for demand forecasting and inventory metrics.
- **Secure Authentication**: Robust JWT-based security with BCrypt password hashing.

## 🛠️ Tech Stack

### Backend
- **Java 17** & **Spring Boot 3.2**
- **Spring Data JPA** (MySQL Persistence)
- **Spring Security** (JWT & CORS)
- **Flyway** (Database Migrations)
- **Lombok** (Boilerplate reduction)

### Frontend
- **React 18** (Vite build tool)
- **Tailwind CSS** (Modern UI Styling)
- **Recharts** (Data Visualization)
- **Zustand** (Global State Management)
- **Axios** (API Communication)

## 🏗️ Architecture

The project follows **Domain-Driven Design (DDD)** and **Clean Architecture** principles to ensure maintainability and scalability:
- **Domain Layer**: Contains core business logic and entities (Product, Order, Wastage).
- **Application Layer**: Use cases and business services.
- **Infrastructure Layer**: Persistence (Spring Data JPA) and Security configurations.
- **API Layer**: REST Controllers exposing the platform capabilities.

## 🚦 Getting Started

### Prerequisites
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### Database Setup
1. Create a MySQL database named `perishable_platform`.
2. Configure your credentials in `backend/src/main/resources/application.properties` or via environment variables:
   ```env
   DB_PASSWORD=your_password
   ```

### Running the Backend
```bash
cd backend
mvn spring-boot:run
```

### Running the Frontend
```bash
cd frontend
npm install
npm run dev
```

## 📈 Roadmap
- [ ] AI-driven demand forecasting integration.
- [ ] Supplier portal for real-time inventory synchronization.
- [ ] Mobile application for warehouse staff (React Native).

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.

---
*Created by [Abdurrehman](https://github.com/Abdurrehman510) — Aiming to build software that makes an impact.*
