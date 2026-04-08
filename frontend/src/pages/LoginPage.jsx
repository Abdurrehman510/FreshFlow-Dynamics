import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Leaf, Zap, TrendingUp, ShieldCheck, Eye, EyeOff } from 'lucide-react'
import { authApi } from '../api/client'
import { useAuthStore } from '../store/authStore'

export default function LoginPage() {
  const [mode, setMode] = useState('login') // login | register
  const [form, setForm] = useState({ username: '', password: '', role: 'CUSTOMER' })
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const { login } = useAuthStore()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true); setError('')
    try {
      const data = mode === 'login'
        ? await authApi.login({ username: form.username, password: form.password })
        : await authApi.register(form)
      login(data)
      navigate(data.role === 'CUSTOMER' ? '/shop' : '/admin')
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong')
    } finally {
      setLoading(false)
    }
  }

  const features = [
    { icon: Zap, label: 'Dynamic Expiry Pricing', desc: 'Auto-discounts up to 70% before expiry' },
    { icon: TrendingUp, label: 'Demand Forecasting', desc: '30-day weighted moving average' },
    { icon: ShieldCheck, label: 'Wastage Analytics', desc: 'Track & reduce inventory loss' },
  ]

  return (
    <div className="min-h-screen flex bg-surface">
      {/* Left panel */}
      <div className="hidden lg:flex flex-col justify-between w-1/2 p-12 bg-surface-card border-r border-surface-border">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-brand-600 rounded-lg flex items-center justify-center">
            <Leaf size={16} className="text-white" />
          </div>
          <span className="font-display font-bold text-lg text-white">Perishable Platform</span>
        </div>

        <div>
          <h1 className="font-display text-5xl font-bold text-white leading-tight mb-4">
            Stop Wasting.<br />
            <span className="text-brand-400">Start Selling.</span>
          </h1>
          <p className="text-gray-400 text-lg mb-12 max-w-sm">
            India's only perishables platform with AI-powered pricing and demand forecasting.
          </p>
          <div className="space-y-5">
            {features.map(({ icon: Icon, label, desc }) => (
              <div key={label} className="flex items-start gap-4">
                <div className="w-10 h-10 rounded-lg bg-brand-900/60 border border-brand-700/40 flex items-center justify-center shrink-0">
                  <Icon size={18} className="text-brand-400" />
                </div>
                <div>
                  <div className="text-white font-medium">{label}</div>
                  <div className="text-gray-500 text-sm">{desc}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <p className="text-gray-600 text-sm">© 2024 Perishable Platform. Built for India's kirana ecosystem.</p>
      </div>

      {/* Right panel — form */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="w-full max-w-md animate-in">
          <div className="lg:hidden flex items-center gap-2 mb-8">
            <div className="w-7 h-7 bg-brand-600 rounded-md flex items-center justify-center">
              <Leaf size={14} className="text-white" />
            </div>
            <span className="font-display font-bold text-white">Perishable Platform</span>
          </div>

          <h2 className="font-display text-3xl font-bold text-white mb-1">
            {mode === 'login' ? 'Welcome back' : 'Create account'}
          </h2>
          <p className="text-gray-400 mb-8">
            {mode === 'login' ? 'Sign in to your dashboard' : 'Join the platform today'}
          </p>

          {error && (
            <div className="mb-5 px-4 py-3 rounded-lg bg-red-900/30 border border-red-700/40 text-red-400 text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm text-gray-400 mb-1.5">Username</label>
              <input className="input" placeholder="e.g. storeadmin"
                value={form.username} onChange={e => setForm({...form, username: e.target.value})} required />
            </div>
            <div>
              <label className="block text-sm text-gray-400 mb-1.5">Password</label>
              <div className="relative">
                <input className="input pr-10" type={showPw ? 'text' : 'password'}
                  placeholder="Min. 8 characters"
                  value={form.password} onChange={e => setForm({...form, password: e.target.value})} required />
                <button type="button" onClick={() => setShowPw(!showPw)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-300">
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>
            {mode === 'register' && (
              <div>
                <label className="block text-sm text-gray-400 mb-1.5">Account type</label>
                <select className="input" value={form.role} onChange={e => setForm({...form, role: e.target.value})}>
                  <option value="CUSTOMER">Customer</option>
                  <option value="STORE_ADMIN">Store Admin</option>
                </select>
              </div>
            )}
            <button type="submit" disabled={loading} className="btn-primary w-full py-2.5 mt-2">
              {loading ? 'Please wait...' : mode === 'login' ? 'Sign in' : 'Create account'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <span className="text-gray-500 text-sm">
              {mode === 'login' ? "Don't have an account? " : 'Already have an account? '}
            </span>
            <button onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError('') }}
              className="text-brand-400 hover:text-brand-300 text-sm font-medium transition-colors">
              {mode === 'login' ? 'Register' : 'Sign in'}
            </button>
          </div>

          <div className="mt-8 p-4 rounded-lg bg-surface-elevated border border-surface-border">
            <p className="text-xs text-gray-500 mb-2 font-medium">Demo credentials</p>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button onClick={() => setForm({ username: 'storeadmin', password: 'Admin@1234', role: 'STORE_ADMIN' })}
                className="text-left p-2 rounded bg-surface-border/40 hover:bg-surface-border/80 transition-colors">
                <div className="text-brand-400 font-medium">storeadmin</div>
                <div className="text-gray-500">Admin@1234 · Admin</div>
              </button>
              <button onClick={() => setForm({ username: 'rahul_sharma', password: 'Admin@1234', role: 'CUSTOMER' })}
                className="text-left p-2 rounded bg-surface-border/40 hover:bg-surface-border/80 transition-colors">
                <div className="text-brand-400 font-medium">rahul_sharma</div>
                <div className="text-gray-500">Admin@1234 · Customer</div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
