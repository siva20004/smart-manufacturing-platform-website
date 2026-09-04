'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { api } from '@/lib/api';
import { SalesOrder, Customer, Product } from '@/lib/types';
import { ShoppingCart, Plus, CheckCircle, Clock, FileText, ArrowRight } from 'lucide-react';

export default function SalesOrdersPage() {
  const [salesOrders, setSalesOrders] = useState<SalesOrder[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    soCode: 'SO-2026-NISSAN-01',
    customerId: '',
    productId: '',
    quantity: 1,
    unitPrice: 25000000,
    plantLocation: 'Osaka',
  });

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [orders, custList, prodList] = await Promise.all([
        api.get<any>('/sales/orders').catch(() => []),
        api.get<any>('/customers').catch(() => []),
        api.get<any>('/products').catch(() => []),
      ]);
      const safeOrders = Array.isArray(orders) ? orders : (orders?.content || []);
      const safeCust = Array.isArray(custList) ? custList : (custList?.content || []);
      const safeProd = Array.isArray(prodList) ? prodList : (prodList?.content || []);
      setSalesOrders(safeOrders);
      setCustomers(safeCust);
      setProducts(safeProd);
      if (safeCust.length > 0 && !formData.customerId) {
        setFormData((prev) => ({ ...prev, customerId: safeCust[0].id }));
      }
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
      await api.post('/sales/orders', {
        soCode: formData.soCode,
        customerId: formData.customerId,
        plantLocation: formData.plantLocation,
        items: [
          {
            itemSeq: 1,
            productId: formData.productId,
            quantity: formData.quantity,
            unitPrice: formData.unitPrice,
          },
        ],
      });
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to create sales order');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmOrder = async (orderId: string) => {
    try {
      const res = await api.put<any>(`/sales/orders/${orderId}/confirm`);
      alert(`Sales order confirmed! Reserved ${res.reservedMaterials?.length || 0} BOM component items in plant warehouse.`);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Order confirmation failed');
    }
  };

  return (
    <>
      <Header
        title="Sales Orders & Demand Execution"
        subtitle="Commercial contracts with automated BOM material explosion and stock reservation"
      />
      <main className="flex-1 p-8 space-y-6 overflow-y-auto">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-800">Commercial Order Contracts</h3>
            <p className="text-xs text-slate-500">Confirming an order triggers BOM explosion & stock reservation</p>
          </div>
          <Button onClick={() => setIsModalOpen(true)} size="sm">
            <Plus className="w-4 h-4 mr-1" /> Create Sales Order
          </Button>
        </div>

        <Card>
          <CardHeader
            title="Sales Order Contracts"
            subtitle="PostgreSQL transactional ledger"
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">SO Code</th>
                    <th className="px-6 py-3">Customer Account</th>
                    <th className="px-6 py-3">Plant Assignment</th>
                    <th className="px-6 py-3">Status</th>
                    <th className="px-6 py-3 text-right">Contract Amount</th>
                    <th className="px-6 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {salesOrders.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-6 py-12 text-center text-slate-400">
                        {isLoading ? 'Loading sales orders...' : 'No sales orders registered.'}
                      </td>
                    </tr>
                  ) : (
                    salesOrders.map((so) => (
                      <tr key={so.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold font-mono text-slate-900">{so.soCode}</td>
                        <td className="px-6 py-3.5 font-medium text-slate-800">{so.customerName}</td>
                        <td className="px-6 py-3.5 text-slate-600">{so.plantLocation} Plant</td>
                        <td className="px-6 py-3.5">
                          <Badge
                            variant={
                              so.status === 'CONFIRMED' || so.status === 'READY_TO_SHIP'
                                ? 'success'
                                : 'info'
                            }
                          >
                            {so.status}
                          </Badge>
                        </td>
                        <td className="px-6 py-3.5 text-right font-bold text-slate-900">
                          ¥{so.totalAmount.toLocaleString()}
                        </td>
                        <td className="px-6 py-3.5 text-right">
                          {so.status === 'DRAFT' ? (
                            <Button onClick={() => handleConfirmOrder(so.id)} size="sm">
                              Confirm & Reserve BOM
                            </Button>
                          ) : (
                            <span className="text-emerald-600 font-bold flex items-center justify-end gap-1">
                              <CheckCircle className="w-3.5 h-3.5" /> BOM Reserved
                            </span>
                          )}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>

        {/* Modal */}
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title="Create New Sales Order Contract"
        >
          <form onSubmit={handleCreateOrder} className="space-y-4 text-xs">
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Sales Order Code *</label>
              <input
                type="text"
                required
                value={formData.soCode}
                onChange={(e) => setFormData({ ...formData, soCode: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs font-mono"
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Customer Account *</label>
                <select
                  value={formData.customerId}
                  onChange={(e) => setFormData({ ...formData, customerId: e.target.value })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                >
                  {customers.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.companyName} ({c.customerCode})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Machine Model *</label>
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
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Quantity Units *</label>
                <input
                  type="number"
                  min={1}
                  value={formData.quantity}
                  onChange={(e) => setFormData({ ...formData, quantity: Number(e.target.value) })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Unit Price (JPY)</label>
                <input
                  type="number"
                  value={formData.unitPrice}
                  onChange={(e) => setFormData({ ...formData, unitPrice: Number(e.target.value) })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
              <Button type="button" variant="outline" size="sm" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" isLoading={isSubmitting}>
                Create Sales Order
              </Button>
            </div>
          </form>
        </Modal>
      </main>
    </>
  );
}
