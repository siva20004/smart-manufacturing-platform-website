'use client';

import React, { useEffect, useState } from 'react';
import { StatCard } from '@/components/ui/StatCard';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { api } from '@/lib/api';
import { ShoppingCart, FileText, Target, Users, ArrowUpRight } from 'lucide-react';
import Link from 'next/link';

export const SalesDashboard: React.FC = () => {
  const [salesOrders, setSalesOrders] = useState<any[]>([]);
  const [opportunities, setOpportunities] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [orders, opps] = await Promise.all([
          api.get<any[]>('/sales/orders').catch(() => []),
          api.get<any[]>('/crm/opportunities').catch(() => []),
        ]);
        setSalesOrders(orders);
        setOpportunities(opps);
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, []);

  const totalPipeline = opportunities.reduce((acc, o) => acc + (o.estimatedValue || 0), 0);
  const confirmedOrders = salesOrders.filter((o) => o.status === 'CONFIRMED' || o.status === 'READY_TO_SHIP');

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Active Opportunities Pipeline"
          value={`¥${totalPipeline.toLocaleString()}`}
          icon={<Target className="w-6 h-6" />}
          subtitle={`${opportunities.length} Commercial Deals`}
          color="sky"
        />
        <StatCard
          title="Confirmed Sales Orders"
          value={confirmedOrders.length}
          icon={<ShoppingCart className="w-6 h-6" />}
          trend={{ value: '100% BOM Verified', isPositive: true }}
          color="emerald"
        />
        <StatCard
          title="Standard Lead Time"
          value="6-8 Weeks"
          icon={<FileText className="w-6 h-6" />}
          subtitle="SMW-HM-500 Machining Centers"
          color="indigo"
        />
        <StatCard
          title="Total Strategic Accounts"
          value="12"
          icon={<Users className="w-6 h-6" />}
          subtitle="Japan & Global Precision SCM"
          color="amber"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Sales Orders */}
        <Card>
          <CardHeader
            title="Recent Sales Orders"
            subtitle="Orders requiring stock reservation and plant scheduling"
            action={
              <Link href="/sales-orders" className="text-xs font-semibold text-sky-600 hover:text-sky-700 flex items-center gap-1">
                View All <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            }
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Order Code</th>
                    <th className="px-6 py-3">Customer</th>
                    <th className="px-6 py-3">Status</th>
                    <th className="px-6 py-3 text-right">Amount</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {salesOrders.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-6 py-8 text-center text-slate-400">
                        {isLoading ? 'Loading orders...' : 'No sales orders recorded yet.'}
                      </td>
                    </tr>
                  ) : (
                    salesOrders.slice(0, 5).map((so) => (
                      <tr key={so.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold font-mono text-slate-900">{so.soCode}</td>
                        <td className="px-6 py-3.5 font-medium text-slate-700">{so.customerName}</td>
                        <td className="px-6 py-3.5">
                          <Badge variant={so.status === 'CONFIRMED' ? 'success' : 'info'}>
                            {so.status}
                          </Badge>
                        </td>
                        <td className="px-6 py-3.5 text-right font-bold text-slate-900">
                          ¥{so.totalAmount.toLocaleString()}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>

        {/* CRM Opportunity Pipeline */}
        <Card>
          <CardHeader
            title="Deal Opportunity Stages"
            subtitle="High-probability precision machinery expansions"
            action={
              <Link href="/crm" className="text-xs font-semibold text-sky-600 hover:text-sky-700 flex items-center gap-1">
                Open CRM <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            }
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Opportunity</th>
                    <th className="px-6 py-3">Customer</th>
                    <th className="px-6 py-3">Stage</th>
                    <th className="px-6 py-3 text-right">Estimated Value</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {opportunities.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-6 py-8 text-center text-slate-400">
                        {isLoading ? 'Loading pipeline...' : 'No active CRM opportunities.'}
                      </td>
                    </tr>
                  ) : (
                    opportunities.slice(0, 5).map((opp) => (
                      <tr key={opp.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold text-slate-900">{opp.name}</td>
                        <td className="px-6 py-3.5 font-medium text-slate-600">{opp.customerName}</td>
                        <td className="px-6 py-3.5">
                          <Badge variant="purple">{opp.stage}</Badge>
                        </td>
                        <td className="px-6 py-3.5 text-right font-bold text-emerald-600">
                          ¥{opp.estimatedValue.toLocaleString()}
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
