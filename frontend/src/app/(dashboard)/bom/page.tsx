'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { api } from '@/lib/api';
import { EbomHeader, MbomHeader, EbomItem, MbomItem } from '@/lib/types';
import { GitFork, Layers, CheckCircle2, ArrowRight, RefreshCw, Cpu, Plus } from 'lucide-react';

export default function BomPage() {
  const [eboms, setEboms] = useState<EbomHeader[]>([]);
  const [selectedEbom, setSelectedEbom] = useState<EbomHeader | null>(null);
  const [selectedEbomTree, setSelectedEbomTree] = useState<EbomItem[]>([]);
  const [mboms, setMboms] = useState<MbomHeader[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isTransforming, setIsTransforming] = useState(false);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [eList, mList] = await Promise.all([
        api.get<EbomHeader[]>('/bom/ebom'),
        api.get<MbomHeader[]>('/bom/mbom'),
      ]);
      setEboms(eList);
      setMboms(mList);
      if (eList.length > 0) {
        const initial = eList[0];
        setSelectedEbom(initial);
        const tree = await api.get<EbomItem[]>(`/bom/ebom/${initial.id}/tree`);
        setSelectedEbomTree(tree);
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

  const handleSelectEbom = async (ebom: EbomHeader) => {
    setSelectedEbom(ebom);
    try {
      const tree = await api.get<EbomItem[]>(`/bom/ebom/${ebom.id}/tree`);
      setSelectedEbomTree(tree);
    } catch (e) {
      console.error(e);
    }
  };

  const handleTransformToMbom = async () => {
    if (!selectedEbom) return;
    setIsTransforming(true);
    try {
      await api.post('/bom/transform', {
        ebomHeaderId: selectedEbom.id,
        plantLocation: 'Osaka',
        targetRevisionCode: 'A',
      });
      alert('mBOM transformation completed successfully! Generated shop floor routing structure.');
      loadData();
    } catch (err: any) {
      alert(err.message || 'Transformation failed');
    } finally {
      setIsTransforming(false);
    }
  };

  const renderEbomNode = (item: EbomItem, depth = 0) => {
    return (
      <div key={item.id} className="space-y-1">
        <div
          className={`flex items-center justify-between p-2 rounded-lg border text-xs ${
            item.itemType === 'ASSEMBLY'
              ? 'bg-slate-50 border-slate-200 font-bold'
              : 'bg-white border-slate-100'
          }`}
          style={{ marginLeft: `${depth * 20}px` }}
        >
          <div className="flex items-center gap-2">
            <span className="font-mono text-[11px] text-sky-600">{item.partNumber}</span>
            <span className="text-slate-800">{item.description}</span>
          </div>
          <div className="flex items-center gap-3">
            <Badge variant={item.itemType === 'ASSEMBLY' ? 'purple' : 'neutral'}>
              {item.itemType}
            </Badge>
            <span className="font-semibold text-slate-900 font-mono">
              {item.quantity} {item.uom}
            </span>
          </div>
        </div>
        {item.children?.map((child) => renderEbomNode(child, depth + 1))}
      </div>
    );
  };

  return (
    <>
      <Header
        title="Multi-BOM & Transformation Management"
        subtitle="eBOM Design Structure → mBOM Manufacturing & Plant Work Center Routing"
      />
      <main className="flex-1 p-8 space-y-6 overflow-y-auto">
        {/* Actions Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <label className="text-xs font-semibold text-slate-700">Active eBOM Model:</label>
            <select
              value={selectedEbom?.id || ''}
              onChange={(e) => {
                const b = eboms.find((x) => x.id === e.target.value);
                if (b) handleSelectEbom(b);
              }}
              className="bg-white border border-slate-200 rounded-lg px-3 py-1.5 text-xs font-bold text-slate-800 focus:ring-2 focus:ring-sky-500"
            >
              {eboms.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.productNumber} (Rev {b.revisionCode}) - {b.status}
                </option>
              ))}
            </select>
          </div>

          <Button
            onClick={handleTransformToMbom}
            size="sm"
            isLoading={isTransforming}
            variant="secondary"
          >
            <RefreshCw className="w-4 h-4 mr-1" /> Execute eBOM → mBOM Transformation
          </Button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* eBOM Visual Hierarchy Tree */}
          <Card>
            <CardHeader
              title={`Engineering BOM: ${selectedEbom?.productNumber || 'SMW-HM-500'}`}
              subtitle={`Revision ${selectedEbom?.revisionCode || 'A'} • Status: ${selectedEbom?.status || 'APPROVED'}`}
              action={<Badge variant="success">{selectedEbom?.status || 'APPROVED'}</Badge>}
            />
            <CardBody className="space-y-2 max-h-[600px] overflow-y-auto">
              {selectedEbomTree.length === 0 ? (
                <div className="text-center py-12 text-slate-400 text-xs">
                  {isLoading ? 'Loading eBOM tree...' : 'No BOM components in this hierarchy.'}
                </div>
              ) : (
                selectedEbomTree.map((item) => renderEbomNode(item))
              )}
            </CardBody>
          </Card>

          {/* mBOM Manufacturing Routing */}
          <Card>
            <CardHeader
              title="Manufacturing BOM (mBOM)"
              subtitle="Work center routing, shop floor assembly steps, and plant assignments"
            />
            <CardBody className="p-0 max-h-[600px] overflow-y-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Seq / Routing</th>
                    <th className="px-6 py-3">Part / Assembly</th>
                    <th className="px-6 py-3">Work Center</th>
                    <th className="px-6 py-3 text-right">Quantity</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {mboms.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-6 py-12 text-center text-slate-400">
                        {isLoading ? 'Loading mBOM...' : 'No mBOM generated. Click "Execute Transformation" above.'}
                      </td>
                    </tr>
                  ) : (
                    mboms[0]?.items?.map((item) => (
                      <tr key={item.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-bold font-mono text-slate-900">
                          OP-{item.operationSeq || item.itemSeq * 10}
                        </td>
                        <td className="px-6 py-3.5">
                          <div className="font-bold text-slate-900">{item.description}</div>
                          <div className="text-[11px] font-mono text-sky-600">{item.partNumber}</div>
                        </td>
                        <td className="px-6 py-3.5">
                          <Badge variant="purple">{item.workCenter || 'WC-ASSEMBLY-01'}</Badge>
                        </td>
                        <td className="px-6 py-3.5 text-right font-bold text-slate-900">
                          {item.quantity} {item.uom}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </CardBody>
          </Card>
        </div>
      </main>
    </>
  );
}
