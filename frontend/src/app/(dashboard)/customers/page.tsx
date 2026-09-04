'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { api } from '@/lib/api';
import { Customer } from '@/lib/types';
import { Users, Plus, Building2, Mail, Phone, MapPin, Search } from 'lucide-react';

export default function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Form State
  const [formData, setFormData] = useState({
    customerCode: '',
    companyName: '',
    industry: 'Automotive & Heavy Industry',
    country: 'Japan',
    creditLimit: 50000000,
    paymentTerms: 'NET_60',
    contactName: '',
    contactEmail: '',
    phone: '',
    address: '',
  });

  const loadCustomers = async () => {
    setIsLoading(true);
    try {
      const data = await api.get<any>('/customers');
      const list = Array.isArray(data) ? data : (data?.content || []);
      setCustomers(list);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  const handleCreateCustomer = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await api.post('/customers', formData);
      setIsModalOpen(false);
      setFormData({
        customerCode: '',
        companyName: '',
        industry: 'Automotive & Heavy Industry',
        country: 'Japan',
        creditLimit: 50000000,
        paymentTerms: 'NET_60',
        contactName: '',
        contactEmail: '',
        phone: '',
        address: '',
      });
      loadCustomers();
    } catch (err: any) {
      alert(err.message || 'Failed to create customer');
    } finally {
      setIsSubmitting(false);
    }
  };

  const filtered = customers.filter(
    (c) =>
      c.companyName.toLowerCase().includes(search.toLowerCase()) ||
      c.customerCode.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <>
      <Header
        title="Customer Enterprise Directory"
        subtitle="Manage global machinery client accounts, credit limits, and contact profiles"
      />
      <main className="flex-1 p-8 space-y-6 overflow-y-auto">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="relative w-full sm:w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by company or customer code..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>
          <Button onClick={() => setIsModalOpen(true)} size="sm">
            <Plus className="w-4 h-4 mr-1" /> Add Enterprise Account
          </Button>
        </div>

        <Card>
          <CardHeader
            title="Registered Machinery Customers"
            subtitle="Tier-1 automotive, aerospace, and precision engineering accounts"
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Account Code</th>
                    <th className="px-6 py-3">Company Name</th>
                    <th className="px-6 py-3">Country / Region</th>
                    <th className="px-6 py-3">Status</th>
                    <th className="px-6 py-3">Payment Terms</th>
                    <th className="px-6 py-3 text-right">Credit Limit</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {filtered.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-6 py-12 text-center text-slate-400">
                        {isLoading ? 'Loading customers...' : 'No customer records match your query.'}
                      </td>
                    </tr>
                  ) : (
                    filtered.map((c) => (
                      <tr key={c.id} className="hover:bg-slate-50/80 transition-colors">
                        <td className="px-6 py-3.5 font-bold font-mono text-slate-900">{c.customerCode}</td>
                        <td className="px-6 py-3.5">
                          <div className="font-bold text-slate-900">{c.companyName}</div>
                          {c.contactEmail && (
                            <div className="text-[11px] text-slate-400 flex items-center gap-1 mt-0.5">
                              <Mail className="w-3 h-3" /> {c.contactEmail}
                            </div>
                          )}
                        </td>
                        <td className="px-6 py-3.5 text-slate-600">{c.country}</td>
                        <td className="px-6 py-3.5">
                          <Badge variant={c.status === 'ACTIVE' ? 'success' : 'warning'}>{c.status}</Badge>
                        </td>
                        <td className="px-6 py-3.5 text-slate-600">{c.paymentTerms || 'NET_30'}</td>
                        <td className="px-6 py-3.5 text-right font-bold text-slate-900">
                          ¥{(c.creditLimit || 0).toLocaleString()}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>

        {/* Create Customer Modal */}
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title="Create New Enterprise Customer"
        >
          <form onSubmit={handleCreateCustomer} className="space-y-4 text-xs">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Customer Code *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. CUST-HONDA"
                  value={formData.customerCode}
                  onChange={(e) => setFormData({ ...formData, customerCode: e.target.value })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Company Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Honda Motor Co., Ltd."
                  value={formData.companyName}
                  onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Country</label>
                <input
                  type="text"
                  value={formData.country}
                  onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Payment Terms</label>
                <select
                  value={formData.paymentTerms}
                  onChange={(e) => setFormData({ ...formData, paymentTerms: e.target.value })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                >
                  <option value="NET_30">NET 30</option>
                  <option value="NET_60">NET 60</option>
                  <option value="LC_SIGHT">Letter of Credit (LC)</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Credit Limit (JPY)</label>
                <input
                  type="number"
                  value={formData.creditLimit}
                  onChange={(e) => setFormData({ ...formData, creditLimit: Number(e.target.value) })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Contact Email</label>
                <input
                  type="email"
                  placeholder="purchasing@client.jp"
                  value={formData.contactEmail}
                  onChange={(e) => setFormData({ ...formData, contactEmail: e.target.value })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
              <Button type="button" variant="outline" size="sm" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" isLoading={isSubmitting}>
                Save Customer
              </Button>
            </div>
          </form>
        </Modal>
      </main>
    </>
  );
}
