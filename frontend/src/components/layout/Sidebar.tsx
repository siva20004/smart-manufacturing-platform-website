'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import clsx from 'clsx';
import { useAuth } from '@/lib/auth';
import { RoleName } from '@/lib/types';
import {
  LayoutDashboard,
  Users,
  Box,
  FileCode2,
  GitFork,
  Warehouse,
  ShoppingCart,
  Truck,
  Factory,
  Headphones,
  BarChart3,
  Bot,
  ShieldCheck,
  Building2,
  LogOut
} from 'lucide-react';

interface NavItem {
  name: string;
  href: string;
  icon: React.ReactNode;
  roles: RoleName[];
}

const navItems: NavItem[] = [
  { name: 'Dashboard', href: '/dashboard', icon: <LayoutDashboard className="w-4 h-4" />, roles: ['ADMIN', 'SALES', 'ENGINEERING', 'PROCUREMENT', 'PRODUCTION', 'FINANCE', 'MANAGEMENT', 'IT_ENGINEER'] },
  { name: 'Customers', href: '/customers', icon: <Users className="w-4 h-4" />, roles: ['ADMIN', 'SALES', 'MANAGEMENT', 'IT_ENGINEER'] },
  { name: 'Products', href: '/products', icon: <Box className="w-4 h-4" />, roles: ['ADMIN', 'SALES', 'ENGINEERING', 'PROCUREMENT', 'PRODUCTION', 'FINANCE', 'MANAGEMENT', 'IT_ENGINEER'] },
  { name: 'PDM Documents', href: '/pdm', icon: <FileCode2 className="w-4 h-4" />, roles: ['ADMIN', 'ENGINEERING', 'PRODUCTION', 'IT_ENGINEER'] },
  { name: 'BOM Management', href: '/bom', icon: <GitFork className="w-4 h-4" />, roles: ['ADMIN', 'ENGINEERING', 'PRODUCTION', 'PROCUREMENT', 'IT_ENGINEER'] },
  { name: 'Inventory & Stock', href: '/inventory', icon: <Warehouse className="w-4 h-4" />, roles: ['ADMIN', 'PRODUCTION', 'PROCUREMENT', 'FINANCE', 'IT_ENGINEER'] },
  { name: 'Sales Orders', href: '/sales-orders', icon: <ShoppingCart className="w-4 h-4" />, roles: ['ADMIN', 'SALES', 'MANAGEMENT', 'FINANCE', 'IT_ENGINEER'] },
  { name: 'Procurement SCM', href: '/procurement', icon: <Truck className="w-4 h-4" />, roles: ['ADMIN', 'PROCUREMENT', 'PRODUCTION', 'MANAGEMENT', 'IT_ENGINEER'] },
  { name: 'Production Shop', href: '/production', icon: <Factory className="w-4 h-4" />, roles: ['ADMIN', 'PRODUCTION', 'ENGINEERING', 'MANAGEMENT', 'IT_ENGINEER'] },
  { name: 'CRM & Service', href: '/crm', icon: <Headphones className="w-4 h-4" />, roles: ['ADMIN', 'SALES', 'PRODUCTION', 'MANAGEMENT', 'IT_ENGINEER'] },
  { name: 'Analytics', href: '/analytics', icon: <BarChart3 className="w-4 h-4" />, roles: ['ADMIN', 'MANAGEMENT', 'FINANCE', 'SALES', 'IT_ENGINEER'] },
  { name: 'AI Engineering Assistant', href: '/ai-assistant', icon: <Bot className="w-4 h-4" />, roles: ['ADMIN', 'SALES', 'ENGINEERING', 'PROCUREMENT', 'PRODUCTION', 'FINANCE', 'MANAGEMENT', 'IT_ENGINEER'] },
  { name: 'Audit Logs', href: '/audit', icon: <ShieldCheck className="w-4 h-4" />, roles: ['ADMIN', 'MANAGEMENT', 'IT_ENGINEER'] },
];

export const Sidebar: React.FC = () => {
  const pathname = usePathname();
  const { user, logout, hasAnyRole } = useAuth();

  return (
    <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col flex-shrink-0 h-screen sticky top-0 border-r border-slate-800 select-none">
      {/* Brand Header */}
      <div className="h-16 px-6 flex items-center gap-3 border-b border-slate-800">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-sky-600 to-indigo-500 flex items-center justify-center text-white font-bold shadow-md shadow-sky-500/20">
          <Building2 className="w-5 h-5" />
        </div>
        <div>
          <h1 className="text-sm font-bold text-white tracking-wide">SIVA MACHINE</h1>
          <p className="text-[10px] font-medium text-slate-400 uppercase tracking-widest">Smart Manufacturing</p>
        </div>
      </div>

      {/* Navigation Menu */}
      <div className="flex-1 overflow-y-auto px-3 py-4 space-y-1">
        <div className="px-3 pb-2 text-[10px] font-semibold uppercase tracking-wider text-slate-500">
          Platform Navigation
        </div>
        {navItems
          .filter((item) => hasAnyRole(item.roles))
          .map((item) => {
            const isActive = pathname === item.href || (item.href !== '/dashboard' && pathname.startsWith(item.href));
            return (
              <Link
                key={item.name}
                href={item.href}
                className={clsx(
                  'flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-medium transition-all',
                  isActive
                    ? 'bg-sky-600 text-white font-semibold shadow-sm'
                    : 'text-slate-400 hover:bg-slate-800/80 hover:text-slate-200'
                )}
              >
                {item.icon}
                <span>{item.name}</span>
              </Link>
            );
          })}
      </div>

      {/* User Info Footer */}
      <div className="p-4 border-t border-slate-800 bg-slate-950/40">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3 overflow-hidden">
            <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-xs font-bold text-sky-400 flex-shrink-0">
              {user?.firstName?.[0] || 'U'}{user?.lastName?.[0] || 'S'}
            </div>
            <div className="overflow-hidden">
              <p className="text-xs font-medium text-white truncate">{user?.firstName} {user?.lastName}</p>
              <p className="text-[10px] text-sky-400 font-semibold truncate">{user?.roles?.[0] || 'USER'}</p>
            </div>
          </div>
          <button
            onClick={logout}
            title="Sign Out"
            className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-800 transition-colors"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
};
