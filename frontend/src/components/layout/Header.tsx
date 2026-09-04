'use client';

import React from 'react';
import { useAuth, DEMO_USERS } from '@/lib/auth';
import { RoleName } from '@/lib/types';
import { Shield, Sparkles } from 'lucide-react';

export const Header: React.FC<{ title?: string; subtitle?: string }> = ({
  title = 'Platform Portal',
  subtitle,
}) => {
  const { user, switchDemoRole } = useAuth();

  return (
    <header className="h-16 bg-white border-b border-slate-200 px-8 flex items-center justify-between sticky top-0 z-30 shadow-xs">
      <div>
        <h2 className="text-base font-bold text-slate-900">{title}</h2>
        {subtitle && <p className="text-xs text-slate-500">{subtitle}</p>}
      </div>

      {/* Role Switcher Toolbar */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-lg p-1 text-xs">
          <div className="flex items-center gap-1.5 px-2 text-slate-500 font-medium">
            <Shield className="w-3.5 h-3.5 text-sky-600" />
            <span>Active Role:</span>
          </div>
          <select
            value={user?.roles?.[0] || 'ADMIN'}
            onChange={(e) => switchDemoRole(e.target.value as RoleName)}
            className="bg-white border border-slate-200 rounded-md px-2 py-1 text-xs font-semibold text-slate-800 focus:outline-none focus:ring-1 focus:ring-sky-500 cursor-pointer shadow-2xs"
          >
            {Object.entries(DEMO_USERS).map(([role, info]) => (
              <option key={role} value={role}>
                {role} ({info.label})
              </option>
            ))}
          </select>
        </div>

        <div className="hidden md:flex items-center gap-1.5 text-[11px] font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg px-2.5 py-1.5">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Backend Connected (Port 8080)</span>
        </div>
      </div>
    </header>
  );
};
