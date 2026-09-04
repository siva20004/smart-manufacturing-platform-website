'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { api } from '@/lib/api';
import { AuditLog } from '@/lib/types';
import { ShieldCheck, Search, Filter } from 'lucide-react';

export default function AuditPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    async function loadLogs() {
      setIsLoading(true);
      try {
        const data = await api.get<AuditLog[]>('/audit/logs').catch(() => []);
        setLogs(data);
      } finally {
        setIsLoading(false);
      }
    }
    loadLogs();
  }, []);

  const filtered = logs.filter(
    (l) =>
      l.entityName.toLowerCase().includes(search.toLowerCase()) ||
      l.action.toLowerCase().includes(search.toLowerCase()) ||
      (l.changedData && l.changedData.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <>
      <Header
        title="Enterprise Security & Audit Trail"
        subtitle="Immutable state-change logging via Aspect-Oriented Programming (AOP)"
      />
      <main className="flex-1 p-8 space-y-6 overflow-y-auto">
        <div className="flex items-center justify-between">
          <div className="relative w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search audit trail by entity, action, or details..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>
        </div>

        <Card>
          <CardHeader
            title="System Audit Log Explorer"
            subtitle="Captures entity creations, state transitions, and user authorization timestamps"
          />
          <CardBody className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3">Timestamp</th>
                    <th className="px-6 py-3">Entity Domain</th>
                    <th className="px-6 py-3">Action</th>
                    <th className="px-6 py-3">Audit Details</th>
                    <th className="px-6 py-3">IP Address</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {filtered.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-12 text-center text-slate-400">
                        {isLoading ? 'Loading audit records...' : 'No audit log entries match your filter.'}
                      </td>
                    </tr>
                  ) : (
                    filtered.map((log) => (
                      <tr key={log.id} className="hover:bg-slate-50/80">
                        <td className="px-6 py-3.5 font-mono text-slate-500">
                          {new Date(log.createdAt).toLocaleString()}
                        </td>
                        <td className="px-6 py-3.5 font-bold text-slate-900">{log.entityName}</td>
                        <td className="px-6 py-3.5">
                          <Badge variant="purple">{log.action}</Badge>
                        </td>
                        <td className="px-6 py-3.5 text-slate-700 max-w-md truncate">
                          {log.changedData || 'State transition recorded'}
                        </td>
                        <td className="px-6 py-3.5 font-mono text-slate-400">{log.ipAddress || '127.0.0.1'}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardBody>
        </Card>
      </main>
    </>
  );
}
