import React, { useEffect, useState } from 'react'
import { Truck, Plus, Users, Trash2 } from 'lucide-react'
import { analyticsApi, userApi } from '../../api/client'
import { SectionHeader, Spinner, EmptyState, Modal } from '../shared/UIComponents'
import clsx from 'clsx'

const CATEGORIES = ['MILK', 'FRUIT', 'VEGETABLE', 'BAKERY']
const CAT_LABELS  = { MILK: 'Milk', FRUIT: 'Fruits', VEGETABLE: 'Vegetables', BAKERY: 'Bakery' }

// ── Admin Suppliers ───────────────────────────────────────────
export function AdminSuppliers() {
  const [suppliers, setSuppliers] = useState([])
  const [loading, setLoading] = useState(true)
  const [showAdd, setShowAdd] = useState(false)
  const [form, setForm] = useState({ name:'', contactNumber:'', email:'', category:'MILK' })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => { analyticsApi.getSuppliers().then(setSuppliers).finally(() => setLoading(false)) }, [])

  const handleAdd = async () => {
    setSaving(true); setError('')
    try {
      const s = await analyticsApi.addSupplier(form)
      setSuppliers(prev => [...prev, s]); setShowAdd(false)
    } catch (e) { setError(e.response?.data?.message || 'Failed') }
    finally { setSaving(false) }
  }

  const reliabilityBar = (score) => {
    const pct = Math.round(score * 100)
    const color = pct >= 90 ? 'bg-brand-500' : pct >= 75 ? 'bg-warning' : 'bg-danger'
    return (
      <div className="flex items-center gap-2">
        <div className="w-20 h-1.5 bg-surface-border rounded-full overflow-hidden">
          <div className={clsx('h-full rounded-full', color)} style={{ width: `${pct}%` }} />
        </div>
        <span className="font-mono text-xs text-gray-400">{pct}%</span>
      </div>
    )
  }

  return (
    <div className="p-8 animate-in">
      <SectionHeader title="Suppliers" subtitle={`${suppliers.length} registered suppliers`}
        action={<button onClick={() => setShowAdd(true)} className="btn-primary flex items-center gap-2 text-sm"><Plus size={14}/> Add Supplier</button>} />

      {loading ? <Spinner /> : suppliers.length === 0 ? (
        <EmptyState icon={Truck} title="No suppliers yet" desc="Add your first supplier to enable smart routing" />
      ) : (
        <div className="card overflow-hidden p-0">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-surface-border">
                {['Supplier','Category','Contact','Avg Delivery','Reliability','Status'].map(h => (
                  <th key={h} className="text-left px-4 py-3 text-xs text-gray-500 font-medium">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {suppliers.map((s, i) => (
                <tr key={s.id} className="border-b border-surface-border/50 hover:bg-surface-elevated/50 transition-colors">
                  <td className="px-4 py-3">
                    <div className="font-medium text-white">{s.name}</div>
                    <div className="text-gray-600 text-xs">{s.email}</div>
                  </td>
                  <td className="px-4 py-3"><span className="badge badge-info">{CAT_LABELS[s.category]}</span></td>
                  <td className="px-4 py-3 text-gray-400 font-mono text-xs">{s.contactNumber}</td>
                  <td className="px-4 py-3 font-mono text-gray-300">{s.avgDeliveryHours}h</td>
                  <td className="px-4 py-3">{reliabilityBar(s.reliabilityScore)}</td>
                  <td className="px-4 py-3">
                    <span className={clsx('badge', s.isActive ? 'badge-fresh' : 'badge-danger')}>
                      {s.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={showAdd} onClose={() => setShowAdd(false)} title="Add Supplier">
        {error && <div className="mb-3 p-3 rounded-lg bg-red-900/30 border border-red-700/40 text-red-400 text-sm">{error}</div>}
        <div className="space-y-3">
          {[['Name','name'],['Contact Number','contactNumber'],['Email','email']].map(([label, key]) => (
            <div key={key}>
              <label className="block text-xs text-gray-400 mb-1">{label}</label>
              <input className="input text-sm" value={form[key]} onChange={e => setForm({...form, [key]: e.target.value})} />
            </div>
          ))}
          <div>
            <label className="block text-xs text-gray-400 mb-1">Category</label>
            <select className="input text-sm" value={form.category} onChange={e => setForm({...form, category: e.target.value})}>
              {CATEGORIES.map(c => <option key={c} value={c}>{CAT_LABELS[c]}</option>)}
            </select>
          </div>
          <div className="flex gap-2 pt-2">
            <button onClick={() => setShowAdd(false)} className="btn-ghost flex-1 text-sm">Cancel</button>
            <button onClick={handleAdd} disabled={saving} className="btn-primary flex-1 text-sm">{saving ? 'Adding...' : 'Add'}</button>
          </div>
        </div>
      </Modal>
    </div>
  )
}

// ── Admin Customers ───────────────────────────────────────────
export function AdminCustomers() {
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(true)
  const [deleteId, setDeleteId] = useState(null)

  const load = () => { userApi.getAll().then(setCustomers).finally(() => setLoading(false)) }
  useEffect(() => { load() }, [])

  const handleDelete = async () => {
    try { await userApi.deleteUser(deleteId); setDeleteId(null); load() }
    catch (e) { alert(e.response?.data?.message || 'Delete failed') }
  }

  return (
    <div className="p-8 animate-in">
      <SectionHeader title="Customers" subtitle={`${customers.length} registered customers`} />

      {loading ? <Spinner /> : customers.length === 0 ? (
        <EmptyState icon={Users} title="No customers yet" />
      ) : (
        <div className="card overflow-hidden p-0">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-surface-border">
                {['User','Wallet Balance','Registered','Last Login',''].map(h => (
                  <th key={h} className="text-left px-4 py-3 text-xs text-gray-500 font-medium">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {customers.map(u => (
                <tr key={u.id} className="border-b border-surface-border/50 hover:bg-surface-elevated/50 transition-colors">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2.5">
                      <div className="w-7 h-7 rounded-full bg-brand-900 border border-brand-700/40 flex items-center justify-center text-xs font-bold text-brand-300">
                        {u.username[0].toUpperCase()}
                      </div>
                      <span className="font-medium text-white">{u.username}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3 font-mono text-brand-400">₹{u.walletBalance.toFixed(2)}</td>
                  <td className="px-4 py-3 text-gray-500 text-xs">{u.createdAt?.slice(0,10) || '—'}</td>
                  <td className="px-4 py-3 text-gray-500 text-xs">{u.lastLoginAt?.slice(0,10) || 'Never'}</td>
                  <td className="px-4 py-3">
                    <button onClick={() => setDeleteId(u.id)} className="p-1.5 rounded hover:bg-danger/10 text-gray-500 hover:text-danger transition-colors">
                      <Trash2 size={13}/>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={!!deleteId} onClose={() => setDeleteId(null)} title="Delete Customer">
        <p className="text-gray-400 mb-5">Delete this customer? All their orders will also be deleted. This cannot be undone.</p>
        <div className="flex gap-2">
          <button onClick={() => setDeleteId(null)} className="btn-ghost flex-1">Cancel</button>
          <button onClick={handleDelete} className="btn-danger flex-1">Delete</button>
        </div>
      </Modal>
    </div>
  )
}

export default AdminSuppliers
