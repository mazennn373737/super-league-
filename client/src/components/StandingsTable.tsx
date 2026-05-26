'use client';

import { useEffect, useState, useCallback } from 'react';
import {
  Trophy,
  Medal,
  ArrowUpDown,
  RefreshCw,
  TrendingUp,
  Circle,
} from 'lucide-react';
import { StandingRow, fetchStandings, createWebSocket } from '@/lib/api';

const DIVISION_META: Record<string, { label: string; icon: React.ReactNode; color: string; headerBg: string }> = {
  STAR: {
    label: 'Star Division',
    icon: <Trophy className="w-5 h-5 text-purple-400" />,
    color: 'text-purple-400',
    headerBg: 'bg-purple-900/30',
  },
  GOLD: {
    label: 'Gold Division',
    icon: <Medal className="w-5 h-5 text-amber-400" />,
    color: 'text-amber-400',
    headerBg: 'bg-amber-900/30',
  },
  BLUE: {
    label: 'Blue Division',
    icon: <Circle className="w-5 h-5 text-blue-400" />,
    color: 'text-blue-400',
    headerBg: 'bg-blue-900/30',
  },
};

function PositionBadge({ pos }: { pos: number }) {
  if (pos <= 2) {
    return (
      <span
        className={`inline-flex items-center justify-center w-7 h-7 rounded-full text-xs font-bold ${
          pos === 1
            ? 'bg-green-500/20 text-green-400'
            : 'bg-blue-500/20 text-blue-400'
        }`}
      >
        {pos}
      </span>
    );
  }
  if (pos <= 4) {
    return (
      <span className="inline-flex items-center justify-center w-7 h-7 rounded-full bg-slate-700 text-slate-300 text-xs font-bold">
        {pos}
      </span>
    );
  }
  return (
    <span className="inline-flex items-center justify-center w-7 h-7 text-slate-500 text-xs font-bold">
      {pos}
    </span>
  );
}

export default function StandingsTable() {
  const [standings, setStandings] = useState<Record<string, StandingRow[]>>({});
  const [loading, setLoading] = useState(true);
  const [activeDivision, setActiveDivision] = useState<string>('STAR');

  const loadStandings = useCallback(async () => {
    try {
      const data = await fetchStandings();
      setStandings(data);
    } catch (e) {
      console.error('Failed to load standings:', e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStandings();

    const ws = createWebSocket((data: unknown) => {
      const msg = data as Record<string, unknown>;
      if (msg && msg.division && msg.standings) {
        setStandings((prev) => ({
          ...prev,
          [msg.division as string]: msg.standings as StandingRow[],
        }));
      }
    });

    return () => ws.close();
  }, [loadStandings]);

  const divisions = ['STAR', 'GOLD', 'BLUE'];
  const currentRows = standings[activeDivision] || [];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <RefreshCw className="w-8 h-8 animate-spin text-blue-400" />
      </div>
    );
  }

  return (
    <div>
      <div className="flex gap-2 mb-6">
        {divisions.map((div) => {
          const meta = DIVISION_META[div];
          return (
            <button
              key={div}
              onClick={() => setActiveDivision(div)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                activeDivision === div
                  ? `${meta.headerBg} ${meta.color}`
                  : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
              }`}
            >
              {meta.icon}
              {meta.label}
            </button>
          );
        })}
      </div>

      {currentRows.length === 0 ? (
        <div className="text-center py-16 text-slate-400">
          <TrendingUp className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p className="text-lg">No standings data yet</p>
          <p className="text-sm text-slate-500">Standings will appear after matches are played</p>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-slate-700">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-800 text-slate-400 uppercase text-xs tracking-wider">
                <th className="py-3 px-4 text-left">#</th>
                <th className="py-3 px-4 text-left">Team</th>
                <th className="py-3 px-4 text-center">MP</th>
                <th className="py-3 px-4 text-center">W</th>
                <th className="py-3 px-4 text-center">D</th>
                <th className="py-3 px-4 text-center">L</th>
                <th className="py-3 px-4 text-center">GF</th>
                <th className="py-3 px-4 text-center">GA</th>
                <th className="py-3 px-4 text-center">GD</th>
                <th className="py-3 px-4 text-center font-bold text-white">PTS</th>
              </tr>
            </thead>
            <tbody>
              {currentRows.map((row, idx) => (
                <tr
                  key={row.teamId}
                  className={`border-t border-slate-700/50 transition-colors hover:bg-slate-800/50 ${
                    row.position <= 4 ? 'bg-slate-800/30' : ''
                  }`}
                >
                  <td className="py-3 px-4">
                    <PositionBadge pos={row.position} />
                  </td>
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-white">{row.teamName}</span>
                    </div>
                  </td>
                  <td className="py-3 px-4 text-center text-slate-300">{row.played}</td>
                  <td className="py-3 px-4 text-center text-green-400">{row.won}</td>
                  <td className="py-3 px-4 text-center text-yellow-400">{row.drawn}</td>
                  <td className="py-3 px-4 text-center text-red-400">{row.lost}</td>
                  <td className="py-3 px-4 text-center text-slate-300">{row.goalsFor}</td>
                  <td className="py-3 px-4 text-center text-slate-300">{row.goalsAgainst}</td>
                  <td
                    className={`py-3 px-4 text-center font-medium ${
                      row.goalDifference > 0
                        ? 'text-green-400'
                        : row.goalDifference < 0
                        ? 'text-red-400'
                        : 'text-slate-400'
                    }`}
                  >
                    {row.goalDifference > 0 ? '+' : ''}
                    {row.goalDifference}
                  </td>
                  <td className="py-3 px-4 text-center font-bold text-white text-base">
                    {row.points}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="mt-4 flex items-center gap-4 text-xs text-slate-500">
        <span className="flex items-center gap-1">
          <ArrowUpDown className="w-3 h-3" /> Sorted by: Points &gt; GD &gt; GF
        </span>
        <span>
          Top 4 qualify for knockout stage
        </span>
      </div>
    </div>
  );
}
