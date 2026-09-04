'use client';

import React from 'react';
import { StatCard } from '@/components/ui/StatCard';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Shield, Users, Database, Lock, Activity, Server, Cpu } from 'lucide-react';

export const AdminDashboard: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="RBAC Roles Configured"
          value="8 Roles"
          icon={<Shield className="w-6 h-6" />}
          subtitle="Method-Level @PreAuthorize"
          color="sky"
        />
        <StatCard
          title="System Architecture"
          value="Modular Monolith"
          icon={<Server className="w-6 h-6" />}
          subtitle="Spring Boot 3.3.4 & Java 25"
          color="emerald"
        />
        <StatCard
          title="Flyway Schema Migrations"
          value="V1 - V9"
          icon={<Database className="w-6 h-6" />}
          subtitle="100% Validated & Applied"
          color="indigo"
        />
        <StatCard
          title="Security Governance"
          value="JWT + AOP Audit"
          icon={<Lock className="w-6 h-6" />}
          subtitle="Zero Hardcoded Secrets"
          color="amber"
        />
      </div>

      <Card>
        <CardHeader
          title="Platform Architecture & Subsystem Health Status"
          subtitle="Enterprise digital manufacturing control nodes"
        />
        <CardBody>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
            <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50">
              <div className="flex items-center gap-2 font-bold text-slate-900 mb-2">
                <Database className="w-4 h-4 text-sky-600" /> PostgreSQL & Flyway
              </div>
              <p className="text-slate-600 leading-relaxed">
                Relational transactional core managing PDM, eBOM, mBOM, multi-warehouse stock balances, and shop floor work orders.
              </p>
            </div>
            <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50">
              <div className="flex items-center gap-2 font-bold text-slate-900 mb-2">
                <Shield className="w-4 h-4 text-emerald-600" /> 8-Tier RBAC & AOP Audit
              </div>
              <p className="text-slate-600 leading-relaxed">
                Method-level authorization matrix enforcing separation of concerns between Design Engineering, SCM, and Shop Operations.
              </p>
            </div>
            <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50">
              <div className="flex items-center gap-2 font-bold text-slate-900 mb-2">
                <Cpu className="w-4 h-4 text-indigo-600" /> LangChain4j RAG AI Layer
              </div>
              <p className="text-slate-600 leading-relaxed">
                Role-scoped manufacturing intelligence with zero direct SQL execution, strict schema grounding, and source citation.
              </p>
            </div>
          </div>
        </CardBody>
      </Card>
    </div>
  );
};
