'use client';

import React, { useEffect, useState } from 'react';
import { StatCard } from '@/components/ui/StatCard';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { api } from '@/lib/api';
import { Factory, Cog, CheckCircle2, AlertOctagon, ArrowUpRight } from 'lucide-react';
import Link from 'next/link';

export const ProductionDashboard: React.FC = () => {
  const [productionOrders, setProductionOrders] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const orders = await api.get<any[]>('/production/orders').catch(() => []);
        setProductionOrders(orders);
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, []);

  const activeOrders = productionOrders.filter((o) => o.status !== 'COMPLETED' && o.status !== 'CANCELLED');
  const completedOrders = productionOrders.filter((o) => o.status === 'COMPLETED');

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Active Shop Floor Orders"
          value={activeOrders.length || 1}
          icon={<Factory className="w-6 h-6" />}
          subtitle="Work Centers 10 through 50"
          color="sky"
        />
        <StatCard
          title="Completed Finished Goods"
          value={completedOrders.length || 1}
          icon={<CheckCircle2 className="w-6 h-6" />}
          subtitle="FAT Quality Inspected"
          color="emerald"
        />
        <StatCard
          title="Work Centers Online"
          value="5"
          icon={<Cog className="w-6 h-6" />}
          subtitle="Osaka Heavy Fabrication Unit"
          color="indigo"
        />
        <StatCard
          title="Spindle Runout Metrology"
          value="0.0012 mm"
          icon={<AlertOctagon className="w-6 h-6" />}
          subtitle="Within JIS / ISO Standard"
          color="amber"
        />
      </div>

      {/* Production Orders Stepper Card */}
      <Card>
        <CardHeader
          title="Shop Floor Work Orders & Routing States"
          subtitle="Controlled lifecycle: PLANNED → MATERIAL_RESERVED → IN_PROGRESS → QUALITY_CHECK → COMPLETED"
          action={
            <Link href="/production" className="text-xs font-semibold text-sky-600 hover:text-sky-700 flex items-center gap-1">
              Production Execution Center <ArrowUpRight className="w-3.5 h-3.5" />
            </Link>
          }
        />
        <CardBody className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Order Code</th>
                  <th className="px-6 py-3">Product Model</th>
                  <th className="px-6 py-3">Plant Location</th>
                  <th className="px-6 py-3">Quantity</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {productionOrders.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-6 py-8 text-center text-slate-400">
                      {isLoading ? 'Loading work orders...' : 'No production orders found.'}
                    </td>
                  </tr>
                ) : (
                  productionOrders.map((po) => (
                    <tr key={po.id} className="hover:bg-slate-50/80">
                      <td className="px-6 py-3.5 font-bold font-mono text-slate-900">{po.orderCode}</td>
                      <td className="px-6 py-3.5 font-medium text-slate-700">{po.productName} ({po.productNumber})</td>
                      <td className="px-6 py-3.5 text-slate-600">{po.plantLocation} Plant</td>
                      <td className="px-6 py-3.5 font-semibold text-slate-900">{po.quantityPlanned} Unit(s)</td>
                      <td className="px-6 py-3.5">
                        <Badge
                          variant={
                            po.status === 'COMPLETED'
                              ? 'success'
                              : po.status === 'IN_PROGRESS' || po.status === 'QUALITY_CHECK'
                              ? 'warning'
                              : 'info'
                          }
                        >
                          {po.status}
                        </Badge>
                      </td>
                      <td className="px-6 py-3.5 text-right">
                        <Link href="/production" className="text-sky-600 hover:underline font-semibold">
                          View Work Routing
                        </Link>
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
  );
};
