import React, { useEffect, useState } from 'react'
import { Search, ShoppingBag, Wallet, CheckCircle } from 'lucide-react'
import { productApi, orderApi, userApi } from '../../api/client'
import { ProductCard, Spinner, EmptyState, Modal, SectionHeader } from '../shared/UIComponents'
import { useAuthStore } from '../../store/authStore'
import clsx from 'clsx'

const SORT_OPTIONS = [
  { key: 'default',  label: 'Default' },
  { key: 'price_asc',  label: 'Price: Low → High' },
  { key: 'price_desc', label: 'Price: High → Low' },
  { key: 'expiry',     label: 'Freshest First' },
  { key: 'discount',   label: 'Best Deals' },
]

export default function ShopPage() {
  const [products, setProducts]     = useState([])
  const [loading, setLoading]       = useState(true)
  const [search, setSearch]         = useState('')
  const [sort, setSort]             = useState('default')
  const [ordering, setOrdering]     = useState(null)   // product being ordered
  const [qty, setQty]               = useState(1)
  const [payMethod, setPayMethod]   = useState('CASH')
  const [orderResult, setOrderResult] = useState(null)
  const [placing, setPlacing]       = useState(false)
  const [orderError, setOrderError] = useState('')
  const [wallet, setWallet]         = useState(0)
  const { login, user }             = useAuthStore()

  const loadProducts = () => {
    setLoading(true)
    const req = search.trim() ? productApi.search(search.trim()) : productApi.getAvailable()
    req.then(setProducts).finally(() => setLoading(false))
  }

  useEffect(() => { loadProducts() }, [])

  useEffect(() => {
    userApi.getMe().then(u => { setWallet(u.walletBalance); login({ ...user, ...u }) })
  }, [])

  const handleSearch = (e) => { e.preventDefault(); loadProducts() }

  const sorted = [...products].sort((a, b) => {
    if (sort === 'price_asc')  return a.currentPrice - b.currentPrice
    if (sort === 'price_desc') return b.currentPrice - a.currentPrice
    if (sort === 'expiry')     return a.daysUntilExpiry - b.daysUntilExpiry
    if (sort === 'discount')   return b.discountPercent - a.discountPercent
    return 0
  })

  const openOrder = (product) => { setOrdering(product); setQty(1); setPayMethod('CASH'); setOrderError(''); setOrderResult(null) }

  const handlePlaceOrder = async () => {
    setPlacing(true); setOrderError('')
    try {
      const result = await orderApi.place({ productId: ordering.id, quantity: qty, paymentMethod: payMethod })
      setOrderResult(result)
      // Refresh wallet balance
      const me = await userApi.getMe()
      setWallet(me.walletBalance)
      login({ ...user, ...me })
      loadProducts()
    } catch (e) { setOrderError(e.response?.data?.message || 'Order failed') }
    finally { setPlacing(false) }
  }

  const orderTotal = ordering ? (ordering.currentPrice * qty).toFixed(2) : 0

  return (
    <div className="p-8 animate-in">
      <div className="flex items-center justify-between mb-6">
        <SectionHeader title="Browse Products" subtitle={`${products.length} items available`} />
        <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-surface-elevated border border-surface-border">
          <Wallet size={14} className="text-brand-400" />
          <span className="text-white text-sm font-mono font-medium">₹{wallet.toFixed(2)}</span>
        </div>
      </div>

      {/* Search + Sort */}
      <form onSubmit={handleSearch} className="flex gap-3 mb-6 flex-wrap">
        <div className="relative flex-1 min-w-64">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
          <input className="input pl-9 text-sm" placeholder="Search products..."
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <button type="submit" className="btn-primary text-sm px-5">Search</button>
        {search && <button type="button" onClick={() => { setSearch(''); productApi.getAvailable().then(setProducts) }}
          className="btn-ghost text-sm">Clear</button>}
        <select className="input text-sm w-44" value={sort} onChange={e => setSort(e.target.value)}>
          {SORT_OPTIONS.map(o => <option key={o.key} value={o.key}>{o.label}</option>)}
        </select>
      </form>

      {/* Products grid */}
      {loading ? <Spinner /> : sorted.length === 0 ? (
        <EmptyState icon={ShoppingBag} title="No products found" desc={search ? `No results for "${search}"` : 'Check back later for new stock'} />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-4">
          {sorted.map(p => <ProductCard key={p.id} product={p} onOrder={openOrder} />)}
        </div>
      )}

      {/* Order Modal */}
      <Modal open={!!ordering && !orderResult} onClose={() => setOrdering(null)} title="Place Order">
        {ordering && (
          <div className="space-y-4">
            <div className="p-4 rounded-lg bg-surface-elevated border border-surface-border">
              <div className="font-medium text-white mb-1">{ordering.name}</div>
              <div className="text-gray-500 text-xs mb-2">{ordering.description}</div>
              <div className="flex items-center justify-between">
                <div>
                  {ordering.hasDiscount && <div className="text-xs text-gray-600 line-through">₹{ordering.basePrice.toFixed(2)}</div>}
                  <span className={clsx('font-mono font-bold', ordering.hasDiscount ? 'text-orange-400' : 'text-white')}>
                    ₹{ordering.currentPrice.toFixed(2)}
                  </span>
                  {ordering.hasDiscount && <span className="ml-1.5 text-xs text-orange-500">-{ordering.discountPercent}% off</span>}
                </div>
                <span className="text-xs text-gray-500">{ordering.stockQuantity} in stock</span>
              </div>
            </div>

            {orderError && <div className="p-3 rounded-lg bg-red-900/30 border border-red-700/40 text-red-400 text-sm">{orderError}</div>}

            <div>
              <label className="block text-xs text-gray-400 mb-1.5">Quantity</label>
              <div className="flex items-center gap-3">
                <button onClick={() => setQty(q => Math.max(1, q-1))} className="w-9 h-9 rounded-lg bg-surface-elevated border border-surface-border text-white font-bold hover:bg-surface-border transition-colors">−</button>
                <span className="font-mono text-xl text-white w-8 text-center">{qty}</span>
                <button onClick={() => setQty(q => Math.min(ordering.stockQuantity, q+1))} className="w-9 h-9 rounded-lg bg-surface-elevated border border-surface-border text-white font-bold hover:bg-surface-border transition-colors">+</button>
              </div>
            </div>

            <div>
              <label className="block text-xs text-gray-400 mb-1.5">Payment Method</label>
              <div className="grid grid-cols-2 gap-2">
                {[['CASH','Cash on Delivery'],['WALLET','Wallet']].map(([val, label]) => (
                  <button key={val} onClick={() => setPayMethod(val)}
                    className={clsx('p-3 rounded-lg border text-sm font-medium transition-all',
                      payMethod === val
                        ? 'bg-brand-900/60 border-brand-600 text-brand-300'
                        : 'bg-surface-elevated border-surface-border text-gray-400 hover:text-white')}>
                    {label}
                    {val === 'WALLET' && <div className="text-xs text-gray-500 font-normal mt-0.5">Balance: ₹{wallet.toFixed(2)}</div>}
                  </button>
                ))}
              </div>
              {payMethod === 'WALLET' && wallet < parseFloat(orderTotal) && (
                <p className="text-warning text-xs mt-2">⚠ Insufficient wallet balance for this order</p>
              )}
            </div>

            <div className="flex items-center justify-between p-3 rounded-lg bg-surface-elevated border border-surface-border">
              <span className="text-gray-400 text-sm">Total</span>
              <span className="font-mono font-bold text-white text-lg">₹{orderTotal}</span>
            </div>

            <div className="flex gap-2">
              <button onClick={() => setOrdering(null)} className="btn-ghost flex-1">Cancel</button>
              <button onClick={handlePlaceOrder} disabled={placing || (payMethod === 'WALLET' && wallet < parseFloat(orderTotal))}
                className="btn-primary flex-1">{placing ? 'Placing...' : 'Confirm Order'}</button>
            </div>
          </div>
        )}
      </Modal>

      {/* Success modal */}
      <Modal open={!!orderResult} onClose={() => { setOrderResult(null); setOrdering(null) }} title="Order Confirmed!">
        <div className="text-center py-4">
          <div className="w-16 h-16 rounded-full bg-brand-900/50 border border-brand-600 flex items-center justify-center mx-auto mb-4">
            <CheckCircle size={28} className="text-brand-400" />
          </div>
          <div className="text-white font-medium mb-1">Order placed successfully</div>
          <div className="text-gray-400 text-sm mb-1">Order #{orderResult?.id}</div>
          <div className="font-mono text-brand-400 text-xl font-bold">₹{orderResult?.totalAmount?.toFixed(2)}</div>
          <div className="text-gray-600 text-xs mt-1">{orderResult?.paymentMethod === 'WALLET' ? 'Deducted from wallet' : 'Pay on delivery'}</div>
          <button onClick={() => { setOrderResult(null); setOrdering(null) }} className="btn-primary mt-6 px-8">Done</button>
        </div>
      </Modal>
    </div>
  )
}
