'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { api } from '@/lib/api';
import { ProductionOrder, Product } from '@/lib/types';
import { Factory, Plus, CheckCircle, ArrowRight, Play, CheckSquare, Sparkles } from 'lucide-react';

export default function ProductionPage() {
  const [orders, setOrders] = useState<ProductionOrder[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    orderCode: 'PRD-2026-HM500-02',
    productId: '',
    warehouseId: 'd1111111-1111-1111-1111-111111111111',
    quantityPlanned: 1,
    plantLocation: 'Osaka',
  });

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [oList, pList] = await Promise.all([
        api.get<any>('/production/orders').catch(() => []),
        api.get<any>('/products').catch(() => []),
      ]);
      const safeOrders = Array.isArray(oList) ? oList : (oList?.content || []);
      const safeProd = Array.isArray(pList) ? pList : (pList?.content || []);
      setOrders(safeOrders);
      setProducts(safeProd);
      if (safeProd.length > 0 && !formData.productId) {
        setFormData((prev) => ({ ...prev, productId: safeProd[0].id }));
      }
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreateOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await api.post('/production/orders', formData);
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to create work order');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReserveMaterials = async (id: string) => {
    try {
      await api.put(`/production/orders/${id}/reserve-materials`);
      alert('BOM raw materials & subassemblies reserved in warehouse!');
      loadData();
    } catch (err: any) {
      alert(err.message || 'Reservation failed');
    }
  };

  const handleStartProduction = async (id: string) => {
    try {
      await api.put(`/production/orders/${id}/start`);
      alert('Production started! Work routing active across work centers.');
      loadData();
    } catch (err: any) {
      alert(err.message || 'Start failed');
    }
  };

  const handleQualityCheck = async (id: string) => {
    try {
      await api.post(`/production/orders/${id}/quality-check`, {
        inspectionType: 'FAT_FINAL_ALIGNMENT',
        spindleRunoutMm: 0.0012,
        positioningAccuracyMm: 0.0025,
        notes: 'Laser interferometer calibration passed JIS B 6338 precision standards',
      });
      alert('Quality Inspection (FAT) Passed!');
      loadData();
    } catch (err: any) {
      alert(err.message || 'Quality check failed');
    }
  };

  const handleCompleteProduction = async (id: string) => {
    try {
      await api.put(`/production/orders/${id}/complete`);
      alert('Order completed! Materials consumed and Finished Goods stock incremented (+1.0).');
      loadData();
    } catch (err: any) {
      alert(err.message || 'Completion failed');
    }
  };

  return (
    <>
      <Header
        title="Production Execution & Shop Floor Routing"
        subtitle="Work Centers, Operation Sequencing, Metrology Quality Inspection & Completion"
      />
      <main className="flex-1 p-8 space-y-6 overflow-y-auto">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-800">Controlled Lifecycle State Machine</h3>
            <p className="text-xs text-slate-500">
              PLANNED → MATERIAL_RESERVED → IN_PROGRESS → QUALITY_CHECK → COMPLETED
            </p>
          </div>
          <Button onClick={() => setIsModalOpen(true)} size="sm">
            <Plus className="w-4 h-4 mr-1" /> Schedule Production Order
          </Button>
        </div>

        <div className="space-y-6">
          {orders.map((po) => (
            <Card key={po.id}>
              <CardHeader
                title={`${po.orderCode} - ${po.productName} (${po.productNumber})`}
                subtitle={`Plant Location: ${po.plantLocation} • Qty Planned: ${po.quantityPlanned} Unit(s)`}
                action={
                  <div className="flex items-center gap-2">
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
                  </div>
                }
              />
              <CardBody className="space-y-4">
                {/* Stepper Buttons */}
                <div className="flex flex-wrap items-center gap-2 pt-2 border-b border-slate-100 pb-4">
                  {po.status === 'PLANNED' && (
                    <Button onClick={() => handleReserveMaterials(po.id)} size="sm">
                      1. Reserve Materials
                    </Button>
                  )}
                  {po.status === 'MATERIAL_RESERVED' && (
                    <Button onClick={() => handleStartProduction(po.id)} size="sm" variant="secondary">
                      <Play className="w-3.5 h-3.5 mr-1" /> 2. Start Shop Floor Assembly
                    </Button>
                  )}
                  {po.status === 'IN_PROGRESS' && (
                    <Button onClick={() => handleQualityCheck(po.id)} size="sm" variant="secondary">
                      <CheckSquare className="w-3.5 h-3.5 mr-1" /> 3. Submit Quality Inspection (FAT)
                    </Button>
                  )}
                  {po.status === 'QUALITY_CHECK' && (
                    <Button onClick={() => handleCompleteProduction(po.id)} size="sm">
                      <CheckCircle className="w-3.5 h-3.5 mr-1" /> 4. Complete & Increment FG Inventory
                    </Button>
                  )}
                  {po.status === 'COMPLETED' && (
                    <span className="text-xs font-bold text-emerald-600 flex items-center gap-1.5">
                      <CheckCircle className="w-4 h-4" /> Production Complete & Finished Goods In Stock (+1.0 Unit)
                    </span>
                  )}
                </div>

                {/* Operations Table */}
                <div>
                  <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
                    Shop Floor Operations Routing
                  </h4>
                  <div className="overflow-x-auto">
                    <table className="w-full text-xs text-left">
                      <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                        <tr>
                          <th className="px-4 py-2">Seq</th>
                          <th className="px-4 py-2">Operation Name</th>
                          <th className="px-4 py-2">Work Center</th>
                          <th className="px-4 py-2">Status</th>
                          <th className="px-4 py-2 text-right">Planned Hours</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {po.operations?.map((op) => (
                          <tr key={op.id} className="hover:bg-slate-50/80">
                            <td className="px-4 py-2 font-mono font-bold text-slate-900">OP-{op.operationSeq}</td>
                            <td className="px-4 py-2 font-medium text-slate-800">{op.operationName}</td>
                            <td className="px-4 py-2 font-mono text-sky-600">{op.workCenterCode}</td>
                            <td className="px-4 py-2">
                              <Badge
                                variant={
                                  op.status === 'COMPLETED'
                                    ? 'success'
                                    : op.status === 'IN_PROGRESS'
                                    ? 'warning'
                                    : 'neutral'
                                }
                              >
                                {op.status}
                              </Badge>
                            </td>
                            <td className="px-4 py-2 text-right font-mono font-semibold text-slate-900">
                              {op.plannedHours || 8} hrs
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </CardBody>
            </Card>
          ))}
        </div>

        {/* Modal */}
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title="Schedule New Production Order"
        >
          <form onSubmit={handleCreateOrder} className="space-y-4 text-xs">
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Order Code *</label>
              <input
                type="text"
                required
                value={formData.orderCode}
                onChange={(e) => setFormData({ ...formData, orderCode: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs font-mono"
              />
            </div>
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Target Product Model *</label>
              <select
                value={formData.productId}
                onChange={(e) => setFormData({ ...formData, productId: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs"
              >
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} ({p.productNumber})
                  </option>
                ))}
              </select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Quantity Units *</label>
                <input
                  type="number"
                  min={1}
                  value={formData.quantityPlanned}
                  onChange={(e) => setFormData({ ...formData, quantityPlanned: Number(e.target.value) })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Plant Location</label>
                <select
                  value={formData.plantLocation}
                  onChange={(e) => setFormData({ ...formData, plantLocation: e.target.value })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                >
                  <option value="Osaka">Osaka Heavy Plant</option>
                  <option value="Nagoya">Nagoya Precision Works</option>
                  <option value="Penang">Penang Assembly Hub</option>
                </select>
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
              <Button type="button" variant="outline" size="sm" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" isLoading={isSubmitting}>
                Schedule Order
              </Button>
            </div>
          </form>
        </Modal>
      </main>
    </>
  );
}
