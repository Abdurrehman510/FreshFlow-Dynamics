import React, { useEffect, useState } from 'react'
import { BarChart2, TrendingDown } from 'lucide-react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { analyticsApi } from '../../api/client'
import { SectionHeader, Spinner, EmptyState, StatCard } from '../shared/UIComponents'

export default function AdminWastage() {
  const [report, setReport]   = useState(null)
  const [loading, setLoading] = useState(true)
  const [from, setFrom] = useState(() => {
    const d = new Date(); d.setDate(d.getDate() - 30)
    return d.toISOString().slice(0,10)
  })
  const [to, setTo] = useState(() => new Date().toISOString().slice(0,10))

  const load = () => {
    setLoading(true)
    analyticsApi.getWastage(from, to).then(setReport).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const chartData = report?.topWasters?.map(w => ({
    name: w.productName.length > 16 ? w.productName.slice(0,16)+'…' : w.productName,
    value: w.valueLost,
  })) || []

  const REDS = ['#f85149','#da3633','#b62324','#8e1519','#6e1010']

  return (
    <div className="p-8 animate-in space-y-8">
      <SectionHeader title="Wastage Analytics" subtitle="Track revenue lost to food expiry and spoilage" />

      {/* Date range picker */}
      <div className="card flex items-end gap-4 flex-wrap">
        <div>
          <label className="block text-xs text-gray-400 mb-1.5">From</label>
          <input type="date" className="input text-sm w-44" value={from} onChange={e => setFrom(e.target.value)} />
        </div>
        <div>
          <label className="block text-xs text-gray-400 mb-1.5">To</label>
          <input type="date" className="input text-sm w-44" value={to} onChange={e => setTo(e.target.value)} />
        </div>
        <button onClick={load} className="btn-primary text-sm">Generate Report</button>
      </div>

      {loading ? <Spinner /> : !report ? null : (
        <>
          {/* KPI row */}
          <div className="grid grid-cols-3 gap-4">
            <StatCard icon={TrendingDown} label="Total Value Lost" value={`₹${report.totalValueLost.toFixed(2)}`} color="danger" />
            <StatCard icon={BarChart2}    label="Units Wasted"     value={report.totalUnitsWasted} color="warning" />
            <StatCard icon={BarChart2}    label="Wastage Incidents" value={report.incidentCount}   color="info" />
          </div>

          {/* Bar chart */}
          {chartData.length === 0 ? (
            <EmptyState icon={BarChart2} title="No wastage this period" desc="Great job! No wastage records found for the selected date range." />
          ) : (
            <div className="card">
              <SectionHeader title="Top 5 Wastage Products" subtitle="Value lost (₹) in selected period" />
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={chartData} layout="vertical" margin={{ left: 10, right: 30 }}>
                  <XAxis type="number" tick={{ fontSize:11, fill:'#9ca3af' }} tickLine={false} axisLine={false} tickFormatter={v=>`₹${v}`} />
                  <YAxis type="category" dataKey="name" width={130} tick={{ fontSize:12, fill:'#e6edf3' }} tickLine={false} axisLine={false} />
                  <Tooltip contentStyle={{ background:'#161b22', border:'1px solid #30363d', borderRadius:8, color:'#e6edf3', fontSize:12 }}
                    formatter={v => [`₹${v.toFixed(2)}`, 'Value Lost']} />
                  <Bar dataKey="value" radius={[0,4,4,0]} maxBarSize={32}>
                    {chartData.map((_, i) => <Cell key={i} fill={REDS[i % REDS.length]} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}

          {/* Table */}
          {report.topWasters.length > 0 && (
            <div className="card overflow-hidden p-0">
              <div className="px-5 py-4 border-b border-surface-border">
                <h3 className="font-display font-semibold text-white">Detailed Breakdown</h3>
              </div>
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-surface-border">
                    <th className="text-left px-4 py-3 text-xs text-gray-500 font-medium">Rank</th>
                    <th className="text-left px-4 py-3 text-xs text-gray-500 font-medium">Product</th>
                    <th className="text-right px-4 py-3 text-xs text-gray-500 font-medium">Value Lost</th>
                    <th className="text-right px-4 py-3 text-xs text-gray-500 font-medium">% of Total</th>
                  </tr>
                </thead>
                <tbody>
                  {report.topWasters.map((w, i) => (
                    <tr key={i} className="border-b border-surface-border/40 hover:bg-surface-elevated/40 transition-colors">
                      <td className="px-4 py-3 font-mono text-gray-500">#{i+1}</td>
                      <td className="px-4 py-3 text-white font-medium">{w.productName}</td>
                      <td className="px-4 py-3 text-right font-mono text-danger">₹{w.valueLost.toFixed(2)}</td>
                      <td className="px-4 py-3 text-right text-gray-400 text-xs">
                        {report.totalValueLost > 0 ? ((w.valueLost / report.totalValueLost) * 100).toFixed(1) : 0}%
                      </td>
                    </tr>
                  ))}
                  <tr className="bg-surface-elevated/50">
                    <td colSpan={2} className="px-4 py-3 text-gray-400 font-medium text-sm">Total</td>
                    <td className="px-4 py-3 text-right font-mono text-white font-bold">₹{report.totalValueLost.toFixed(2)}</td>
                    <td className="px-4 py-3 text-right text-xs text-gray-500">100%</td>
                  </tr>
                </tbody>
              </table>
            </div>
          )}

          {/* Recommendation box */}
          {report.totalValueLost > 1000 && (
            <div className="card border-warning/40 bg-yellow-900/10">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-lg bg-yellow-900/50 border border-yellow-700/40 flex items-center justify-center shrink-0">
                  <TrendingDown size={15} className="text-warning" />
                </div>
                <div>
                  <div className="font-semibold text-white mb-1">High Wastage Alert</div>
                  <p className="text-gray-400 text-sm">
                    You've lost <span className="text-danger font-medium">₹{report.totalValueLost.toFixed(2)}</span> to wastage
                    in the selected period. Consider enabling more aggressive expiry discounts,
                    reducing order quantities for high-wastage products, or reviewing your
                    demand forecast for these SKUs.
                  </p>
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
