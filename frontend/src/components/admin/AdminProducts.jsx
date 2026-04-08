import React, { useEffect, useState } from 'react'
import { Plus, Pencil, Trash2, Package, Search, RefreshCw } from 'lucide-react'
import { productApi } from '../../api/client'
import { SectionHeader, Modal, Spinner, EmptyState, ExpiryBadge } from '../shared/UIComponents'
import clsx from 'clsx'

const CATEGORIES = ['MILK', 'FRUIT', 'VEGETABLE', 'BAKERY']
const CATEGORY_LABELS = { MILK: 'Milk Products', FRUIT: 'Fruits', VEGETABLE: 'Vegetables', BAKERY: 'Bakery' }

export default function AdminProducts() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [filterCat, setFilterCat] = useState('ALL')
  const [showAdd, setShowAdd] = useState(false)
  const [editProduct, setEditProduct] = useState(null)
  const [deleteId, setDeleteId] = useState(null)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({ name:'', description:'', basePrice:'', category:'MILK', expiryDate:'', stockQuantity:'', supplierId:'1' })
  const [error, setError] = useState('')

  const load = () => {
    setLoading(true)
    productApi.getAll().then(setProducts).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const filtered = products.filter(p => {
    const matchCat = filterCat === 'ALL' || p.category === filterCat
    const matchSearch = !search || p.name.toLowerCase().includes(search.toLowerCase())
    return matchCat && matchSearch
  })

  const openAdd = () => { setForm({ name:'', description:'', basePrice:'', category:'MILK', expiryDate:'', stockQuantity:'', supplierId:'1' }); setError(''); setShowAdd(true) }
  const openEdit = (p) => { setEditProduct(p); setError('') }

  const handleSave = async () => {
    setSaving(true); setError('')
    try {
      await productApi.create({ ...form, basePrice: parseFloat(form.basePrice), stockQuantity: parseInt(form.stockQuantity), supplierId: parseInt(form.supplierId), expiryDate: form.expiryDate })
      setShowAdd(false); load()
    } catch (e) { setError(e.response?.data?.message || 'Failed to save product') }
    finally { setSaving(false) }
  }

  const handleUpdate = async () => {
    setSaving(true); setError('')
    try {
      await productApi.update(editProduct.id, { name: editProduct.name, description: editProduct.description, basePrice: editProduct.basePrice })
      setEditProduct(null); load()
    } catch (e) { setError(e.response?.data?.message || 'Failed to update') }
    finally { setSaving(false) }
  }

  const handleDelete = async () => {
    try { await productApi.delete(deleteId); setDeleteId(null); load() }
    catch (e) { alert(e.response?.data?.message || 'Delete failed') }
  }

  return (
    <div className="p-8 animate-in">
      <SectionHeader title="Products" subtitle={`${products.length} products total`}
        action={
          <div className="flex gap-2">
            <button onClick={load} className="btn-ghost flex items-center gap-2 text-sm"><RefreshCw size={14}/> Refresh</button>
            <button onClick={openAdd} className="btn-primary flex items-center gap-2 text-sm"><Plus size={14}/> Add Product</button>
          </div>
        }
      />

      {/* Filters */}
      <div className="flex gap-3 mb-5 flex-wrap">
        <div className="relative flex-1 min-w-48">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
          <input className="input pl-9 text-sm" placeholder="Search products..."
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        {['ALL', ...CATEGORIES].map(cat => (
          <button key={cat} onClick={() => setFilterCat(cat)}
            className={clsx('px-3 py-2 rounded-lg text-sm font-medium border transition-all',
              filterCat === cat
                ? 'bg-brand-900/60 text-brand-300 border-brand-700/40'
                : 'bg-surface-card text-gray-400 border-surface-border hover:text-white')}>
            {cat === 'ALL' ? 'All' : CATEGORY_LABELS[cat]}
          </button>
        ))}
      </div>

      {/* Table */}
      {loading ? <Spinner /> : filtered.length === 0 ? (
        <EmptyState icon={Package} title="No products found" desc="Try adjusting your search or filters" />
      ) : (
        <div className="card overflow-hidden p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-surface-border">
                  {['Product', 'Category', 'Base Price', 'Current Price', 'Stock', 'Expiry', 'Wastage%', ''].map(h => (
                    <th key={h} className="text-left px-4 py-3 text-xs text-gray-500 font-medium">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.map((p, i) => (
                  <tr key={p.id} className={clsx('border-b border-surface-border/50 hover:bg-surface-elevated/50 transition-colors',
                    i % 2 === 0 ? '' : 'bg-surface-elevated/20')}>
                    <td className="px-4 py-3">
                      <div className="font-medium text-white">{p.name}</div>
                      <div className="text-gray-600 text-xs truncate max-w-48">{p.description}</div>
                    </td>
                    <td className="px-4 py-3"><span className="badge badge-info">{CATEGORY_LABELS[p.category]}</span></td>
                    <td className="px-4 py-3 font-mono text-gray-400">₹{p.basePrice.toFixed(2)}</td>
                    <td className="px-4 py-3">
                      <span className={clsx('font-mono font-semibold', p.hasDiscount ? 'text-orange-400' : 'text-white')}>
                        ₹{p.currentPrice.toFixed(2)}
                      </span>
                      {p.hasDiscount && <span className="ml-1 text-xs text-orange-500">-{p.discountPercent}%</span>}
                    </td>
                    <td className="px-4 py-3">
                      <span className={clsx('font-mono', p.stockQuantity === 0 ? 'text-danger' : p.stockQuantity <= 5 ? 'text-warning' : 'text-white')}>
                        {p.stockQuantity}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <ExpiryBadge days={p.daysUntilExpiry} critical={p.isCritical} expiringSoon={p.isExpiringSoon} />
                    </td>
                    <td className="px-4 py-3">
                      <span className={clsx('font-mono text-xs', p.wastageRate > 30 ? 'text-danger' : p.wastageRate > 10 ? 'text-warning' : 'text-gray-500')}>
                        {p.wastageRate.toFixed(1)}%
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex gap-1">
                        <button onClick={() => openEdit(p)} className="p-1.5 rounded hover:bg-surface-border text-gray-400 hover:text-white transition-colors"><Pencil size={13}/></button>
                        <button onClick={() => setDeleteId(p.id)} className="p-1.5 rounded hover:bg-danger/10 text-gray-400 hover:text-danger transition-colors"><Trash2 size={13}/></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Add Modal */}
      <Modal open={showAdd} onClose={() => setShowAdd(false)} title="Add New Product">
        {error && <div className="mb-4 p-3 rounded-lg bg-red-900/30 border border-red-700/40 text-red-400 text-sm">{error}</div>}
        <div className="space-y-3">
          {[['Name','name','text'],['Description','description','text'],['Base Price (₹)','basePrice','number'],['Stock Quantity','stockQuantity','number'],['Supplier ID','supplierId','number']].map(([label, key, type]) => (
            <div key={key}>
              <label className="block text-xs text-gray-400 mb-1">{label}</label>
              <input className="input text-sm" type={type} value={form[key]} onChange={e => setForm({...form, [key]: e.target.value})} />
            </div>
          ))}
          <div>
            <label className="block text-xs text-gray-400 mb-1">Category</label>
            <select className="input text-sm" value={form.category} onChange={e => setForm({...form, category: e.target.value})}>
              {CATEGORIES.map(c => <option key={c} value={c}>{CATEGORY_LABELS[c]}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs text-gray-400 mb-1">Expiry Date</label>
            <input className="input text-sm" type="date" value={form.expiryDate} onChange={e => setForm({...form, expiryDate: e.target.value})} />
          </div>
          <div className="flex gap-2 pt-2">
            <button onClick={() => setShowAdd(false)} className="btn-ghost flex-1 text-sm">Cancel</button>
            <button onClick={handleSave} disabled={saving} className="btn-primary flex-1 text-sm">{saving ? 'Saving...' : 'Add Product'}</button>
          </div>
        </div>
      </Modal>

      {/* Edit Modal */}
      <Modal open={!!editProduct} onClose={() => setEditProduct(null)} title="Edit Product">
        {editProduct && (
          <div className="space-y-3">
            {error && <div className="p-3 rounded-lg bg-red-900/30 border border-red-700/40 text-red-400 text-sm">{error}</div>}
            <div><label className="block text-xs text-gray-400 mb-1">Name</label>
              <input className="input text-sm" value={editProduct.name} onChange={e => setEditProduct({...editProduct, name: e.target.value})} /></div>
            <div><label className="block text-xs text-gray-400 mb-1">Description</label>
              <input className="input text-sm" value={editProduct.description} onChange={e => setEditProduct({...editProduct, description: e.target.value})} /></div>
            <div><label className="block text-xs text-gray-400 mb-1">Base Price (₹)</label>
              <input className="input text-sm" type="number" value={editProduct.basePrice} onChange={e => setEditProduct({...editProduct, basePrice: parseFloat(e.target.value)})} /></div>
            <div className="flex gap-2 pt-2">
              <button onClick={() => setEditProduct(null)} className="btn-ghost flex-1 text-sm">Cancel</button>
              <button onClick={handleUpdate} disabled={saving} className="btn-primary flex-1 text-sm">{saving ? 'Saving...' : 'Update'}</button>
            </div>
          </div>
        )}
      </Modal>

      {/* Delete confirm */}
      <Modal open={!!deleteId} onClose={() => setDeleteId(null)} title="Confirm Delete">
        <p className="text-gray-400 mb-5">Are you sure you want to delete this product? This cannot be undone.</p>
        <div className="flex gap-2">
          <button onClick={() => setDeleteId(null)} className="btn-ghost flex-1">Cancel</button>
          <button onClick={handleDelete} className="btn-danger flex-1">Delete</button>
        </div>
      </Modal>
    </div>
  )
}
