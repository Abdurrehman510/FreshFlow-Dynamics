import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { Leaf, LogOut } from 'lucide-react'
import { useAuthStore } from '../../store/authStore'
import clsx from 'clsx'

export default function Sidebar({ links, user }) {
  const { logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = () => { logout(); navigate('/login') }

  return (
    <aside className="w-60 shrink-0 h-screen sticky top-0 flex flex-col bg-surface-card border-r border-surface-border">
      {/* Logo */}
      <div className="flex items-center gap-3 px-5 py-5 border-b border-surface-border">
        <div className="w-8 h-8 bg-brand-600 rounded-lg flex items-center justify-center shrink-0">
          <Leaf size={15} className="text-white" />
        </div>
        <div>
          <div className="font-display font-bold text-white text-sm leading-none">Perishable</div>
          <div className="text-gray-500 text-xs">Platform</div>
        </div>
      </div>

      {/* User chip */}
      <div className="px-4 py-3 border-b border-surface-border">
        <div className="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-surface-elevated">
          <div className="w-7 h-7 rounded-full bg-brand-700 flex items-center justify-center text-xs font-bold text-brand-200">
            {user?.username?.[0]?.toUpperCase() || '?'}
          </div>
          <div className="min-w-0">
            <div className="text-white text-sm font-medium truncate">{user?.username}</div>
            <div className="text-gray-500 text-xs">{user?.role?.replace('_',' ')}</div>
          </div>
        </div>
      </div>

      {/* Nav links */}
      <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
        {links.map(({ to, icon: Icon, label }) => (
          <NavLink key={to} to={to} end={to.endsWith('dashboard') || to.endsWith('shop')}
            className={({ isActive }) => clsx(
              'flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-all duration-150',
              isActive
                ? 'bg-brand-900/60 text-brand-300 font-medium border border-brand-700/30'
                : 'text-gray-400 hover:text-white hover:bg-surface-elevated'
            )}>
            <Icon size={16} />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* Logout */}
      <div className="p-3 border-t border-surface-border">
        <button onClick={handleLogout}
          className="flex items-center gap-3 px-3 py-2 w-full rounded-lg text-sm text-gray-400
                     hover:text-danger hover:bg-danger/10 transition-all duration-150">
          <LogOut size={16} />
          Sign out
        </button>
      </div>
    </aside>
  )
}
