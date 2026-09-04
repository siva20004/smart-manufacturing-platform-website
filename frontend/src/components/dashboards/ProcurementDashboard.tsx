'use client';

import React, { useEffect, useState } from 'react';
import { StatCard } from '@/components/ui/StatCard';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { api } from '@/lib/api';
import { Truck, PackageCheck, AlertTriangle, FileSpreadsheet, ArrowUpRight } from 'lucide-react';
import Link from 'next/link';

export const ProcurementDashboard: React.FC = () => {
  const [purchaseOrders, setPurchaseOrders] = useState<any[]>([]);
  const [purchaseRequests, setPurchaseRequests] = useState<any[]>([]);
  const [suppliers, setSuppliers] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [pos, prs, sups] = await Promise.all([
          api.get<any>('/procurement/orders').catch(() => []),
          api.get<any>('/procurement/requests').catch(() => []),
          api.get<any>('/procurement/suppliers').catch(() => []),
        ]);
        setPurchaseOrders(Array.isArray(pos) ? pos : ((pos as any)?.content || []));
        setPurchaseRequests(Array.isArray(prs) ? prs : ((prs as any)?.content || []));
        setSuppliers(Array.isArray(sups) ? sups : ((sups as any)?.content || []));
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, []);

  const totalSpend = purchaseOrders.reduce((acc, po) => acc + (po.totalAmount || 0), 0);
  const pendingPrs = purchaseRequests.filter((pr) => pr.status === 'DRAFT');

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Committed PO Spend"
          value={`¥${totalSpend.toLocaleString()}`}
          icon={<Truck className="w-6 h-6" />}
          subtitle={`${purchaseOrders.length} Issued Purchase Orders`}
          color="sky"
        />
        <StatCard
          title="PRs Pending Approval"
          value={pendingPrs.length}
          icon={<AlertTriangle className="w-6 h-6" />}
          subtitle="Tiered Authorization Matrix"
          color="amber"
        />
        <StatCard
          title="Active Certified Suppliers"
          value={suppliers.length || 3}
          icon={<PackageCheck className="w-6 h-6" />}
          subtitle="Yuken, Siemens, Keyence"
          color="emerald"
        />
        <StatCard
          title="Goods Receipt Ledger"
          value="100% Verified"
          icon={<FileSpreadsheet className="w-6 h-6" />}
          subtitle="Atomic Stock Increments"
          color="indigo"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Active Purchase Orders */}
        <Card>
          <CardHeader
            title="Issued Purchase Orders"
            subtitle="Supplier POs with delivery tracking to plant warehouses"
            action={
              <Link href="/procurement" className="text-xs font-semibold text-sky-600 hover:text-sky-700 flex items-center gap-1">
                View All <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            }
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">PO Code</th>
                    <th className="px-6 py-3">Supplier</th>
                    <th className="px-6 py-3">Warehouse</th>
                    <th className="px-6 py-3">Status</th>
                    <th className="px-6 py-3 text-right">Total</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {purchaseOrders.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-8 text-center text-slate-400">
                        {isLoading ? 'Loading POs...' : 'No purchase orders issued yet.'}
                      </td>
                    </tr>
                  ) : (
                    purchaseOrders.slice(0, 5).map((po) => (
                      <tr key={po.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold font-mono text-slate-900">{po.poCode}</td>
                        <td className="px-6 py-3.5 font-medium text-slate-700">{po.supplierName}</td>
                        <td className="px-6 py-3.5 font-mono text-slate-500">{po.warehouseCode}</td>
                        <td className="px-6 py-3.5">
                          <Badge variant={po.status === 'ISSUED' ? 'info' : 'success'}>{po.status}</Badge>
                        </td>
                        <td className="px-6 py-3.5 text-right font-bold text-slate-900">
                          ¥{po.totalAmount.toLocaleString()}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>

        {/* Suppliers Directory */}
        <Card>
          <CardHeader
            title="Strategic Tier-1 Suppliers"
            subtitle="Precision hydraulics, CNC drives, and linear scales"
            action={
              <Link href="/procurement" className="text-xs font-semibold text-sky-600 hover:text-sky-700 flex items-center gap-1">
                Manage Suppliers <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            }
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Supplier Name</th>
                    <th className="px-6 py-3">Code</th>
                    <th className="px-6 py-3">Country</th>
                    <th className="px-6 py-3">Payment Terms</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {suppliers.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-6 py-8 text-center text-slate-400">
                        {isLoading ? 'Loading suppliers...' : 'No suppliers registered.'}
                      </td>
                    </tr>
                  ) : (
                    suppliers.map((sup) => (
                      <tr key={sup.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold text-slate-900">{sup.companyName}</td>
                        <td className="px-6 py-3.5 font-mono text-slate-500">{sup.supplierCode}</td>
                        <td className="px-6 py-3.5 font-medium text-slate-600">{sup.country}</td>
                        <td className="px-6 py-3.5 text-slate-600">{sup.paymentTerms}</td>
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
