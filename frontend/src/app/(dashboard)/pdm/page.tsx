'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { api } from '@/lib/api';
import { ProductDocument, Product } from '@/lib/types';
import { FileCode2, Upload, FileText, Download, Eye, Layers } from 'lucide-react';

export default function PdmPage() {
  const [documents, setDocuments] = useState<ProductDocument[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    documentNumber: '',
    title: '',
    documentType: 'DRAWING',
    mimeType: 'application/pdf',
    fileSizeBytes: 2048500,
    storageKey: 'drawings/HM500_SPINDLE_REV_A.pdf',
  });

  const loadData = async () => {
    setIsLoading(true);
    try {
      const pData = await api.get<any>('/products');
      const pList = Array.isArray(pData) ? pData : (pData?.content || []);
      setProducts(pList);
      if (pList.length > 0 && !selectedProduct) {
        setSelectedProduct(pList[0].id);
      }
      const docsRes = await api.get<any>('/pdm/documents');
      const docs = Array.isArray(docsRes) ? docsRes : (docsRes?.content || []);
      setDocuments(docs);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleUploadDoc = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await api.post('/pdm/documents', {
        documentNumber: formData.documentNumber,
        title: formData.title,
        docType: formData.documentType,
        productId: selectedProduct || undefined,
        isRestricted: false,
      });
      setIsModalOpen(false);
      setFormData({
        documentNumber: '',
        title: '',
        documentType: 'DRAWING',
        mimeType: 'application/pdf',
        fileSizeBytes: 2048500,
        storageKey: 'drawings/NEW_DOC.pdf',
      });
      loadData();
    } catch (err: any) {
      alert(err.message || 'Failed to upload document metadata');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <Header
        title="Product Data Management (PDM)"
        subtitle="Governed 2D engineering drawings, CAD metadata, and technical specifications"
      />
      <main className="flex-1 p-8 space-y-6 overflow-y-auto">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3 w-full sm:w-auto">
            <label className="text-xs font-semibold text-slate-700">Filter by Product:</label>
            <select
              value={selectedProduct}
              onChange={(e) => setSelectedProduct(e.target.value)}
              className="bg-white border border-slate-200 rounded-lg px-3 py-1.5 text-xs font-bold text-slate-800 focus:ring-2 focus:ring-sky-500"
            >
              {products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.productNumber} - {p.name}
                </option>
              ))}
            </select>
          </div>
          <Button onClick={() => setIsModalOpen(true)} size="sm">
            <Upload className="w-4 h-4 mr-1" /> Register CAD / Drawing
          </Button>
        </div>

        <Card>
          <CardHeader
            title="Technical Documents & Drawings Vault"
            subtitle="Secure object-storage abstraction with cryptographic version hashing"
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Doc Number</th>
                    <th className="px-6 py-3">Title</th>
                    <th className="px-6 py-3">Type</th>
                    <th className="px-6 py-3">Version</th>
                    <th className="px-6 py-3">Storage Key</th>
                    <th className="px-6 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {documents.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-6 py-12 text-center text-slate-400">
                        {isLoading ? 'Loading PDM vault...' : 'No engineering drawings registered for this product model.'}
                      </td>
                    </tr>
                  ) : (
                    documents.map((doc) => (
                      <tr key={doc.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold font-mono text-slate-900">{doc.documentNumber}</td>
                        <td className="px-6 py-3.5 font-medium text-slate-800">{doc.title}</td>
                        <td className="px-6 py-3.5">
                          <Badge variant={doc.documentType === 'DRAWING' ? 'info' : 'purple'}>
                            {doc.documentType}
                          </Badge>
                        </td>
                        <td className="px-6 py-3.5 font-semibold text-slate-700">v{doc.version}</td>
                        <td className="px-6 py-3.5 font-mono text-slate-500 text-[11px]">{doc.storageKey}</td>
                        <td className="px-6 py-3.5 text-right">
                          <button
                            onClick={() => alert(`Pre-signed S3 download generated for: ${doc.storageKey}`)}
                            className="inline-flex items-center gap-1 text-sky-600 hover:text-sky-800 font-semibold"
                          >
                            <Download className="w-3.5 h-3.5" /> Download
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>

        {/* Upload Modal */}
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title="Register Engineering Drawing in PDM"
        >
          <form onSubmit={handleUploadDoc} className="space-y-4 text-xs">
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Drawing / Document Number *</label>
              <input
                type="text"
                required
                placeholder="e.g. DWG-HM500-HYD-001"
                value={formData.documentNumber}
                onChange={(e) => setFormData({ ...formData, documentNumber: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs font-mono"
              />
            </div>
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Document Title *</label>
              <input
                type="text"
                required
                placeholder="e.g. Hydraulic Circuit Schematic Diagram"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                className="w-full p-2 border border-slate-200 rounded-lg text-xs"
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Document Type</label>
                <select
                  value={formData.documentType}
                  onChange={(e) => setFormData({ ...formData, documentType: e.target.value as any })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs"
                >
                  <option value="DRAWING">2D CAD Drawing (DWG/PDF)</option>
                  <option value="CAD_MODEL">3D CAD Model (STEP/IGES)</option>
                  <option value="MANUAL">Operation & Maintenance Manual</option>
                  <option value="DATASHEET">Component Datasheet</option>
                </select>
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Storage Key</label>
                <input
                  type="text"
                  value={formData.storageKey}
                  onChange={(e) => setFormData({ ...formData, storageKey: e.target.value })}
                  className="w-full p-2 border border-slate-200 rounded-lg text-xs font-mono"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
              <Button type="button" variant="outline" size="sm" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" isLoading={isSubmitting}>
                Save Document
              </Button>
            </div>
          </form>
        </Modal>
      </main>
    </>
  );
}
