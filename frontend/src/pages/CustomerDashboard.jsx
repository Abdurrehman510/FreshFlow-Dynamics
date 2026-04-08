import React, { useEffect, useState } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { ShoppingBag, Tag, ClipboardList, Wallet } from 'lucide-react'
import Sidebar from '../components/shared/Sidebar'
import { useAuthStore } from '../store/authStore'
import { userApi } from '../api/client'
import ShopPage from '../components/customer/ShopPage'
import DealsPage from '../components/customer/DealsPage'
import OrdersPage from '../components/customer/OrdersPage'

const links = [
  { to: '/shop',        icon: ShoppingBag,  label: 'Browse Products' },
  { to: '/shop/deals',  icon: Tag,          label: 'Deals & Discounts' },
  { to: '/shop/orders', icon: ClipboardList, label: 'My Orders' },
]

export default function CustomerDashboard() {
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
          <Route index        element={<ShopPage />} />
          <Route path="deals"  element={<DealsPage />} />
          <Route path="orders" element={<OrdersPage />} />
          <Route path="*"      element={<Navigate to="/shop" replace />} />
        </Routes>
      </main>
    </div>
  )
}
