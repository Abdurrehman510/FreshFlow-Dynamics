import React, { useEffect, useState } from 'react'
import { Tag, ClipboardList, Package } from 'lucide-react'
import { productApi, orderApi } from '../../api/client'
import { ProductCard, Spinner, EmptyState, SectionHeader } from '../shared/UIComponents'
import clsx from 'clsx'

// ── Deals Page ────────────────────────────────────────────────
export function DealsPage() {
  const [deals, setDeals] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => { productApi.getDeals().then(setDeals).finally(() => setLoading(false)) }, [])

  const totalSavings = deals.reduce((acc, p) => acc + (p.basePrice - p.currentPrice), 0)

  return (
    <div className="p-8 animate-in">
      <SectionHeader title="Today's Deals" subtitle="Products with expiry discounts — save up to 70%" />

      {deals.length > 0 && (
        <div className="mb-6 p-4 rounded-xl bg-orange-900/20 border border-orange-700/30 flex items-center gap-4">
          <Tag size={20} className="text-orange-400 shrink-0" />
          <div>
            <div className="text-orange-300 font-medium">{deals.length} deal{deals.length !== 1 ? 's' : ''} available today</div>
            <div className="text-gray-500 text-sm">Buy all deals and save up to ₹{totalSavings.toFixed(2)} compared to base prices</div>
          </div>
        </div>
      )}

      {loading ? <Spinner /> : deals.length === 0 ? (
        <EmptyState icon={Tag} title="No deals right now" desc="Check back later — deals are updated nightly as products approach expiry" />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-4">
          {deals.map(p => <ProductCard key={p.id} product={p} />)}
        </div>
      )}
    </div>
  )
}

// ── Orders Page ───────────────────────────────────────────────
const STATUS_STYLES = {
  PENDING:   'badge-warning',
  CONFIRMED: 'badge-info',
  DELIVERED: 'badge-fresh',
  CANCELLED: 'badge-danger',
}

export function OrdersPage() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => { orderApi.getMyOrders().then(setOrders).finally(() => setLoading(false)) }, [])

  const totalSpent = orders.filter(o => o.status !== 'CANCELLED')
    .reduce((acc, o) => acc + o.totalAmount, 0)

  return (
    <div className="p-8 animate-in">
      <SectionHeader title="My Orders" subtitle={`${orders.length} orders placed`} />

      {orders.length > 0 && (
        <div className="grid grid-cols-3 gap-4 mb-6">
          <div className="card"><div className="text-gray-400 text-sm mb-1">Total Orders</div><div className="font-display text-2xl font-bold text-white">{orders.length}</div></div>
          <div className="card"><div className="text-gray-400 text-sm mb-1">Total Spent</div><div className="font-display text-2xl font-bold text-brand-400">₹{totalSpent.toFixed(2)}</div></div>
          <div className="card"><div className="text-gray-400 text-sm mb-1">Delivered</div><div className="font-display text-2xl font-bold text-white">{orders.filter(o => o.status === 'DELIVERED').length}</div></div>
        </div>
      )}

      {loading ? <Spinner /> : orders.length === 0 ? (
        <EmptyState icon={ClipboardList} title="No orders yet" desc="Head over to Browse Products to place your first order" />
      ) : (
        <div className="space-y-3">
          {orders.map(order => (
            <div key={order.id} className="card hover:border-surface-elevated transition-colors">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <div className="flex items-center gap-2.5">
                    <span className="font-display font-bold text-white">Order #{order.id}</span>
                    <span className={clsx('badge', STATUS_STYLES[order.status] || 'badge-info')}>{order.status}</span>
                  </div>
                  <div className="text-gray-500 text-xs mt-0.5">
                    {new Date(order.placedAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' })}
                    {' · '}{order.paymentMethod}
                  </div>
                </div>
                <div className="font-mono font-bold text-white">₹{order.totalAmount.toFixed(2)}</div>
              </div>

              <div className="space-y-1.5">
                {order.lines.map((line, i) => (
                  <div key={i} className="flex items-center justify-between text-sm py-1.5 border-t border-surface-border/40">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 rounded bg-surface-elevated border border-surface-border flex items-center justify-center">
                        <Package size={11} className="text-gray-500" />
                      </div>
                      <span className="text-white">{line.productName}</span>
                      <span className="text-gray-500">×{line.quantity}</span>
                    </div>
                    <div className="text-right">
                      <span className="text-gray-400 font-mono text-xs">₹{line.pricePerUnit.toFixed(2)} each</span>
                      <span className="text-white font-mono ml-2">₹{line.lineTotal.toFixed(2)}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default DealsPage
