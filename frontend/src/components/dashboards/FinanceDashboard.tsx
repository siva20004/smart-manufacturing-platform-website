'use client';

import React, { useEffect, useState } from 'react';
import { StatCard } from '@/components/ui/StatCard';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { api } from '@/lib/api';
import { DollarSign, Landmark, Receipt, Scale } from 'lucide-react';

export const FinanceDashboard: React.FC = () => {
  const [salesByCustomer, setSalesByCustomer] = useState<any[]>([]);
  const [inventoryValue, setInventoryValue] = useState<any[]>([]);
  const [orderVolume, setOrderVolume] = useState<any[]>([]);

  useEffect(() => {
    async function load() {
      const [sales, inv, vol] = await Promise.all([
        api.get<any[]>('/analytics/sales-by-customer').catch(() => []),
        api.get<any[]>('/analytics/inventory-value').catch(() => []),
        api.get<any[]>('/analytics/order-volume').catch(() => []),
      ]);
      setSalesByCustomer(sales);
      setInventoryValue(inv);
      setOrderVolume(vol);
    }
    load();
  }, []);

  const totalRev = salesByCustomer.reduce((acc, c) => acc + (c.totalRevenue || 0), 0);
  const totalInv = inventoryValue.reduce((acc, w) => acc + (w.totalInventoryValue || 0), 0);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Recognized Revenue"
          value={`¥${totalRev.toLocaleString()}`}
          icon={<DollarSign className="w-6 h-6" />}
          color="emerald"
        />
        <StatCard
          title="Current Stock Asset Value"
          value={`¥${totalInv.toLocaleString()}`}
          icon={<Landmark className="w-6 h-6" />}
          color="sky"
        />
        <StatCard
          title="Average Machine Deal Size"
          value="¥25,000,000"
          icon={<Receipt className="w-6 h-6" />}
          color="indigo"
        />
        <StatCard
          title="Gross Operating Margin"
          value="32.5%"
          icon={<Scale className="w-6 h-6" />}
          color="amber"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader title="Revenue Breakdown by Customer Account" subtitle="Gross invoice volume" />
          <CardBody className="p-0">
            <table className="w-full text-xs text-left">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Account</th>
                  <th className="px-6 py-3 text-right">Gross Total (JPY)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {salesByCustomer.map((c) => (
                  <tr key={c.customerId}>
                    <td className="px-6 py-3 font-medium text-slate-900">{c.companyName}</td>
                    <td className="px-6 py-3 text-right font-bold text-sky-600">¥{c.totalRevenue.toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardBody>
        </Card>

        <Card>
          <CardHeader title="Order Ledger Valuation by Status" subtitle="In-flight and booked contracts" />
          <CardBody className="p-0">
            <table className="w-full text-xs text-left">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3">Count</th>
                  <th className="px-6 py-3 text-right">Sum Total (JPY)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {orderVolume.map((v) => (
                  <tr key={v.orderStatus}>
                    <td className="px-6 py-3 font-semibold text-slate-800">{v.orderStatus}</td>
                    <td className="px-6 py-3 text-slate-600">{v.orderCount} Orders</td>
                    <td className="px-6 py-3 text-right font-bold text-emerald-600">¥{v.totalAmount.toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardBody>
        </Card>
      </div>
    </div>
  );
};
