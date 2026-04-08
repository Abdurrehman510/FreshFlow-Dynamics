# 📡 API Reference: FreshFlow Dynamics

The FreshFlow API is a RESTful service built with Spring Boot, secured via JWT, and designed for high-concurrency inventory operations.

## 🔐 Authentication
All non-public endpoints require a `Bearer` token in the `Authorization` header.
- **Login**: `POST /api/auth/login` (Returns JWT)
- **Roles**: `CUSTOMER`, `STORE_ADMIN`, `PLATFORM_ADMIN`.

## 📦 Products API

| Method | Endpoint | Access | Description |
|:--- |:--- |:--- |:--- |
| `GET` | `/api/products/available` | Public | List all fresh products in stock. |
| `GET` | `/api/products/deals` | Public | List highly discounted products near expiry. |
| `GET` | `/api/products/{id}` | Public | Get detailed product metadata. |
| `POST` | `/api/products` | Admin | Create a new perishable product. |
| `PUT` | `/api/products/{id}` | Admin | Update product details. |
| `DELETE` | `/api/products/{id}` | Admin | Remove product from inventory. |
| `POST` | `/api/products/reprice` | Admin | Trigger manual run of the Dynamic Pricing Engine. |

## 📊 Analytics & Reporting

| Method | Endpoint | Access | Description |
|:--- |:--- |:--- |:--- |
| `GET` | `/api/analytics/dashboard` | Admin | KPI summary (Wastage, Stock, Customers). |
| `GET` | `/api/analytics/wastage` | Admin | Deep dive into lost value per category/date. |
| `GET` | `/api/analytics/forecast` | Admin | Product-level demand predictions (WMA). |
| `GET` | `/api/analytics/suppliers` | Admin | Performance metrics for integrated suppliers. |

## 🛒 Orders & Transactions

| Method | Endpoint | Access | Description |
|:--- |:--- |:--- |:--- |
| `POST` | `/api/orders` | Customer | Place order with dynamic prices. |
| `GET` | `/api/orders/history` | User | View personal or global order history. |

---

## 📮 Postman Collection
Copy the following JSON and import it into Postman to get started immediately:

```json
{
	"info": {
		"name": "FreshFlow Dynamics API",
		"schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
	},
	"item": [
		{
			"name": "Auth",
			"item": [
				{
					"name": "Login",
					"request": {
						"method": "POST",
						"url": "http://localhost:8080/api/auth/login",
						"body": {
							"mode": "raw",
							"raw": "{\"username\": \"admin\", \"password\": \"password\"}"
						}
					}
				}
			]
		},
		{
			"name": "Products",
			"item": [
				{
					"name": "Get Available",
					"request": {
						"method": "GET",
						"url": "http://localhost:8080/api/products/available"
					}
				}
			]
		}
	]
}
```
