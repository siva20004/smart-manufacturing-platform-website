'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth, DEMO_USERS } from '@/lib/auth';
import { RoleName } from '@/lib/types';
import { Building2, ShieldCheck, ArrowRight, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('Password@123');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      await login(username, password);
      window.location.href = '/dashboard';
    } catch (err: any) {
      setError(err.message || 'Invalid username or password');
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuickLogin = async (role: RoleName) => {
    const demo = DEMO_USERS[role];
    if (demo) {
      setError(null);
      setIsLoading(true);
      try {
        await login(demo.username, 'Password@123');
        window.location.href = '/dashboard';
      } catch (err: any) {
        setError(err.message || 'Quick login failed');
      } finally {
        setIsLoading(false);
      }
    }
  };

  return (
    <div className="min-h-screen flex bg-slate-950 text-white">
      {/* Left Column - Branding and Precision Machinery Philosophy */}
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between p-12 bg-radial from-slate-900 to-slate-950 border-r border-slate-800 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-96 h-96 bg-sky-500/10 rounded-full blur-3xl" />
        <div className="relative z-10">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center text-white font-bold shadow-lg shadow-sky-500/30">
              <Building2 className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-tight text-white">SIVA MACHINE WORKS</h1>
              <p className="text-xs text-sky-400 font-semibold tracking-widest uppercase">Smart Manufacturing Platform</p>
            </div>
          </div>
          <div className="mt-16">
            <span className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-sky-950/60 border border-sky-800/80 text-sky-300">
              <ShieldCheck className="w-3.5 h-3.5" /> High-Precision Japanese Machinery Systems
            </span>
            <h2 className="text-3xl font-extrabold mt-4 leading-tight tracking-tight">
              Enterprise Digital Monolith for CNC, eBOM, mBOM & Shop Floor Execution.
            </h2>
            <p className="text-sm text-slate-400 mt-4 leading-relaxed max-w-lg">
              Unified digital control for flagship horizontal machining centers (SMW-HM-500 & SMW-HM-700), multi-plant inventory, MRP procurement, and closed-loop metrology.
            </p>
          </div>
        </div>

        {/* Japanese Quality Commitment */}
        <div className="relative z-10 border-t border-slate-800/80 pt-6">
          <p className="text-xs text-slate-400 font-mono">
            Plants: Osaka Precision Zone | Nagoya Port Heavy Depot | Penang Hub
          </p>
          <p className="text-[11px] text-slate-500 mt-1">© 2026 Siva Machine Works Co., Ltd. All rights reserved.</p>
        </div>
      </div>

      {/* Right Column - Login & Quick Role Switcher */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
        <div className="w-full max-w-md space-y-8">
          <div>
            <h2 className="text-2xl font-bold tracking-tight text-white">Sign in to Platform</h2>
            <p className="text-sm text-slate-400 mt-1">Enter your credentials or choose a quick test role profile</p>
          </div>

          {error && (
            <div className="p-3 rounded-lg bg-rose-950/50 border border-rose-800 text-rose-300 text-xs font-medium">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Username / Account ID</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-sky-500 transition-all placeholder:text-slate-600"
                placeholder="e.g. admin, sales_user, eng_user"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-sky-500 transition-all"
              />
            </div>
            <Button type="submit" className="w-full py-2.5" isLoading={isLoading}>
              Sign In <ArrowRight className="w-4 h-4 ml-1" />
            </Button>
          </form>

          {/* Role Quick-Switcher */}
          <div className="border-t border-slate-800/80 pt-6">
            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
              One-Click Role Demonstration Login
            </p>
            <div className="grid grid-cols-2 gap-2">
              {(Object.keys(DEMO_USERS) as RoleName[]).map((role) => {
                const info = DEMO_USERS[role];
                return (
                  <button
                    key={role}
                    type="button"
                    onClick={() => handleQuickLogin(role)}
                    disabled={isLoading}
                    className="flex flex-col text-left p-2.5 rounded-lg bg-slate-900 hover:bg-slate-800/80 border border-slate-800 transition-colors group cursor-pointer"
                  >
                    <div className="flex items-center justify-between w-full">
                      <span className="text-xs font-bold text-sky-400 group-hover:text-sky-300">{role}</span>
                      <ArrowRight className="w-3 h-3 text-slate-600 group-hover:text-sky-400 transition-colors" />
                    </div>
                    <span className="text-[11px] text-slate-400 truncate mt-0.5">{info.label}</span>
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
