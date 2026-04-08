# ⚛️ Frontend Engineering Guide: FreshFlow Dynamics

The FreshFlow UI is a high-performance Single Page Application (SPA) built with React 18 and Vite.

## 🏗️ Technical Stack

- **Framework**: React 18 (Functional Components, Hooks)
- **Styling**: Tailwind CSS (Utility-first)
- **State Management**: Zustand (Atomic, lightweight)
- **Visuals**: Recharts (Dynamic charting)
- **Navigation**: React Router DOM v6
- **Icons**: Lucide React

## 📦 Component Architecture

```text
src/
├── components/
│   ├── admin/       # Dashboard, Inventory management, Analytics
│   ├── customer/    # Shopping, Deals, Order history
│   └── shared/      # Siderbar, Modal, Button, UI atoms
├── store/           # Zustand stores (auth, cart)
├── api/             # Axios instance & request interceptors
└── pages/           # Route-level containers
```

## 🧠 State Management (Zustand)
We use a centralized state approach for cross-cutting concerns:
- **AuthStore**: Manages JWT lifecycle, user profile, and persistent login state.
- **InventoryStore**: Optimistic UI updates for admin product deletions and edits.

## 🎨 Design System
Tailwind configuration ensures a cohesive enterprise look:
- **Theme**: Dark-mode primary with vibrant accent colors (Emerald for fresh, Amber for expiring).
- **Responsive**: Mobile-first design using grid/flexbox.

## 📡 API Integration
All requests are piped through `src/api/client.js`.
- **Interceptors**: Automatically attach JWT tokens to the `Authorization` header.
- **Handling**: Unified error handling for 401 (Expired token) and 500 (Server error) responses.
