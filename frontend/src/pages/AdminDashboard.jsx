import React, { useEffect, useState } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { LayoutDashboard, Package, TrendingUp, BarChart3, Truck, Users, Zap } from 'lucide-react'
import Sidebar from '../components/shared/Sidebar'
import { useAuthStore } from '../store/authStore'
import { userApi } from '../api/client'
import AdminOverview from '../components/admin/AdminOverview'
import AdminProducts from '../components/admin/AdminProducts'
import AdminForecast from '../components/admin/AdminForecast'
import AdminWastage from '../components/admin/AdminWastage'
import AdminSuppliers from '../components/admin/AdminSuppliers'
import AdminCustomers from '../components/admin/AdminCustomers'

const links = [
  { to: '/admin',           icon: LayoutDashboard, label: 'Overview' },
  { to: '/admin/products',  icon: Package,         label: 'Products' },
  { to: '/admin/forecast',  icon: TrendingUp,      label: 'Demand Forecast' },
  { to: '/admin/wastage',   icon: BarChart3,        label: 'Wastage Analytics' },
  { to: '/admin/suppliers', icon: Truck,            label: 'Suppliers' },
  { to: '/admin/customers', icon: Users,            label: 'Customers' },
]

export default function AdminDashboard() {
  const { user, login } = useAuthStore()
  const [profile, setProfile] = useState(user)

  useEffect(() => {
    userApi.getMe().then(data => { setProfile(data); login({ ...user, ...data }) }).catch(() => {})
  }, [])

  return (
    <div className="flex min-h-screen bg-surface">
      <Sidebar links={links} user={profile || user} />
      <main className="flex-1 overflow-auto">
        <Routes>
          <Route index          element={<AdminOverview />} />
          <Route path="products"  element={<AdminProducts />} />
          <Route path="forecast"  element={<AdminForecast />} />
          <Route path="wastage"   element={<AdminWastage />} />
          <Route path="suppliers" element={<AdminSuppliers />} />
          <Route path="customers" element={<AdminCustomers />} />
          <Route path="*"         element={<Navigate to="/admin" replace />} />
        </Routes>
      </main>
    </div>
  )
}
