'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { api } from '@/lib/api';
import { Product } from '@/lib/types';
import { Box, Plus, Layers, Cpu, CheckCircle } from 'lucide-react';

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    productNumber: '',
    name: '',
    description: '',
    category: 'Horizontal Machining Center',
    standardCost: 15000000,
    listPrice: 25000000,
  });

  const loadProducts = async () => {
    setIsLoading(true);
    try {
      const data = await api.get<any>('/products');
      const list = Array.isArray(data) ? data : (data?.content || []);
      setProducts(list);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const handleCreateProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await api.post('/products', formData);
      setIsModalOpen(false);
      setFormData({
        productNumber: '',
        name: '',
        description: '',
        category: 'Horizontal Machining Center',
        standardCost: 15000000,
        listPrice: 25000000,
      });
      loadProducts();
    } catch (err: any) {
      alert(err.message || 'Failed to create product');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <Header
        title="Product Engineering Catalog"
        subtitle="Flagship CNC Horizontal Machining Centers & Precision Machine Configurations"
      />
      <main className="flex-1 p-8 space-y-6 overflow-y-auto">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-800">Master Product Catalog</h3>
            <p className="text-xs text-slate-500">Governed under strict PDM revision control</p>
          </div>
          <Button onClick={() => setIsModalOpen(true)} size="sm">
            <Plus className="w-4 h-4 mr-1" /> Create Machine Model
          </Button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {products.map((p) => (
            <Card key={p.id} className="hover:border-sky-300 transition-colors">
              <CardBody className="p-6 space-y-4">
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-xl bg-sky-50 border border-sky-200 text-sky-600 flex items-center justify-center">
                      <Box className="w-6 h-6" />
                    </div>
                    <div>
                      <h4 className="text-base font-bold text-slate-900">{p.name}</h4>
                      <p className="text-xs font-mono font-bold text-sky-600">{p.productNumber}</p>
                    </div>
                  </div>
                  <Badge variant={p.status === 'ACTIVE' ? 'success' : 'neutral'}>{p.status}</Badge>
                </div>

                <p className="text-xs text-slate-600 leading-relaxed">
                  {p.description || 'Precision 5-Axis Horizontal Machining Center engineered for automotive powertrain casting & aerospace titanium components.'}
                </p>

                <div className="grid grid-cols-2 gap-2 pt-3 border-t border-slate-100 text-xs">
                  <div>
                    <span className="text-slate-400 block">Standard Unit Cost</span>
                    <span className="font-bold text-slate-900">¥{(p.standardCost || 0).toLocaleString()}</span>
                  </div>
                  <div>
                    <span className="text-slate-400 block">List Price (MSRP)</span>
                    <span className="font-bold text-emerald-600">¥{(p.listPrice || 0).toLocaleString()}</span>
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
          title="Register New CNC Product Model"
        >
          <form onSubmit={handleCreateProduct} className="space-y-4 text-xs">
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Product Model Code *</label>
              <input
                type="text"
                required
                placeholder="e.g. SMW-HM-900"
                value={formData.productNumber}
                onChange={(e) => setFormData({ ...formData, productNumber: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs font-mono"
              />
            </div>
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Product Model Name *</label>
              <input
                type="text"
                required
                placeholder="e.g. Horizontal Machining Center 900 Heavy Duty"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs"
              />
            </div>
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Description</label>
              <textarea
                rows={3}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                placeholder="Technical specifications and workpiece envelope..."
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Standard Cost (JPY)</label>
                <input
                  type="number"
                  value={formData.standardCost}
                  onChange={(e) => setFormData({ ...formData, standardCost: Number(e.target.value) })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">List Price (JPY)</label>
                <input
                  type="number"
                  value={formData.listPrice}
                  onChange={(e) => setFormData({ ...formData, listPrice: Number(e.target.value) })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
              <Button type="button" variant="outline" size="sm" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" isLoading={isSubmitting}>
                Save Model
              </Button>
            </div>
          </form>
        </Modal>
      </main>
    </>
  );
}
