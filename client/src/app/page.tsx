'use client';

import { useState } from 'react';
import { Radio, Table2, GitBranch } from 'lucide-react';
import LiveFeed from '@/components/LiveFeed';
import StandingsTable from '@/components/StandingsTable';
import KnockoutBracket from '@/components/KnockoutBracket';

const TABS = [
  { id: 'live', label: 'Live Feed', icon: Radio },
  { id: 'standings', label: 'Standings', icon: Table2 },
  { id: 'bracket', label: 'Knockout Bracket', icon: GitBranch },
];

export default function Home() {
  const [activeTab, setActiveTab] = useState('live');

  return (
    <div className="min-h-screen bg-slate-900">
      <header className="border-b border-slate-700 bg-slate-800/50 backdrop-blur-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 py-3">
          <div className="flex items-center gap-3">
            <Radio className="w-6 h-6 text-green-400" />
            <h1 className="text-xl font-bold">Super League Hub</h1>
            <span className="text-[10px] uppercase tracking-widest text-slate-500 border border-slate-600 px-2 py-0.5 rounded">
              Live Scores
            </span>
          </div>
        </div>
      </header>

      <nav className="border-b border-slate-700 bg-slate-800/30">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex gap-1">
            {TABS.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 px-5 py-3 text-sm font-medium transition-all border-b-2 ${
                    activeTab === tab.id
                      ? 'border-green-400 text-green-400 bg-green-500/5'
                      : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-500'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 py-6">
        {activeTab === 'live' && <LiveFeed />}
        {activeTab === 'standings' && <StandingsTable />}
        {activeTab === 'bracket' && <KnockoutBracket />}
      </main>
    </div>
  );
}
