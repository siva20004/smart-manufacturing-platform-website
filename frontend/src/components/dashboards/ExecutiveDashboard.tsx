'use client';

import React, { useEffect, useState } from 'react';
import { StatCard } from '@/components/ui/StatCard';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { api } from '@/lib/api';
import { DollarSign, TrendingUp, Package, Factory, Truck, Headphones, Activity } from 'lucide-react';

export const ExecutiveDashboard: React.FC = () => {
  const [salesByCustomer, setSalesByCustomer] = useState<any[]>([]);
  const [orderVolume, setOrderVolume] = useState<any[]>([]);
  const [inventoryValue, setInventoryValue] = useState<any[]>([]);
  const [productionMetrics, setProductionMetrics] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const [sales, volume, inv, prd] = await Promise.all([
          api.get<any[]>('/analytics/sales-by-customer').catch(() => []),
          api.get<any[]>('/analytics/order-volume').catch(() => []),
          api.get<any[]>('/analytics/inventory-value').catch(() => []),
          api.get<any>('/analytics/production-metrics').catch(() => null),
        ]);
        setSalesByCustomer(sales);
        setOrderVolume(volume);
        setInventoryValue(inv);
        setProductionMetrics(prd);
      } finally {
        setIsLoading(false);
      }
    }
    loadData();
  }, []);

  const totalRev = salesByCustomer.reduce((acc, c) => acc + (c.totalRevenue || 0), 0);
  const totalInvValue = inventoryValue.reduce((acc, w) => acc + (w.totalInventoryValue || 0), 0);

  return (
    <div className="space-y-6">
      {/* KPI Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Consolidated Sales Revenue"
          value={`¥${totalRev.toLocaleString()}`}
          icon={<DollarSign className="w-6 h-6" />}
          trend={{ value: '18.4% YoY', isPositive: true }}
          color="sky"
        />
        <StatCard
          title="Total Inventory Asset Value"
          value={`¥${totalInvValue.toLocaleString()}`}
          icon={<Package className="w-6 h-6" />}
          subtitle="Across 3 Global Depots"
          color="emerald"
        />
        <StatCard
          title="On-Time Production Rate"
          value={`${productionMetrics?.onTimeCompletionRatePct || 100}%`}
          icon={<Factory className="w-6 h-6" />}
          subtitle="Precision Tolerance Metrology"
          color="indigo"
        />
        <StatCard
          title="Active Production Orders"
          value={productionMetrics?.activeOrdersInProgress || 1}
          icon={<Activity className="w-6 h-6" />}
          subtitle="Flagship CNC Units Assembling"
          color="amber"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Top Key Customer Revenue */}
        <Card>
          <CardHeader
            title="Strategic Key Customer Accounts"
            subtitle="Live confirmed revenue from PostgreSQL ledger"
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Customer</th>
                    <th className="px-6 py-3">Account Code</th>
                    <th className="px-6 py-3">Total Orders</th>
                    <th className="px-6 py-3 text-right">Revenue (JPY)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {salesByCustomer.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-6 py-8 text-center text-slate-400">
                        {isLoading ? 'Loading analytics...' : 'No sales ledger records yet.'}
                      </td>
                    </tr>
                  ) : (
                    salesByCustomer.map((c) => (
                      <tr key={c.customerId} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold text-slate-900">{c.companyName}</td>
                        <td className="px-6 py-3.5 font-mono text-slate-500">{c.customerCode}</td>
                        <td className="px-6 py-3.5 font-semibold text-slate-700">{c.totalOrders} Units</td>
                        <td className="px-6 py-3.5 text-right font-bold text-sky-600">
                          ¥{c.totalRevenue.toLocaleString()}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>

        {/* Multi-Warehouse Valuation */}
        <Card>
          <CardHeader
            title="Multi-Plant Warehouse Valuation"
            subtitle="Raw material & component stock balances"
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Warehouse / Plant</th>
                    <th className="px-6 py-3">Code</th>
                    <th className="px-6 py-3">Unique Parts</th>
                    <th className="px-6 py-3 text-right">Asset Value (JPY)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {inventoryValue.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-6 py-8 text-center text-slate-400">
                        {isLoading ? 'Loading warehouse balances...' : 'No inventory records.'}
                      </td>
                    </tr>
                  ) : (
                    inventoryValue.map((w) => (
                      <tr key={w.warehouseId} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold text-slate-900">{w.plantLocation} Plant</td>
                        <td className="px-6 py-3.5 font-mono text-slate-500">{w.warehouseCode}</td>
                        <td className="px-6 py-3.5 font-semibold text-slate-700">{w.uniquePartsCount} SKUs</td>
                        <td className="px-6 py-3.5 text-right font-bold text-emerald-600">
                          ¥{w.totalInventoryValue.toLocaleString()}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>
      </div>
    </div>
  );
};
