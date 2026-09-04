'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { api } from '@/lib/api';
import { InventoryStock, Warehouse } from '@/lib/types';
import { Warehouse as WarehouseIcon, PackageCheck, AlertTriangle, ArrowUpDown, Plus } from 'lucide-react';

export default function InventoryPage() {
  const [stocks, setStocks] = useState<InventoryStock[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [selectedWh, setSelectedWh] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    warehouseId: '',
    partNumber: 'PUMP-HP-75',
    quantity: 10,
    unitCost: 185000,
    uom: 'EA',
  });

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [whList, sList] = await Promise.all([
        api.get<Warehouse[]>('/inventory/warehouses'),
        api.get<InventoryStock[]>('/inventory/stocks'),
      ]);
      setWarehouses(whList);
      setStocks(sList);
      if (whList.length > 0 && !selectedWh) {
        setSelectedWh(whList[0].id);
        setFormData((prev) => ({ ...prev, warehouseId: whList[0].id }));
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

  const handleAdjustStock = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await api.post('/inventory/adjust', formData);
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Stock adjustment failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const filteredStocks = selectedWh ? stocks.filter((s) => s.warehouseId === selectedWh) : stocks;

  return (
    <>
      <Header
        title="Multi-Plant Inventory & Stock Balances"
        subtitle="Real-time on-hand, reserved, and available stock levels with transactional ledger locks"
      />
      <main className="flex-1 p-8 space-y-6 overflow-y-auto">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <label className="text-xs font-semibold text-slate-700">Filter Plant Warehouse:</label>
            <select
              value={selectedWh}
              onChange={(e) => setSelectedWh(e.target.value)}
              className="bg-white border border-slate-200 rounded-lg px-3 py-1.5 text-xs font-bold text-slate-800 focus:ring-2 focus:ring-sky-500"
            >
              <option value="">All Depots (Consolidated)</option>
              {warehouses.map((wh) => (
                <option key={wh.id} value={wh.id}>
                  {wh.name} ({wh.code}) - {wh.plantLocation}
                </option>
              ))}
            </select>
          </div>

          <Button onClick={() => setIsModalOpen(true)} size="sm">
            <Plus className="w-4 h-4 mr-1" /> Adjust Stock Balance
          </Button>
        </div>

        <Card>
          <CardHeader
            title="Component & Raw Material Stock Ledger"
            subtitle="Atomic reservations prevent double-allocation during sales confirmation"
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Part Number</th>
                    <th className="px-6 py-3">Description</th>
                    <th className="px-6 py-3">Warehouse</th>
                    <th className="px-6 py-3 text-right">Qty On Hand</th>
                    <th className="px-6 py-3 text-right">Qty Reserved</th>
                    <th className="px-6 py-3 text-right">Qty Available</th>
                    <th className="px-6 py-3 text-right">Unit Cost</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {filteredStocks.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="px-6 py-12 text-center text-slate-400">
                        {isLoading ? 'Loading inventory...' : 'No stock balance records found.'}
                      </td>
                    </tr>
                  ) : (
                    filteredStocks.map((stock) => (
                      <tr key={stock.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold font-mono text-slate-900">{stock.partNumber}</td>
                        <td className="px-6 py-3.5 font-medium text-slate-700">{stock.description || stock.partNumber}</td>
                        <td className="px-6 py-3.5 font-mono text-slate-500">{stock.warehouseCode}</td>
                        <td className="px-6 py-3.5 text-right font-bold text-slate-900">
                          {stock.qtyOnHand} {stock.uom}
                        </td>
                        <td className="px-6 py-3.5 text-right font-semibold text-amber-600">
                          {stock.qtyReserved} {stock.uom}
                        </td>
                        <td className="px-6 py-3.5 text-right font-bold text-emerald-600">
                          {stock.qtyAvailable} {stock.uom}
                        </td>
                        <td className="px-6 py-3.5 text-right font-bold text-slate-900">
                          ¥{stock.unitCost?.toLocaleString()}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>

        {/* Adjust Stock Modal */}
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title="Manual Stock Balance Adjustment"
        >
          <form onSubmit={handleAdjustStock} className="space-y-4 text-xs">
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Target Warehouse *</label>
              <select
                value={formData.warehouseId}
                onChange={(e) => setFormData({ ...formData, warehouseId: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs"
              >
                {warehouses.map((wh) => (
                  <option key={wh.id} value={wh.id}>
                    {wh.name} ({wh.code}) - {wh.plantLocation}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Part Number *</label>
              <input
                type="text"
                required
                value={formData.partNumber}
                onChange={(e) => setFormData({ ...formData, partNumber: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs font-mono"
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Quantity to Adjust *</label>
                <input
                  type="number"
                  required
                  value={formData.quantity}
                  onChange={(e) => setFormData({ ...formData, quantity: Number(e.target.value) })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Unit Cost (JPY)</label>
                <input
                  type="number"
                  value={formData.unitCost}
                  onChange={(e) => setFormData({ ...formData, unitCost: Number(e.target.value) })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
              <Button type="button" variant="outline" size="sm" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" isLoading={isSubmitting}>
                Post Inventory Transaction
              </Button>
            </div>
          </form>
        </Modal>
      </main>
    </>
  );
}
