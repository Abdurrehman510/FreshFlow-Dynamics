import React, { useEffect, useState } from 'react'
import { Package, AlertTriangle, TrendingDown, Users, ShoppingCart, RefreshCw } from 'lucide-react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, LineChart, Line } from 'recharts'
import { analyticsApi, productApi } from '../../api/client'
import { StatCard, Spinner, SectionHeader } from '../shared/UIComponents'

const CATEGORY_COLORS = { 'Milk Products': '#22c55e', Fruits: '#f97316', Vegetables: '#84cc16', 'Bakery Products': '#a78bfa' }
const CHART_STYLE = { fontSize: 12, fill: '#9ca3af' }

export default function AdminOverview() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [repricing, setRepricing] = useState(false)
  const [lastUpdated, setLastUpdated] = useState(new Date())

  const load = () => {
    setLoading(true)
    analyticsApi.getDashboard()
      .then(setStats)
      .finally(() => { setLoading(false); setLastUpdated(new Date()) })
  }

  useEffect(() => { load() }, [])

  const handleReprice = async () => {
    setRepricing(true)
    try { await productApi.reprice(); load() }
    finally { setRepricing(false) }
  }

  if (loading) return <Spinner />

  const pieData = stats.categoryBreakdown.map(c => ({ name: c.category, value: c.productCount }))

  return (
    <div className="p-8 space-y-8 animate-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-3xl font-bold text-white">Overview</h1>
          <p className="text-gray-500 text-sm mt-1">Last updated: {lastUpdated.toLocaleTimeString()}</p>
        </div>
        <div className="flex gap-2">
          <button onClick={load} className="btn-ghost flex items-center gap-2 text-sm">
            <RefreshCw size={14} /> Refresh
          </button>
          <button onClick={handleReprice} disabled={repricing} className="btn-primary flex items-center gap-2 text-sm">
            <RefreshCw size={14} className={repricing ? 'animate-spin' : ''} />
            {repricing ? 'Repricing...' : 'Force Reprice All'}
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 xl:grid-cols-3 gap-4">
        <StatCard icon={Package}       label="Total Products"       value={stats.totalProducts}      color="brand" />
        <StatCard icon={AlertTriangle} label="Expiring in 3 Days"   value={stats.expiringIn3Days}    color={stats.expiringIn3Days > 0 ? 'warning' : 'brand'} />
        <StatCard icon={Package}       label="Out of Stock"         value={stats.outOfStock}          color={stats.outOfStock > 0 ? 'danger' : 'brand'} />
        <StatCard icon={TrendingDown}  label="Wastage This Month"   value={`₹${stats.totalWastageThisMonth.toFixed(0)}`} color="danger" />
        <StatCard icon={ShoppingCart}  label="Pending Orders"       value={stats.pendingOrders}       color="info" />
        <StatCard icon={Users}         label="Total Customers"      value={stats.totalCustomers}      color="brand" />
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Wastage bar chart */}
        <div className="card">
          <SectionHeader title="Top Wastage Products" subtitle="Value lost this month (₹)" />
          {stats.topWasters.length === 0 ? (
            <div className="text-gray-500 text-sm py-8 text-center">No wastage recorded this month 🎉</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={stats.topWasters} margin={{ left: -20 }}>
                <XAxis dataKey="productName" tick={CHART_STYLE} tickLine={false}
                  axisLine={false} interval={0} angle={-20} textAnchor="end" height={50}
                  tickFormatter={v => v.length > 12 ? v.slice(0,12)+'…' : v} />
                <YAxis tick={CHART_STYLE} tickLine={false} axisLine={false}
                  tickFormatter={v => `₹${v}`} />
                <Tooltip contentStyle={{ background:'#161b22', border:'1px solid #30363d', borderRadius:8, color:'#e6edf3', fontSize:12 }}
                  formatter={v => [`₹${v.toFixed(2)}`, 'Value Lost']} />
                <Bar dataKey="valueLost" fill="#f85149" radius={[4,4,0,0]} maxBarSize={48} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Category pie chart */}
        <div className="card">
          <SectionHeader title="Inventory by Category" subtitle="Available products per category" />
          <div className="flex items-center gap-6">
            <ResponsiveContainer width="50%" height={200}>
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={55} outerRadius={80}
                  paddingAngle={3} dataKey="value">
                  {pieData.map((entry) => (
                    <Cell key={entry.name} fill={CATEGORY_COLORS[entry.name] || '#58a6ff'} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ background:'#161b22', border:'1px solid #30363d', borderRadius:8, color:'#e6edf3', fontSize:12 }} />
              </PieChart>
            </ResponsiveContainer>
            <div className="space-y-3 flex-1">
              {stats.categoryBreakdown.map(c => (
                <div key={c.category} className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="w-2.5 h-2.5 rounded-full" style={{ background: CATEGORY_COLORS[c.category] || '#58a6ff' }} />
                    <span className="text-sm text-gray-400">{c.category}</span>
                  </div>
                  <div className="text-right">
                    <span className="text-white text-sm font-medium">{c.productCount}</span>
                    <span className="text-gray-600 text-xs ml-1">items</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Avg price per category */}
      <div className="card">
        <SectionHeader title="Average Selling Price by Category" subtitle="Current dynamic prices across inventory" />
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={stats.categoryBreakdown} margin={{ left: -10 }}>
            <XAxis dataKey="category" tick={CHART_STYLE} tickLine={false} axisLine={false} />
            <YAxis tick={CHART_STYLE} tickLine={false} axisLine={false} tickFormatter={v => `₹${v}`} />
            <Tooltip contentStyle={{ background:'#161b22', border:'1px solid #30363d', borderRadius:8, color:'#e6edf3', fontSize:12 }}
              formatter={v => [`₹${v.toFixed(2)}`, 'Avg Price']} />
            <Bar dataKey="avgPrice" radius={[4,4,0,0]} maxBarSize={60}>
              {stats.categoryBreakdown.map((entry) => (
                <Cell key={entry.category} fill={CATEGORY_COLORS[entry.category] || '#58a6ff'} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
