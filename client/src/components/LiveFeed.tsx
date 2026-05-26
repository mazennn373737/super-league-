'use client';

import { useEffect, useState, useCallback } from 'react';
import { Goal, Clock, AlertTriangle, Palette, RefreshCw } from 'lucide-react';
import { FixtureData, fetchLiveFixtures, fetchAllFixtures, createWebSocket } from '@/lib/api';

const DIVISION_COLORS: Record<string, string> = {
  STAR: 'bg-purple-600',
  GOLD: 'bg-amber-600',
  BLUE: 'bg-blue-600',
};

const DIVISION_BADGES: Record<string, string> = {
  STAR: 'text-purple-400 border-purple-500',
  GOLD: 'text-amber-400 border-amber-500',
  BLUE: 'text-blue-400 border-blue-500',
};

const STATUS_CONFIG: Record<string, { label: string; color: string; pulse?: boolean }> = {
  FIRST_HALF: { label: 'LIVE', color: 'bg-green-500', pulse: true },
  SECOND_HALF: { label: 'LIVE', color: 'bg-green-500', pulse: true },
  HALFTIME: { label: 'HT', color: 'bg-yellow-500' },
  FINISHED: { label: 'FT', color: 'bg-gray-500' },
  NOT_STARTED: { label: 'UP', color: 'bg-blue-500' },
};

export default function LiveFeed() {
  const [fixtures, setFixtures] = useState<FixtureData[]>([]);
  const [activeDivision, setActiveDivision] = useState<string>('ALL');
  const [loading, setLoading] = useState(true);

  const loadFixtures = useCallback(async () => {
    try {
      const data = await fetchAllFixtures();
      setFixtures(data);
    } catch (e) {
      console.error('Failed to load fixtures:', e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadFixtures();

    const ws = createWebSocket((data: unknown) => {
      const msg = data as Record<string, unknown>;
      if (msg && msg.fixtureId) {
        setFixtures((prev) => {
          const existing = prev.find((f) => f.id === msg.fixtureId);
          if (!existing) return prev;
          const hs = typeof msg.homeScore === 'number' ? msg.homeScore : undefined;
          const as = typeof msg.awayScore === 'number' ? msg.awayScore : undefined;
          const st = typeof msg.status === 'string' ? msg.status : undefined;
          const mn = typeof msg.minute === 'number' ? msg.minute : undefined;
          return prev.map((f) =>
            f.id === msg.fixtureId
              ? {
                  ...f,
                  ...(hs !== undefined && { homeScore: hs }),
                  ...(as !== undefined && { awayScore: as }),
                  ...(st !== undefined && { status: st as FixtureData['status'] }),
                  ...(mn !== undefined && { minute: mn }),
                }
              : f
          );
        });
      }
    });

    return () => ws.close();
  }, [loadFixtures]);

  const divisions = ['ALL', 'STAR', 'GOLD', 'BLUE'];
  const groupedFixtures: Record<string, FixtureData[]> = {};
  const filtered =
    activeDivision === 'ALL'
      ? fixtures
      : fixtures.filter((f) => f.division === activeDivision);

  filtered.forEach((f) => {
    const key = f.division;
    if (!groupedFixtures[key]) groupedFixtures[key] = [];
    groupedFixtures[key].push(f);
  });

  const isLive = (status: string) =>
    status === 'FIRST_HALF' || status === 'SECOND_HALF';

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
        {divisions.map((div) => (
          <button
            key={div}
            onClick={() => setActiveDivision(div)}
            className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
              activeDivision === div
                ? `${DIVISION_COLORS[div] || 'bg-slate-600'} text-white`
                : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
            }`}
          >
            {div === 'ALL' ? 'All' : div}
          </button>
        ))}
      </div>

      {Object.entries(groupedFixtures).length === 0 && (
        <div className="text-center py-16 text-slate-400">
          <Goal className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p className="text-lg">No fixtures found</p>
        </div>
      )}

      {Object.entries(groupedFixtures).map(([division, divFixtures]) => (
        <div key={division} className="mb-8">
          <div className="flex items-center gap-2 mb-4">
            <Palette
              className={`w-5 h-5 ${
                division === 'STAR'
                  ? 'text-purple-400'
                  : division === 'GOLD'
                  ? 'text-amber-400'
                  : 'text-blue-400'
              }`}
            />
            <h2 className="text-xl font-bold">{division} DIVISION</h2>
            <span className="text-sm text-slate-500">
              ({divFixtures.filter((f) => isLive(f.status)).length} live)
            </span>
          </div>

          <div className="grid gap-3">
            {divFixtures.map((fixture) => {
              const statusCfg = STATUS_CONFIG[fixture.status] || STATUS_CONFIG.NOT_STARTED;
              const isLiveMatch = isLive(fixture.status);

              return (
                <div
                  key={fixture.id}
                  className={`bg-slate-800 rounded-xl p-4 border transition-all ${
                    isLiveMatch
                      ? 'border-green-500/30 shadow-lg shadow-green-500/5'
                      : fixture.status === 'FINISHED'
                      ? 'border-slate-700'
                      : 'border-slate-700/50'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-xs text-slate-500">{fixture.stage}</span>
                        {isLiveMatch && (
                          <span className="text-[10px] uppercase tracking-wider text-green-400 border border-green-500/40 px-1.5 py-0.5 rounded pulse-live">
                            Live
                          </span>
                        )}
                      </div>
                      <p className="text-lg font-semibold">{fixture.homeTeam}</p>
                      <p className="text-lg font-semibold">{fixture.awayTeam}</p>
                    </div>

                    <div className="text-right">
                      {fixture.status !== 'NOT_STARTED' && (
                        <div className="text-3xl font-bold tabular-nums mb-1">
                          <span
                            className={
                              isLiveMatch && fixture.homeScore > fixture.awayScore
                                ? 'text-green-400'
                                : ''
                            }
                          >
                            {fixture.homeScore}
                          </span>
                          <span className="text-slate-500 mx-1">-</span>
                          <span
                            className={
                              isLiveMatch && fixture.awayScore > fixture.homeScore
                                ? 'text-green-400'
                                : ''
                            }
                          >
                            {fixture.awayScore}
                          </span>
                        </div>
                      )}

                      <div className="flex items-center justify-end gap-1.5">
                        {statusCfg.pulse ? (
                          <span className="relative flex h-2 w-2">
                            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75" />
                            <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500" />
                          </span>
                        ) : null}
                        {fixture.status !== 'NOT_STARTED' && (
                          <span className="text-sm text-slate-400">
                            {fixture.status === 'FINISHED'
                              ? 'FT'
                              : `${fixture.minute}'`}
                          </span>
                        )}
                        {fixture.minute > 0 && isLiveMatch && (
                          <Clock className="w-3.5 h-3.5 text-green-400" />
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
}
