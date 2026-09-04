'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, RoleName, AuthResponse } from './types';
import { api } from './api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  login: (username: string, password?: string) => Promise<void>;
  logout: () => void;
  hasRole: (role: RoleName) => boolean;
  hasAnyRole: (roles: RoleName[]) => boolean;
  switchDemoRole: (role: RoleName) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const DEMO_USERS: Record<RoleName, { username: string; label: string; desc: string }> = {
  ADMIN: { username: 'admin', label: 'IT Director / Admin', desc: 'Full System Control & Configuration' },
  SALES: { username: 'sales_user', label: 'Sales Executive', desc: 'Quotations, Orders & Customer Pipeline' },
  ENGINEERING: { username: 'eng_user', label: 'Chief Design Engineer', desc: 'CAD, eBOM & Drawing Governance' },
  PROCUREMENT: { username: 'proc_user', label: 'SCM Manager', desc: 'Suppliers, Purchase Orders & Goods Receipts' },
  PRODUCTION: { username: 'prod_user', label: 'Plant Operations Director', desc: 'Work Orders, Assembly & Shop Floor Routing' },
  FINANCE: { username: 'fin_user', label: 'Financial Controller', desc: 'Asset Valuation, Invoicing & Cost Variance' },
  MANAGEMENT: { username: 'mgmt_user', label: 'Managing Director (CEO)', desc: 'Executive Analytics & Governance Approvals' },
  IT_ENGINEER: { username: 'it_engineer', label: 'Systems & Cloud Engineer', desc: 'Infrastructure & Integration Pipeline' },
};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('auth_token');
    const savedUser = localStorage.getItem('auth_user');

    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_user');
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string = 'Password@123') => {
    setIsLoading(true);
    try {
      const res = await api.post<AuthResponse>('/auth/login', {
        username,
        usernameOrEmail: username,
        password,
      });

      const roles: RoleName[] = (res.roles || (res.user && res.user.roles) || ['ADMIN']).map(
        (r: string) => r.replace('ROLE_', '') as RoleName
      );

      const userObj: User = res.user || {
        id: res.username || username,
        username: res.username || username,
        email: res.email || `${username}@sivamachineworks.com`,
        firstName: username.charAt(0).toUpperCase() + username.slice(1),
        lastName: '',
        roles,
        isActive: true,
      };

      setToken(res.accessToken);
      setUser(userObj);
      localStorage.setItem('auth_token', res.accessToken);
      localStorage.setItem('auth_user', JSON.stringify(userObj));
    } finally {
      setIsLoading(false);
    }
  };

  const switchDemoRole = async (role: RoleName) => {
    const demo = DEMO_USERS[role];
    if (demo) {
      await login(demo.username, 'Password@123');
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    window.location.href = '/login';
  };

  const hasRole = (role: RoleName): boolean => {
    return user?.roles.includes(role) || user?.roles.includes('ADMIN') || false;
  };

  const hasAnyRole = (roles: RoleName[]): boolean => {
    if (!user) return false;
    if (user.roles.includes('ADMIN')) return true;
    return roles.some((r) => user.roles.includes(r));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isLoading,
        login,
        logout,
        hasRole,
        hasAnyRole,
        switchDemoRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
