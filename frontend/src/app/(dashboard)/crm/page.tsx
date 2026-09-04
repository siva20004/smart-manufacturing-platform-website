'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { api } from '@/lib/api';
import { CrmOpportunity, ServiceRequest, Customer } from '@/lib/types';
import { Headphones, Target, Plus, CheckCircle, Wrench, AlertTriangle, Users } from 'lucide-react';

export default function CrmPage() {
  const [opportunities, setOpportunities] = useState<CrmOpportunity[]>([]);
  const [serviceRequests, setServiceRequests] = useState<ServiceRequest[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    ticketNumber: 'SRV-2026-NISSAN-01',
    customerId: '',
    title: 'Precision Laser Alignment Calibration',
    reportedIssue: 'Scheduled annual preventative maintenance on 5-axis spindle runout',
    priority: 'MEDIUM',
  });

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [opps, srvs, custList] = await Promise.all([
        api.get<any>('/crm/opportunities').catch(() => []),
        api.get<any>('/crm/service-requests').catch(() => []),
        api.get<any>('/customers').catch(() => []),
      ]);
      const safeOpps = Array.isArray(opps) ? opps : (opps?.content || []);
      const safeSrvs = Array.isArray(srvs) ? srvs : (srvs?.content || []);
      const safeCust = Array.isArray(custList) ? custList : (custList?.content || []);
      setOpportunities(safeOpps);
      setServiceRequests(safeSrvs);
      setCustomers(safeCust);
      if (safeCust.length > 0 && !formData.customerId) {
        setFormData((prev) => ({ ...prev, customerId: safeCust[0].id }));
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

  const handleCreateTicket = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await api.post('/crm/service-requests', formData);
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to create ticket');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResolveTicket = async (id: string) => {
    try {
      await api.put(`/crm/service-requests/${id}/resolve?resolutionNotes=Recalibrated spindle bearings and laser scale interferometer`);
      alert('Service ticket resolved successfully!');
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to resolve ticket');
    }
  };

  return (
    <>
      <Header
        title="CRM & Field Service Intelligence"
        subtitle="Commercial Opportunity Pipeline, Enterprise Contacts & After-Sales Field Maintenance"
      />
      <main className="flex-1 p-8 space-y-6 overflow-y-auto">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-800">After-Sales Field Service & Maintenance Tickets</h3>
            <p className="text-xs text-slate-500">24/7 CNC spindle diagnostics and preventative maintenance</p>
          </div>
          <Button onClick={() => setIsModalOpen(true)} size="sm">
            <Plus className="w-4 h-4 mr-1" /> Log Service Ticket
          </Button>
        </div>

        {/* Service Requests Table */}
        <Card>
          <CardHeader
            title="Active Field Service Tickets"
            subtitle="Field technician assignments and resolution tracking"
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Ticket #</th>
                    <th className="px-6 py-3">Customer</th>
                    <th className="px-6 py-3">Issue Summary</th>
                    <th className="px-6 py-3">Priority</th>
                    <th className="px-6 py-3">Status</th>
                    <th className="px-6 py-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {serviceRequests.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-6 py-8 text-center text-slate-400">
                        {isLoading ? 'Loading tickets...' : 'No service requests recorded.'}
                      </td>
                    </tr>
                  ) : (
                    serviceRequests.map((ticket) => (
                      <tr key={ticket.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold font-mono text-slate-900">{ticket.ticketNumber}</td>
                        <td className="px-6 py-3.5 font-medium text-slate-800">{ticket.customerName}</td>
                        <td className="px-6 py-3.5 text-slate-700">{ticket.title}</td>
                        <td className="px-6 py-3.5">
                          <Badge variant={ticket.priority === 'CRITICAL' || ticket.priority === 'HIGH' ? 'danger' : 'warning'}>
                            {ticket.priority}
                          </Badge>
                        </td>
                        <td className="px-6 py-3.5">
                          <Badge variant={ticket.status === 'RESOLVED' ? 'success' : 'info'}>{ticket.status}</Badge>
                        </td>
                        <td className="px-6 py-3.5 text-right">
                          {ticket.status !== 'RESOLVED' ? (
                            <Button onClick={() => handleResolveTicket(ticket.id)} size="sm">
                              <Wrench className="w-3.5 h-3.5 mr-1" /> Mark Resolved
                            </Button>
                          ) : (
                            <span className="text-emerald-600 font-bold flex items-center justify-end gap-1">
                              <CheckCircle className="w-3.5 h-3.5" /> Resolved
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

        {/* Opportunity Pipeline Card */}
        <Card>
          <CardHeader
            title="Strategic Opportunity Deal Pipeline"
            subtitle="High-value CNC machining expansion projects"
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Code</th>
                    <th className="px-6 py-3">Opportunity Name</th>
                    <th className="px-6 py-3">Customer</th>
                    <th className="px-6 py-3">Stage</th>
                    <th className="px-6 py-3">Probability</th>
                    <th className="px-6 py-3 text-right">Estimated Value</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {opportunities.map((opp) => (
                    <tr key={opp.id} className="hover:bg-slate-50/80">
                      <td className="px-6 py-3.5 font-bold font-mono text-slate-900">{opp.opportunityCode}</td>
                      <td className="px-6 py-3.5 font-bold text-slate-900">{opp.name}</td>
                      <td className="px-6 py-3.5 font-medium text-slate-700">{opp.customerName}</td>
                      <td className="px-6 py-3.5">
                        <Badge variant="purple">{opp.stage}</Badge>
                      </td>
                      <td className="px-6 py-3.5 font-bold text-slate-700">{opp.probabilityPct}%</td>
                      <td className="px-6 py-3.5 text-right font-bold text-emerald-600">
                        ¥{opp.estimatedValue.toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>

        {/* Modal */}
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title="Create After-Sales Service Request Ticket"
        >
          <form onSubmit={handleCreateTicket} className="space-y-4 text-xs">
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Ticket Number *</label>
              <input
                type="text"
                required
                value={formData.ticketNumber}
                onChange={(e) => setFormData({ ...formData, ticketNumber: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs font-mono"
              />
            </div>
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
              <label className="block font-semibold text-slate-700 mb-1">Issue Title *</label>
              <input
                type="text"
                required
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs"
              />
            </div>
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Reported Issue Details</label>
              <textarea
                rows={3}
                value={formData.reportedIssue}
                onChange={(e) => setFormData({ ...formData, reportedIssue: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs"
              />
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
              <Button type="button" variant="outline" size="sm" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" isLoading={isSubmitting}>
                Save Service Ticket
              </Button>
            </div>
          </form>
        </Modal>
      </main>
    </>
  );
}
