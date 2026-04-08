import React, { useEffect, useState } from 'react'
import { TrendingUp, TrendingDown, Minus, AlertTriangle, Package } from 'lucide-react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { analyticsApi } from '../../api/client'
import { SectionHeader, Spinner, EmptyState } from '../shared/UIComponents'
import clsx from 'clsx'

const TREND_CONFIG = {
  INCREASING: { icon: TrendingUp,   color: 'text-brand-400',  bg: 'bg-brand-900/40 border-brand-700/30',  label: 'Growing' },
  STABLE:     { icon: Minus,        color: 'text-info',        bg: 'bg-blue-900/30 border-blue-700/30',    label: 'Stable' },
  DECREASING: { icon: TrendingDown, color: 'text-danger',      bg: 'bg-red-900/30 border-red-700/30',      label: 'Declining' },
}

export default function AdminForecast() {
  const [forecasts, setForecasts] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('ALL') // ALL | REORDER | SPIKE

  useEffect(() => {
    analyticsApi.getForecast().then(setForecasts).finally(() => setLoading(false))
  }, [])

  const filtered = forecasts.filter(f => {
    if (filter === 'REORDER') return f.recommendedReorderQty > 0
    if (filter === 'SPIKE')   return f.hasSpikeRisk
    return true
  })

  const reorderCount = forecasts.filter(f => f.recommendedReorderQty > 0).length
  const spikeCount   = forecasts.filter(f => f.hasSpikeRisk).length

  const chartData = forecasts.slice(0, 10).map(f => ({
    name: f.productName.length > 14 ? f.productName.slice(0,14)+'…' : f.productName,
    forecast: Math.round(f.predictedNextWeek),
    reorder: f.recommendedReorderQty,
  }))

  if (loading) return <Spinner />

  return (
    <div className="p-8 animate-in space-y-8">
      <SectionHeader title="Demand Forecast" subtitle="30-day weighted moving average — next 7 days prediction" />

      {/* Summary chips */}
      <div className="flex gap-3 flex-wrap">
        {[
          { key: 'ALL',     label: `All (${forecasts.length})`,        color: 'bg-surface-card border-surface-border text-gray-300' },
          { key: 'REORDER', label: `Needs Reorder (${reorderCount})`,  color: 'bg-warning/10 border-yellow-700/40 text-warning' },
          { key: 'SPIKE',   label: `Spike Risk (${spikeCount})`,       color: 'bg-red-900/30 border-red-700/40 text-danger' },
        ].map(({ key, label, color }) => (
          <button key={key} onClick={() => setFilter(key)}
            className={clsx('px-4 py-2 rounded-full text-sm font-medium border transition-all', color,
              filter === key ? 'ring-1 ring-offset-1 ring-offset-surface ring-current' : 'opacity-70 hover:opacity-100')}>
            {label}
          </button>
        ))}
      </div>

      {/* Forecast chart */}
      <div className="card">
        <SectionHeader title="Predicted Units — Next 7 Days" subtitle="Top 10 products by forecast volume" />
        <ResponsiveContainer width="100%" height={240}>
          <BarChart data={chartData} margin={{ left: -10 }}>
            <XAxis dataKey="name" tick={{ fontSize:11, fill:'#9ca3af' }} tickLine={false} axisLine={false} angle={-15} textAnchor="end" height={45} />
            <YAxis tick={{ fontSize:11, fill:'#9ca3af' }} tickLine={false} axisLine={false} />
            <Tooltip contentStyle={{ background:'#161b22', border:'1px solid #30363d', borderRadius:8, color:'#e6edf3', fontSize:12 }}
              formatter={(v, name) => [v + ' units', name === 'forecast' ? 'Predicted' : 'Reorder Qty']} />
            <Bar dataKey="forecast" fill="#22c55e" radius={[4,4,0,0]} maxBarSize={40} />
            <Bar dataKey="reorder"  fill="#f85149" radius={[4,4,0,0]} maxBarSize={40} />
          </BarChart>
        </ResponsiveContainer>
        <div className="flex gap-4 mt-3">
          <div className="flex items-center gap-1.5 text-xs text-gray-500"><div className="w-3 h-3 rounded bg-brand-500"/> Predicted demand</div>
          <div className="flex items-center gap-1.5 text-xs text-gray-500"><div className="w-3 h-3 rounded bg-danger"/>     Reorder quantity</div>
        </div>
      </div>

      {/* Forecast table */}
      {filtered.length === 0 ? (
        <EmptyState icon={Package} title="No products match this filter" />
      ) : (
        <div className="card overflow-hidden p-0">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-surface-border">
                {['Product', 'Daily Forecast', 'Next 7 Days', 'Reorder Qty', 'Trend', 'Status'].map(h => (
                  <th key={h} className="text-left px-4 py-3 text-xs text-gray-500 font-medium">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map((f, i) => {
                const trend = TREND_CONFIG[f.trend] || TREND_CONFIG.STABLE
                const TrendIcon = trend.icon
                return (
                  <tr key={f.productId} className="border-b border-surface-border/50 hover:bg-surface-elevated/50 transition-colors">
                    <td className="px-4 py-3">
                      <div className="font-medium text-white">{f.productName}</div>
                      {f.hasSpikeRisk && <div className="text-xs text-warning mt-0.5">⚡ Spike risk detected</div>}
                    </td>
                    <td className="px-4 py-3 font-mono text-white">{f.dailyForecast.toFixed(1)} <span className="text-gray-500 text-xs">units/day</span></td>
                    <td className="px-4 py-3 font-mono text-white">{f.predictedNextWeek} <span className="text-gray-500 text-xs">units</span></td>
                    <td className="px-4 py-3">
                      {f.recommendedReorderQty > 0
                        ? <span className="badge badge-warning font-mono">{f.recommendedReorderQty} units</span>
                        : <span className="text-gray-600 text-xs">Sufficient</span>}
                    </td>
                    <td className="px-4 py-3">
                      <span className={clsx('badge border flex items-center gap-1 w-fit', trend.bg, trend.color)}>
                        <TrendIcon size={11}/> {trend.label}
                      </span>
                    </td>
                    <td className="px-4 py-3 max-w-xs">
                      <p className="text-xs text-gray-400 line-clamp-2">{f.recommendation}</p>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
