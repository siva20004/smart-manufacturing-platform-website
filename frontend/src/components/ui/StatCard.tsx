import React from 'react';
import { Card } from './Card';
import clsx from 'clsx';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  trend?: {
    value: string;
    isPositive: boolean;
  };
  subtitle?: string;
  color?: 'sky' | 'emerald' | 'indigo' | 'amber' | 'rose';
}

export const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon,
  trend,
  subtitle,
  color = 'sky',
}) => {
  const iconColors = {
    sky: 'bg-sky-50 text-sky-600 border-sky-200',
    emerald: 'bg-emerald-50 text-emerald-600 border-emerald-200',
    indigo: 'bg-indigo-50 text-indigo-600 border-indigo-200',
    amber: 'bg-amber-50 text-amber-600 border-amber-200',
    rose: 'bg-rose-50 text-rose-600 border-rose-200',
  };

  return (
    <Card className="p-5">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{title}</p>
          <h3 className="text-2xl font-bold text-slate-900 mt-1">{value}</h3>
        </div>
        <div className={clsx('w-12 h-12 rounded-xl flex items-center justify-center border', iconColors[color])}>
          {icon}
        </div>
      </div>
      {(trend || subtitle) && (
        <div className="mt-3 flex items-center gap-2 text-xs">
          {trend && (
            <span
              className={clsx(
                'font-semibold',
                trend.isPositive ? 'text-emerald-600' : 'text-rose-600'
              )}
            >
              {trend.isPositive ? '↑' : '↓'} {trend.value}
            </span>
          )}
          {subtitle && <span className="text-slate-400">{subtitle}</span>}
        </div>
      )}
    </Card>
  );
};
