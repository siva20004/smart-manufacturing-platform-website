'use client';

import React, { useState } from 'react';
import { Header } from '@/components/layout/Header';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Bot, Send, Sparkles, Shield, Database, Cpu, FileCode2 } from 'lucide-react';

interface Message {
  role: 'user' | 'assistant';
  text: string;
  citations?: string[];
}

export default function AiAssistantPage() {
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<Message[]>([
    {
      role: 'assistant',
      text: 'Konnichiwa! I am the Siva Machine Works Smart Manufacturing AI Assistant (LangChain4j RAG Layer). How can I assist you with eBOM configurations, spindle runout tolerances, plant inventory, or supplier procurement today?',
      citations: ['docs/03-system-architecture.md', 'db/schema/V8__production_and_quality.sql'],
    },
  ]);
  const [isLoading, setIsLoading] = useState(false);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMsg = input;
    setMessages((prev) => [...prev, { role: 'user', text: userMsg }]);
    setInput('');
    setIsLoading(true);

    // Simulated RAG processing response grounded in Siva Machine Works domain rules
    setTimeout(() => {
      let reply = '';
      let citations = ['docs/08-database-design.md'];

      if (userMsg.toLowerCase().includes('bom') || userMsg.toLowerCase().includes('hm-500')) {
        reply = 'The flagship SMW-HM-500 Horizontal Machining Center eBOM consists of two primary subassemblies: the Hydraulic System (PUMP-HP-75 displacement pump and VLV-PROP-350 proportional valve) and the Electrical System (PLC-CNC-840D controller, MTR-SRV-22KW servo motors, and SEN-LIN-ENC optical linear encoders). All components are verified and approved under Revision A.';
        citations = ['db/schema/V4__ebom_schema.sql', 'db/schema/V5__mbom_transformation.sql'];
      } else if (userMsg.toLowerCase().includes('runout') || userMsg.toLowerCase().includes('spindle')) {
        reply = 'According to the Factory Acceptance Test (FAT) precision metrology standards for the Osaka Heavy Machining facility, the maximum permissible spindle runout is 0.0020 mm (2.0 µm). Current production orders PRD-2026-HM500-01 logged a test result of 0.0012 mm, exceeding JIS B 6338 precision standards.';
        citations = ['db/schema/V8__production_and_quality_schema.sql', 'docs/08-database-design.md'];
      } else {
        reply = `I have analyzed your query across the enterprise manufacturing knowledge base. All data access adheres to role-scoped RAG retrieval with zero direct unrestricted database access.`;
        citations = ['docs/10-development-rules.md'];
      }

      setMessages((prev) => [
        ...prev,
        {
          role: 'assistant',
          text: reply,
          citations,
        },
      ]);
      setIsLoading(false);
    }, 600);
  };

  return (
    <>
      <Header
        title="AI Manufacturing Intelligence Assistant"
        subtitle="Role-Scoped RAG Layer (LangChain4j) • Grounded in Engineering Drawings, eBOM & Shop Floor Rules"
      />
      <main className="flex-1 p-8 space-y-6 overflow-y-auto">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Chat Window */}
          <div className="lg:col-span-2 space-y-4">
            <Card className="h-[650px] flex flex-col justify-between">
              <CardHeader
                title="Interactive Manufacturing RAG Assistant"
                subtitle="Ask questions regarding CAD drawings, component shortages, and CNC operation routings"
                action={
                  <span className="text-[11px] font-semibold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-1 rounded-md flex items-center gap-1">
                    <Shield className="w-3 h-3" /> Zero SQL Injection / Safe Grounding
                  </span>
                }
              />
              <CardBody className="flex-1 overflow-y-auto space-y-4 p-6">
                {messages.map((m, idx) => (
                  <div
                    key={idx}
                    className={`flex items-start gap-3 text-xs ${
                      m.role === 'user' ? 'justify-end' : 'justify-start'
                    }`}
                  >
                    {m.role === 'assistant' && (
                      <div className="w-8 h-8 rounded-lg bg-sky-600 text-white flex items-center justify-center flex-shrink-0 font-bold shadow-sm">
                        <Bot className="w-4 h-4" />
                      </div>
                    )}
                    <div
                      className={`max-w-xl p-3.5 rounded-2xl ${
                        m.role === 'user'
                          ? 'bg-sky-600 text-white rounded-br-none'
                          : 'bg-slate-100 text-slate-800 rounded-bl-none border border-slate-200'
                      }`}
                    >
                      <p className="leading-relaxed">{m.text}</p>
                      {m.citations && (
                        <div className="mt-2 pt-2 border-t border-slate-200/60 flex flex-wrap gap-1.5">
                          {m.citations.map((c, i) => (
                            <span
                              key={i}
                              className="text-[10px] font-mono bg-white text-slate-600 border border-slate-200 px-1.5 py-0.5 rounded"
                            >
                              📄 {c}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                ))}
                {isLoading && (
                  <div className="flex items-center gap-2 text-slate-400 text-xs italic">
                    <Sparkles className="w-4 h-4 animate-spin text-sky-500" />
                    Retrieving manufacturing knowledge...
                  </div>
                )}
              </CardBody>

              <div className="p-4 border-t border-slate-100 bg-slate-50/50">
                <form onSubmit={handleSend} className="flex gap-2">
                  <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder="Ask about SMW-HM-500 eBOM, spindle tolerances, or warehouse shortages..."
                    className="flex-1 bg-white border border-slate-200 rounded-xl px-4 py-2.5 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-500"
                  />
                  <Button type="submit" size="sm">
                    <Send className="w-4 h-4 mr-1" /> Ask AI
                  </Button>
                </form>
              </div>
            </Card>
          </div>

          {/* AI Security & Architecture Panel */}
          <div className="space-y-4">
            <Card>
              <CardHeader title="AI Governance Directives" subtitle="Strict project rule enforcement" />
              <CardBody className="space-y-3 text-xs">
                <div className="p-3 rounded-lg border border-slate-200 bg-slate-50">
                  <div className="font-bold text-slate-900 mb-1 flex items-center gap-1.5">
                    <Shield className="w-3.5 h-3.5 text-emerald-600" /> No Direct Database Access
                  </div>
                  <p className="text-slate-600">
                    The LLM/AI layer is isolated from direct database connections. Queries execute strictly via role-scoped embeddings & API service boundaries.
                  </p>
                </div>
                <div className="p-3 rounded-lg border border-slate-200 bg-slate-50">
                  <div className="font-bold text-slate-900 mb-1 flex items-center gap-1.5">
                    <FileCode2 className="w-3.5 h-3.5 text-sky-600" /> Mandatory Citations
                  </div>
                  <p className="text-slate-600">
                    Every generated recommendation must cite source drawing numbers, BOM revisions, or operating procedures.
                  </p>
                </div>
              </CardBody>
            </Card>
          </div>
        </div>
      </main>
    </>
  );
}
