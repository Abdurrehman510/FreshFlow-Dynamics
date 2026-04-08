import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import './index.css'
import LoginPage from './pages/LoginPage'
import AdminDashboard from './pages/AdminDashboard'
import CustomerDashboard from './pages/CustomerDashboard'
import { useAuthStore } from './store/authStore'

function ProtectedRoute({ children, adminOnly = false }) {
  const { token, user } = useAuthStore()
  if (!token) return <Navigate to="/login" replace />
  if (adminOnly && user && user.role === 'CUSTOMER') return <Navigate to="/shop" replace />
  return children
}

function App() {
  const { token, user } = useAuthStore()

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={!token ? <LoginPage /> : <Navigate to={user?.role === 'CUSTOMER' ? '/shop' : '/admin'} replace />} />
        <Route path="/admin/*" element={<ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>} />
        <Route path="/shop/*" element={<ProtectedRoute><CustomerDashboard /></ProtectedRoute>} />
        <Route path="/" element={<Navigate to={token ? (user?.role === 'CUSTOMER' ? '/shop' : '/admin') : '/login'} replace />} />
      </Routes>
    </BrowserRouter>
  )
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />)
