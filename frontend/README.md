# Perishable Platform — Frontend Setup

## Requirements
- Node.js 18+ (https://nodejs.org)
- npm 9+

## Run in development

```bash
# From the frontend/ directory:
cd frontend
npm install
npm run dev
```

Then open: http://localhost:5173

The React app proxies all `/api` calls to `http://localhost:8080`
so make sure the Spring Boot backend is also running.

## Build for production

```bash
npm run build
# Output goes to frontend/dist/
```

Copy the `dist/` folder contents into `src/main/resources/static/`
to serve the React app directly from Spring Boot.

## Demo Credentials
| Username      | Password    | Role         |
|---------------|-------------|--------------|
| storeadmin    | Admin@1234  | Store Admin  |
| rahul_sharma  | Admin@1234  | Customer     |
| priya_patel   | Admin@1234  | Customer     |

## Pages
### Admin
- `/admin`           → Overview dashboard (KPI cards + charts)
- `/admin/products`  → Product CRUD with expiry table
- `/admin/forecast`  → Demand forecast with trend chart
- `/admin/wastage`   → Wastage analytics with bar chart
- `/admin/suppliers` → Supplier reliability table
- `/admin/customers` → Customer management

### Customer
- `/shop`        → Browse & order products
- `/shop/deals`  → Expiry discount deals
- `/shop/orders` → Order history
