import React from 'react'
import clsx from 'clsx'

// ── KPI Stat Card ─────────────────────────────────────────────
export function StatCard({ icon: Icon, label, value, sub, color = 'brand', trend }) {
  const colors = {
    brand:   'text-brand-400 bg-brand-900/40 border-brand-700/30',
    danger:  'text-danger bg-red-900/30 border-red-700/30',
    warning: 'text-warning bg-yellow-900/30 border-yellow-700/30',
    info:    'text-info bg-blue-900/30 border-blue-700/30',
  }
  return (
    <div className="card animate-in">
      <div className="flex items-start justify-between mb-3">
        <div className={clsx('w-9 h-9 rounded-lg border flex items-center justify-center', colors[color])}>
          <Icon size={16} />
        </div>
        {trend !== undefined && (
          <span className={clsx('text-xs font-mono font-medium', trend >= 0 ? 'text-brand-400' : 'text-danger')}>
            {trend >= 0 ? '+' : ''}{trend}%
          </span>
        )}
      </div>
      <div className="font-display text-2xl font-bold text-white mb-0.5">{value}</div>
      <div className="text-sm text-gray-400">{label}</div>
      {sub && <div className="text-xs text-gray-600 mt-1">{sub}</div>}
    </div>
  )
}

// ── Product Card ──────────────────────────────────────────────
export function ProductCard({ product, onOrder }) {
  const daysLeft = product.daysUntilExpiry
  const expiryColor = product.isCritical ? 'badge-danger' : product.isExpiringSoon ? 'badge-warning' : 'badge-fresh'
  const expiryLabel = product.isCritical ? `${daysLeft}d — CRITICAL`
    : product.isExpiringSoon ? `${daysLeft}d left`
    : `${daysLeft} days fresh`

  return (
    <div className="card hover:border-surface-elevated transition-all duration-200 group flex flex-col">
      <div className="flex items-start justify-between mb-3">
        <span className={clsx('badge', expiryColor)}>{expiryLabel}</span>
        {product.hasDiscount && (
          <span className="badge bg-orange-900/40 text-orange-400 border border-orange-700/40 font-mono">
            -{product.discountPercent}%
          </span>
        )}
      </div>

      <h3 className="font-display font-semibold text-white mb-1 leading-snug">{product.name}</h3>
      <p className="text-xs text-gray-500 mb-3 flex-1 line-clamp-2">{product.description}</p>

      <div className="flex items-end justify-between">
        <div>
          {product.hasDiscount && (
            <div className="text-xs text-gray-600 line-through font-mono">₹{product.basePrice.toFixed(2)}</div>
          )}
          <div className={clsx('font-mono font-bold text-lg', product.hasDiscount ? 'text-orange-400' : 'text-white')}>
            ₹{product.currentPrice.toFixed(2)}
          </div>
        </div>
        <div className="text-right">
          <div className="text-xs text-gray-500">{product.stockQuantity} in stock</div>
          {product.stockQuantity <= 5 && product.stockQuantity > 0 && (
            <div className="text-xs text-warning">Low stock!</div>
          )}
        </div>
      </div>

      {onOrder && product.isAvailable && (
        <button onClick={() => onOrder(product)}
          className="btn-primary w-full mt-3 py-1.5 text-sm opacity-0 group-hover:opacity-100 transition-opacity">
          Order Now
        </button>
      )}
    </div>
  )
}

// ── Expiry Status Badge ───────────────────────────────────────
export function ExpiryBadge({ days, critical, expiringSoon }) {
  if (critical) return <span className="badge badge-danger">Expires in {days}d ⚠</span>
  if (expiringSoon) return <span className="badge badge-warning">{days}d left</span>
  return <span className="badge badge-fresh">{days}d fresh</span>
}

// ── Loading Spinner ───────────────────────────────────────────
export function Spinner({ size = 20 }) {
  return (
    <div className="flex items-center justify-center p-8">
      <div className="border-2 border-surface-border border-t-brand-500 rounded-full animate-spin"
        style={{ width: size, height: size }} />
    </div>
  )
}

// ── Empty State ───────────────────────────────────────────────
export function EmptyState({ icon: Icon, title, desc }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="w-12 h-12 rounded-xl bg-surface-elevated border border-surface-border flex items-center justify-center mb-4">
        <Icon size={20} className="text-gray-500" />
      </div>
      <div className="text-white font-medium mb-1">{title}</div>
      {desc && <div className="text-gray-500 text-sm max-w-xs">{desc}</div>}
    </div>
  )
}

// ── Section Header ────────────────────────────────────────────
export function SectionHeader({ title, subtitle, action }) {
  return (
    <div className="flex items-center justify-between mb-5">
      <div>
        <h2 className="font-display text-xl font-bold text-white">{title}</h2>
        {subtitle && <p className="text-gray-500 text-sm mt-0.5">{subtitle}</p>}
      </div>
      {action}
    </div>
  )
}

// ── Modal ─────────────────────────────────────────────────────
export function Modal({ open, onClose, title, children }) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/70 backdrop-blur-sm" onClick={onClose} />
      <div className="relative w-full max-w-md card-elevated shadow-2xl animate-in">
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-display text-lg font-bold text-white">{title}</h3>
          <button onClick={onClose} className="text-gray-500 hover:text-white transition-colors">✕</button>
        </div>
        {children}
      </div>
    </div>
  )
}
