'use client';

import React from 'react';
import { useAuth } from '@/lib/auth';
import { Header } from '@/components/layout/Header';
import { ExecutiveDashboard } from '@/components/dashboards/ExecutiveDashboard';
import { SalesDashboard } from '@/components/dashboards/SalesDashboard';
import { EngineeringDashboard } from '@/components/dashboards/EngineeringDashboard';
import { ProcurementDashboard } from '@/components/dashboards/ProcurementDashboard';
import { ProductionDashboard } from '@/components/dashboards/ProductionDashboard';
import { FinanceDashboard } from '@/components/dashboards/FinanceDashboard';
import { AdminDashboard } from '@/components/dashboards/AdminDashboard';

export default function DashboardPage() {
  const { user } = useAuth();
  const activeRole = user?.roles?.[0] || 'ADMIN';

  const renderDashboard = () => {
    switch (activeRole) {
      case 'MANAGEMENT':
        return <ExecutiveDashboard />;
      case 'SALES':
        return <SalesDashboard />;
      case 'ENGINEERING':
        return <EngineeringDashboard />;
      case 'PROCUREMENT':
        return <ProcurementDashboard />;
      case 'PRODUCTION':
        return <ProductionDashboard />;
      case 'FINANCE':
        return <FinanceDashboard />;
      case 'ADMIN':
      case 'IT_ENGINEER':
      default:
        return <ExecutiveDashboard />;
    }
  };

  return (
    <>
      <Header
        title={`${activeRole} Operations Command Center`}
        subtitle="Siva Machine Works Smart Manufacturing Monolith Platform"
      />
      <main className="flex-1 p-8 overflow-y-auto">
        {renderDashboard()}
      </main>
    </>
  );
}
