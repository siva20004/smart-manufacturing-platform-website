'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { api } from '@/lib/api';
import { PurchaseOrder, PurchaseRequest, Supplier } from '@/lib/types';
import { Truck, Plus, CheckCircle, PackageCheck, AlertCircle, FileText, Sparkles, Check, X, ShieldAlert } from 'lucide-react';

interface QuotationExtraction {
  id: string;
  docFileName: string;
  rawText: string;
  supplierId: string | null;
  supplierName: string;
  quotationNumber: string;
  partNumber: string;
  quantity: number;
  unitPrice: number;
  currency: string;
  deliveryDate: string;
  paymentTerms: string;
  confidenceScore: number;
  status: 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED';
  validationWarnings?: string;
  reviewerUsername?: string;
  reviewedAt?: string;
  reviewNotes?: string;
  generatedPoId?: string;
  generatedPoCode?: string;
  createdAt: string;
}

export default function ProcurementPage() {
  const [activeTab, setActiveTab] = useState<'orders' | 'requests' | 'quotations' | 'suppliers'>('quotations');
  const [purchaseOrders, setPurchaseOrders] = useState<PurchaseOrder[]>([]);
  const [purchaseRequests, setPurchaseRequests] = useState<PurchaseRequest[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [quotations, setQuotations] = useState<QuotationExtraction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isExtractModalOpen, setIsExtractModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Review modal state
  const [selectedQuote, setSelectedQuote] = useState<QuotationExtraction | null>(null);
  const [reviewSupplierId, setReviewSupplierId] = useState('');
  const [reviewPartNumber, setReviewPartNumber] = useState('');
  const [reviewQuantity, setReviewQuantity] = useState(10);
  const [reviewUnitPrice, setReviewUnitPrice] = useState(185000);
  const [reviewDeliveryDate, setReviewDeliveryDate] = useState('2026-10-15');
  const [reviewPaymentTerms, setReviewPaymentTerms] = useState('Net 30 Days');
  const [reviewNotes, setReviewNotes] = useState('');

  // Upload State
  const [uploadFileName, setUploadFileName] = useState('Yuken_Hydraulic_Quote_2026.pdf');
  const [uploadText, setUploadText] = useState(`OFFICIAL SUPPLIER QUOTATION
Supplier: Yuken Kogyo Co., Ltd.
Quotation No: QUO-YK-2026-901
Date: 2026-09-02
Valid Until: 2026-10-15

Item Description: High Pressure Axial Piston Pump
Part No: PUMP-HP-75
Quantity: 10
Unit Price: ¥185,000
Total Amount: ¥1,850,000

Delivery Date: 2026-10-15
Payment Terms: Net 30 Days`);

  const [formData, setFormData] = useState({
    prCode: 'PR-2026-AUTO-01',
    partNumber: 'PUMP-HP-75',
    description: 'Variable Displacement Piston Pump 75cc',
    quantityRequested: 10,
    estimatedUnitPrice: 185000,
    uom: 'EA',
  });

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [pos, prs, sups, quotes] = await Promise.all([
        api.get<any>('/procurement/orders').catch(() => []),
        api.get<any>('/procurement/requests').catch(() => []),
        api.get<any>('/procurement/suppliers').catch(() => []),
        api.get<any>('/procurement/quotations').catch(() => []),
      ]);
      setPurchaseOrders(Array.isArray(pos) ? pos : (pos?.content || []));
      setPurchaseRequests(Array.isArray(prs) ? prs : (prs?.content || []));
      setSuppliers(Array.isArray(sups) ? sups : (sups?.content || []));
      setQuotations(Array.isArray(quotes) ? quotes : (quotes?.content || []));
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreatePr = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await api.post('/procurement/requests', {
        prCode: formData.prCode,
        sourceType: 'MANUAL',
        items: [
          {
            itemSeq: 1,
            partNumber: formData.partNumber,
            description: formData.description,
            quantityRequested: formData.quantityRequested,
            uom: formData.uom,
            estimatedUnitPrice: formData.estimatedUnitPrice,
          },
        ],
      });
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to create PR');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleApprovePr = async (id: string) => {
    try {
      await api.put(`/procurement/requests/${id}/approve`);
      alert('Purchase Request Approved by Management!');
      loadData();
    } catch (err: any) {
      alert(err.message || 'Approval failed');
    }
  };

  const handleUploadExtract = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await api.post('/procurement/quotations/upload', {
        docFileName: uploadFileName,
        quotationText: uploadText,
      });
      setIsExtractModalOpen(false);
      loadData();
      alert('Quotation extracted! Placed in mandatory Human Review Queue.');
    } catch (err: any) {
      alert(err.message || 'Quotation extraction failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const openReviewModal = (q: QuotationExtraction) => {
    setSelectedQuote(q);
    setReviewSupplierId(q.supplierId || (suppliers[0]?.id || ''));
    setReviewPartNumber(q.partNumber || '');
    setReviewQuantity(q.quantity || 1);
    setReviewUnitPrice(q.unitPrice || 0);
    setReviewDeliveryDate(q.deliveryDate || '2026-10-15');
    setReviewPaymentTerms(q.paymentTerms || 'Net 30 Days');
    setReviewNotes('Quotation verified against master supply agreement.');
  };

  const handleReviewApprove = async () => {
    if (!selectedQuote) return;
    setIsSubmitting(true);
    try {
      await api.post(`/procurement/quotations/${selectedQuote.id}/review`, {
        action: 'APPROVE',
        confirmedSupplierId: reviewSupplierId,
        confirmedPartNumber: reviewPartNumber,
        confirmedQuantity: Number(reviewQuantity),
        confirmedUnitPrice: Number(reviewUnitPrice),
        confirmedDeliveryDate: reviewDeliveryDate,
        confirmedPaymentTerms: reviewPaymentTerms,
        reviewNotes: reviewNotes,
      });
      setSelectedQuote(null);
      loadData();
      alert('Quotation Approved! Draft Purchase Order successfully created.');
    } catch (err: any) {
      alert(err.message || 'Review approval failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReviewReject = async () => {
    if (!selectedQuote) return;
    setIsSubmitting(true);
    try {
      await api.post(`/procurement/quotations/${selectedQuote.id}/review`, {
        action: 'REJECT',
        reviewNotes: reviewNotes || 'Quotation rejected by procurement team.',
      });
      setSelectedQuote(null);
      loadData();
      alert('Quotation Rejected.');
    } catch (err: any) {
      alert(err.message || 'Rejection failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col">
      <Header />
      <main className="flex-1 p-8 max-w-7xl mx-auto w-full">
        {/* Page Header */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-3">
              <Truck className="h-7 w-7 text-amber-400" />
              Procurement & Supply Chain Management
            </h1>
            <p className="text-sm text-slate-400 mt-1">
              AI-assisted quotation extraction, purchase requisition approval workflows, purchase orders, and multi-depot goods receipts.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Button
              variant="secondary"
              onClick={() => setIsExtractModalOpen(true)}
              className="bg-purple-900/40 border border-purple-500/30 text-purple-200 hover:bg-purple-800/40"
            >
              <Sparkles className="h-4 w-4 mr-2 text-purple-400" />
              AI Extract Quotation
            </Button>
            <Button onClick={() => setIsModalOpen(true)} className="bg-amber-600 hover:bg-amber-500 text-white">
              <Plus className="h-4 w-4 mr-2" />
              New Purchase Requisition
            </Button>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="flex border-b border-slate-800 mb-6 gap-2">
          <button
            onClick={() => setActiveTab('quotations')}
            className={`px-4 py-2.5 text-sm font-medium border-b-2 transition-all flex items-center gap-2 ${
              activeTab === 'quotations'
                ? 'border-purple-500 text-purple-400 bg-purple-950/20'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Sparkles className="h-4 w-4" />
            AI Quotations Review Queue ({quotations.filter((q) => q.status === 'PENDING_REVIEW').length})
          </button>
          <button
            onClick={() => setActiveTab('orders')}
            className={`px-4 py-2.5 text-sm font-medium border-b-2 transition-all flex items-center gap-2 ${
              activeTab === 'orders'
                ? 'border-amber-500 text-amber-400 bg-amber-950/20'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Truck className="h-4 w-4" />
            Purchase Orders ({purchaseOrders.length})
          </button>
          <button
            onClick={() => setActiveTab('requests')}
            className={`px-4 py-2.5 text-sm font-medium border-b-2 transition-all flex items-center gap-2 ${
              activeTab === 'requests'
                ? 'border-amber-500 text-amber-400 bg-amber-950/20'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <PackageCheck className="h-4 w-4" />
            Purchase Requisitions ({purchaseRequests.length})
          </button>
          <button
            onClick={() => setActiveTab('suppliers')}
            className={`px-4 py-2.5 text-sm font-medium border-b-2 transition-all flex items-center gap-2 ${
              activeTab === 'suppliers'
                ? 'border-amber-500 text-amber-400 bg-amber-950/20'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            Certified Suppliers ({suppliers.length})
          </button>
        </div>

        {/* Tab 1: AI Quotation Review Queue */}
        {activeTab === 'quotations' && (
          <div className="space-y-6">
            <div className="bg-purple-950/20 border border-purple-500/30 rounded-xl p-4 flex items-start gap-3">
              <ShieldAlert className="h-5 w-5 text-purple-400 shrink-0 mt-0.5" />
              <div className="text-xs text-purple-200 leading-relaxed">
                <span className="font-semibold text-purple-300">Mandatory Human Review Policy: </span>
                The AI extraction pipeline parses supplier PDFs/quotations into structured candidate data and evaluates confidence scores. In accordance with platform governance, the AI is <strong>strictly prohibited from auto-submitting or auto-approving purchase orders</strong>. A human procurement officer must review, edit, and approve records before a Draft PO is issued.
              </div>
            </div>

            <Card>
              <CardHeader title="Supplier Quotations Ingestion & Review Queue" />
              <CardBody className="p-0">
                {quotations.length === 0 ? (
                  <div className="p-12 text-center text-slate-400">
                    <FileText className="h-10 w-10 mx-auto text-slate-600 mb-3" />
                    <p className="font-medium text-slate-300">No supplier quotations in review queue</p>
                    <p className="text-xs text-slate-500 mt-1">Click 'AI Extract Quotation' above to ingest and parse a supplier document.</p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm text-slate-300">
                      <thead className="bg-slate-950/50 text-slate-400 uppercase text-xs">
                        <tr>
                          <th className="px-6 py-4">Document / Source</th>
                          <th className="px-6 py-4">Quotation #</th>
                          <th className="px-6 py-4">Supplier</th>
                          <th className="px-6 py-4">Part & Quantity</th>
                          <th className="px-6 py-4">Unit Price</th>
                          <th className="px-6 py-4">AI Confidence</th>
                          <th className="px-6 py-4">Status</th>
                          <th className="px-6 py-4 text-right">Action</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-800">
                        {quotations.map((q) => (
                          <tr key={q.id} className="hover:bg-slate-800/40">
                            <td className="px-6 py-4 font-mono text-xs text-white">
                              <div className="flex items-center gap-2">
                                <FileText className="h-4 w-4 text-purple-400" />
                                {q.docFileName}
                              </div>
                            </td>
                            <td className="px-6 py-4 font-mono text-xs text-slate-300">{q.quotationNumber || 'N/A'}</td>
                            <td className="px-6 py-4 font-medium text-white">{q.supplierName || 'Unknown Vendor'}</td>
                            <td className="px-6 py-4">
                              <span className="font-mono text-amber-300">{q.partNumber}</span>
                              <span className="text-slate-400 text-xs ml-2">({q.quantity} units)</span>
                            </td>
                            <td className="px-6 py-4 font-medium text-emerald-400">
                              ¥{(q.unitPrice || 0).toLocaleString()}
                            </td>
                            <td className="px-6 py-4">
                              <div className="flex items-center gap-2">
                                <div className="w-16 bg-slate-800 rounded-full h-2 overflow-hidden">
                                  <div
                                    className={`h-full ${
                                      q.confidenceScore >= 0.8
                                        ? 'bg-emerald-500'
                                        : q.confidenceScore >= 0.5
                                        ? 'bg-amber-500'
                                        : 'bg-rose-500'
                                    }`}
                                    style={{ width: `${Math.min(100, q.confidenceScore * 100)}%` }}
                                  />
                                </div>
                                <span className="text-xs font-mono">{Math.round(q.confidenceScore * 100)}%</span>
                              </div>
                            </td>
                            <td className="px-6 py-4">
                              {q.status === 'PENDING_REVIEW' && (
                                <Badge variant="warning">
                                  Pending Review
                                </Badge>
                              )}
                              {q.status === 'APPROVED' && (
                                <Badge variant="success">
                                  Approved (Draft PO)
                                </Badge>
                              )}
                              {q.status === 'REJECTED' && (
                                <Badge variant="danger">
                                  Rejected
                                </Badge>
                              )}
                            </td>
                            <td className="px-6 py-4 text-right">
                              {q.status === 'PENDING_REVIEW' ? (
                                <Button
                                  size="sm"
                                  onClick={() => openReviewModal(q)}
                                  className="bg-purple-600 hover:bg-purple-500 text-white text-xs"
                                >
                                  Review & Approve
                                </Button>
                              ) : q.generatedPoCode ? (
                                <span className="font-mono text-xs text-emerald-400">PO: {q.generatedPoCode}</span>
                              ) : (
                                <span className="text-xs text-slate-500">Completed</span>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </CardBody>
            </Card>
          </div>
        )}

        {/* Tab 2: Purchase Orders */}
        {activeTab === 'orders' && (
          <Card>
            <CardHeader title="Active Purchase Orders" />
            <CardBody className="p-0">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm text-slate-300">
                  <thead className="bg-slate-950/50 text-slate-400 uppercase text-xs">
                    <tr>
                      <th className="px-6 py-4">PO Code</th>
                      <th className="px-6 py-4">Supplier</th>
                      <th className="px-6 py-4">Order Date</th>
                      <th className="px-6 py-4">Expected Delivery</th>
                      <th className="px-6 py-4">Total Amount</th>
                      <th className="px-6 py-4">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800">
                    {purchaseOrders.map((po) => (
                      <tr key={po.id} className="hover:bg-slate-800/40">
                        <td className="px-6 py-4 font-mono font-medium text-white">{po.poCode}</td>
                        <td className="px-6 py-4 font-medium text-amber-300">{po.supplierName || 'N/A'}</td>
                        <td className="px-6 py-4">{po.orderDate}</td>
                        <td className="px-6 py-4">{po.expectedDeliveryDate || 'Standard (30d)'}</td>
                        <td className="px-6 py-4 font-medium text-emerald-400">¥{(po.totalAmount || 0).toLocaleString()}</td>
                        <td className="px-6 py-4">
                          <Badge variant={po.status === 'RECEIVED' ? 'success' : 'info'}>{po.status}</Badge>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </CardBody>
          </Card>
        )}

        {/* Tab 3: Purchase Requisitions */}
        {activeTab === 'requests' && (
          <Card>
            <CardHeader title="Purchase Requisitions (PR)" />
            <CardBody className="p-0">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm text-slate-300">
                  <thead className="bg-slate-950/50 text-slate-400 uppercase text-xs">
                    <tr>
                      <th className="px-6 py-4">PR Code</th>
                      <th className="px-6 py-4">Created Date</th>
                      <th className="px-6 py-4">Items Count</th>
                      <th className="px-6 py-4">Total Est. Amount</th>
                      <th className="px-6 py-4">Status</th>
                      <th className="px-6 py-4 text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800">
                    {purchaseRequests.map((pr) => (
                      <tr key={pr.id} className="hover:bg-slate-800/40">
                        <td className="px-6 py-4 font-mono font-medium text-white">{pr.prCode}</td>
                        <td className="px-6 py-4">{pr.createdAt?.split('T')[0]}</td>
                        <td className="px-6 py-4">{pr.items?.length || 0} items</td>
                        <td className="px-6 py-4 font-medium text-emerald-400">¥{(pr.totalEstimatedAmount || 0).toLocaleString()}</td>
                        <td className="px-6 py-4">
                          <Badge variant={pr.status === 'APPROVED' ? 'success' : 'warning'}>{pr.status}</Badge>
                        </td>
                        <td className="px-6 py-4 text-right">
                          {pr.status === 'DRAFT' && (
                            <Button size="sm" onClick={() => handleApprovePr(pr.id)} className="bg-emerald-600 hover:bg-emerald-500 text-xs">
                              <CheckCircle className="h-3.5 w-3.5 mr-1" />
                              Approve PR
                            </Button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </CardBody>
          </Card>
        )}

        {/* Tab 4: Certified Suppliers */}
        {activeTab === 'suppliers' && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {suppliers.map((s) => (
              <Card key={s.id} className="border-slate-800 hover:border-slate-700">
                <CardHeader title={s.companyName} subtitle={s.supplierCode} />
                <CardBody className="text-xs space-y-2 text-slate-400">
                  <div>Country: {s.country}</div>
                  <div>Email: {s.contactEmail || 'scm@vendor.co.jp'}</div>
                  <div>Phone: {s.phone || '+81-6-6448-1111'}</div>
                  <div className="pt-2 border-t border-slate-800 flex justify-between items-center">
                    <span>Payment Terms:</span>
                    <span className="text-slate-200">{s.paymentTerms || 'Net 30 Days'}</span>
                  </div>
                </CardBody>
              </Card>
            ))}
          </div>
        )}

        {/* Modal: New Purchase Requisition */}
        {isModalOpen && (
          <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Create New Purchase Requisition">
            <form onSubmit={handleCreatePr} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Requisition Code</label>
                <input
                  type="text"
                  required
                  value={formData.prCode}
                  onChange={(e) => setFormData({ ...formData, prCode: e.target.value })}
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Part Number</label>
                  <input
                    type="text"
                    required
                    value={formData.partNumber}
                    onChange={(e) => setFormData({ ...formData, partNumber: e.target.value })}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white font-mono"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Quantity Requested</label>
                  <input
                    type="number"
                    required
                    min={1}
                    value={formData.quantityRequested}
                    onChange={(e) => setFormData({ ...formData, quantityRequested: Number(e.target.value) })}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Description</label>
                <input
                  type="text"
                  required
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white"
                />
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
                <Button variant="ghost" type="button" onClick={() => setIsModalOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isSubmitting} className="bg-amber-600 hover:bg-amber-500 text-white">
                  {isSubmitting ? 'Creating...' : 'Submit Requisition'}
                </Button>
              </div>
            </form>
          </Modal>
        )}

        {/* Modal: AI Upload & Ingest Quotation */}
        {isExtractModalOpen && (
          <Modal isOpen={isExtractModalOpen} onClose={() => setIsExtractModalOpen(false)} title="AI Supplier Quotation Extractor">
            <form onSubmit={handleUploadExtract} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Source Document File Name</label>
                <input
                  type="text"
                  required
                  value={uploadFileName}
                  onChange={(e) => setUploadFileName(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Quotation Text / OCR Payload</label>
                <textarea
                  rows={8}
                  required
                  value={uploadText}
                  onChange={(e) => setUploadText(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg p-3 text-xs font-mono text-slate-200"
                />
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
                <Button variant="ghost" type="button" onClick={() => setIsExtractModalOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isSubmitting} className="bg-purple-600 hover:bg-purple-500 text-white">
                  <Sparkles className="h-4 w-4 mr-2" />
                  {isSubmitting ? 'Extracting...' : 'Extract & Queue for Review'}
                </Button>
              </div>
            </form>
          </Modal>
        )}

        {/* Modal: Human Review & Draft PO Generation */}
        {selectedQuote && (
          <Modal isOpen={!!selectedQuote} onClose={() => setSelectedQuote(null)} title="Human Review: Supplier Quotation Approval">
            <div className="space-y-4">
              <div className="bg-slate-900/80 p-3 rounded-lg border border-slate-800 text-xs font-mono text-slate-400">
                <div className="text-slate-300 font-semibold mb-1">Original Document Text:</div>
                <div className="max-h-24 overflow-y-auto whitespace-pre-wrap">{selectedQuote.rawText}</div>
              </div>

              {selectedQuote.validationWarnings && (
                <div className="bg-amber-950/30 border border-amber-500/30 p-2.5 rounded-lg text-xs text-amber-300 flex items-center gap-2">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{selectedQuote.validationWarnings}</span>
                </div>
              )}

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Confirm Supplier</label>
                  <select
                    value={reviewSupplierId}
                    onChange={(e) => setReviewSupplierId(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white"
                  >
                    {suppliers.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.companyName}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Confirm Part Number</label>
                  <input
                    type="text"
                    required
                    value={reviewPartNumber}
                    onChange={(e) => setReviewPartNumber(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white font-mono"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Confirmed Quantity</label>
                  <input
                    type="number"
                    required
                    min={1}
                    value={reviewQuantity}
                    onChange={(e) => setReviewQuantity(Number(e.target.value))}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Confirmed Unit Price (JPY)</label>
                  <input
                    type="number"
                    required
                    min={1}
                    value={reviewUnitPrice}
                    onChange={(e) => setReviewUnitPrice(Number(e.target.value))}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Delivery Date</label>
                  <input
                    type="date"
                    required
                    value={reviewDeliveryDate}
                    onChange={(e) => setReviewDeliveryDate(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Payment Terms</label>
                  <input
                    type="text"
                    value={reviewPaymentTerms}
                    onChange={(e) => setReviewPaymentTerms(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">Reviewer Comments / Audit Notes</label>
                <input
                  type="text"
                  value={reviewNotes}
                  onChange={(e) => setReviewNotes(e.target.value)}
                  placeholder="e.g. Verified against master supply agreement."
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white"
                />
              </div>

              <div className="flex justify-between items-center pt-4 border-t border-slate-800">
                <Button
                  variant="ghost"
                  type="button"
                  onClick={handleReviewReject}
                  disabled={isSubmitting}
                  className="text-rose-400 hover:bg-rose-950/40 hover:text-rose-300"
                >
                  <X className="h-4 w-4 mr-1.5" />
                  Reject Quotation
                </Button>

                <div className="flex gap-2">
                  <Button variant="ghost" type="button" onClick={() => setSelectedQuote(null)}>
                    Cancel
                  </Button>
                  <Button
                    type="button"
                    onClick={handleReviewApprove}
                    disabled={isSubmitting}
                    className="bg-emerald-600 hover:bg-emerald-500 text-white"
                  >
                    <Check className="h-4 w-4 mr-1.5" />
                    {isSubmitting ? 'Processing...' : 'Approve & Issue Draft PO'}
                  </Button>
                </div>
              </div>
            </div>
          </Modal>
        )}
      </main>
    </div>
  );
}
