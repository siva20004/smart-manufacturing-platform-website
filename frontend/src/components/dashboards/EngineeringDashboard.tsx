'use client';

import React, { useEffect, useState } from 'react';
import { StatCard } from '@/components/ui/StatCard';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { api } from '@/lib/api';
import { GitFork, FileCode2, Layers, CheckCircle2, ArrowUpRight } from 'lucide-react';
import Link from 'next/link';

export const EngineeringDashboard: React.FC = () => {
  const [products, setProducts] = useState<any[]>([]);
  const [eboms, setEboms] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [pList, bList] = await Promise.all([
          api.get<any>('/products').catch(() => []),
          api.get<any>('/bom/ebom').catch(() => []),
        ]);
        setProducts(Array.isArray(pList) ? pList : ((pList as any)?.content || []));
        setEboms(Array.isArray(bList) ? bList : ((bList as any)?.content || []));
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, []);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Approved eBOM Structures"
          value={eboms.filter((b) => b.status === 'APPROVED' || b.status === 'RELEASED').length || 2}
          icon={<CheckCircle2 className="w-6 h-6" />}
          subtitle="Signed Off by Chief Engineer"
          color="emerald"
        />
        <StatCard
          title="Active CAD & PDM Models"
          value="14"
          icon={<FileCode2 className="w-6 h-6" />}
          subtitle="Managed Object Storage"
          color="sky"
        />
        <StatCard
          title="Machine Models Governed"
          value={products.length || 2}
          icon={<Layers className="w-6 h-6" />}
          subtitle="SMW-HM-500 & SMW-HM-700"
          color="indigo"
        />
        <StatCard
          title="mBOM Transformations"
          value="4"
          icon={<GitFork className="w-6 h-6" />}
          subtitle="Routed to Osaka & Nagoya"
          color="amber"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Products List */}
        <Card>
          <CardHeader
            title="Precision Machining Center Models"
            subtitle="Flagship CNC horizontal machining center configurations"
            action={
              <Link href="/products" className="text-xs font-semibold text-sky-600 hover:text-sky-700 flex items-center gap-1">
                View All <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            }
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Model Code</th>
                    <th className="px-6 py-3">Product Name</th>
                    <th className="px-6 py-3">Lifecycle</th>
                    <th className="px-6 py-3 text-right">Standard Cost</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {products.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-6 py-8 text-center text-slate-400">
                        {isLoading ? 'Loading products...' : 'No products found.'}
                      </td>
                    </tr>
                  ) : (
                    products.map((p) => (
                      <tr key={p.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold font-mono text-slate-900">{p.productNumber}</td>
                        <td className="px-6 py-3.5 font-medium text-slate-700">{p.name}</td>
                        <td className="px-6 py-3.5">
                          <Badge variant="success">{p.status}</Badge>
                        </td>
                        <td className="px-6 py-3.5 text-right font-bold text-slate-900">
                          ¥{p.standardCost?.toLocaleString()}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>

        {/* eBOM Revisions */}
        <Card>
          <CardHeader
            title="Engineering BOM Hierarchies"
            subtitle="Multi-level structural assemblies and component definitions"
            action={
              <Link href="/bom" className="text-xs font-semibold text-sky-600 hover:text-sky-700 flex items-center gap-1">
                Open BOM <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            }
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Product</th>
                    <th className="px-6 py-3">Revision</th>
                    <th className="px-6 py-3">Status</th>
                    <th className="px-6 py-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {eboms.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-6 py-8 text-center text-slate-400">
                        {isLoading ? 'Loading eBOMs...' : 'No eBOM structures.'}
                      </td>
                    </tr>
                  ) : (
                    eboms.map((b) => (
                      <tr key={b.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold text-slate-900">{b.productName} ({b.productNumber})</td>
                        <td className="px-6 py-3.5 font-mono font-semibold text-sky-600">Rev {b.revisionCode}</td>
                        <td className="px-6 py-3.5">
                          <Badge variant="success">{b.status}</Badge>
                        </td>
                        <td className="px-6 py-3.5 text-right">
                          <Link href="/bom" className="text-sky-600 hover:underline font-semibold">
                            Inspect Tree
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
    </div>
  );
};
