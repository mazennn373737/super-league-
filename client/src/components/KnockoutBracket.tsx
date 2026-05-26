'use client';

import { useEffect, useState, useCallback } from 'react';
import { Swords, Trophy, RefreshCw, ChevronRight } from 'lucide-react';
import { BracketNode, fetchBracket, createWebSocket } from '@/lib/api';

const STAGE_ORDER = ['quarter_final', 'semi_final', 'final'];
const STAGE_LABELS: Record<string, string> = {
  quarter_final: 'Quarter-Finals',
  semi_final: 'Semi-Finals',
  final: 'Final',
};

const STAGE_ICONS: Record<string, React.ReactNode> = {
  quarter_final: <Swords className="w-4 h-4" />,
  semi_final: <Swords className="w-4 h-4" />,
  final: <Trophy className="w-4 h-4 text-yellow-400" />,
};

function MatchCard({ node }: { node: BracketNode }) {
  const isFinished = node.status === 'FINISHED';
  const isLive =
    node.status === 'FIRST_HALF' || node.status === 'SECOND_HALF';
  const isHomeWinner =
    isFinished && node.homeScore > node.awayScore;
  const isAwayWinner =
    isFinished && node.awayScore > node.homeScore;

  return (
    <div
      className={`bg-slate-800 rounded-lg p-3 border min-w-[200px] transition-all ${
        isLive
          ? 'border-green-500/40 shadow-md shadow-green-500/10'
          : isFinished
          ? 'border-slate-600'
          : isHomeWinner || isAwayWinner
          ? 'border-yellow-500/30'
          : 'border-slate-700'
      }`}
    >
      {isLive && (
        <div className="flex items-center gap-1.5 mb-2">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500" />
          </span>
          <span className="text-[10px] uppercase text-green-400 font-semibold tracking-wider">
            LIVE {node.homeScore !== undefined ? `${node.homeScore}-${node.awayScore}` : ''}
          </span>
        </div>
      )}

      <div className="space-y-1.5">
        <div
          className={`flex justify-between items-center text-sm ${
            isHomeWinner ? 'text-green-400 font-bold' : 'text-slate-200'
          }`}
        >
          <span className="truncate">{node.homeTeam}</span>
          <span className="ml-3 font-mono tabular-nums">
            {node.homeScore}
          </span>
        </div>
        <div className="border-t border-slate-700/50" />
        <div
          className={`flex justify-between items-center text-sm ${
            isAwayWinner ? 'text-green-400 font-bold' : 'text-slate-200'
          }`}
        >
          <span className="truncate">{node.awayTeam}</span>
          <span className="ml-3 font-mono tabular-nums">
            {node.awayScore}
          </span>
        </div>
      </div>

      {!isLive && !isFinished && node.status !== 'NOT_STARTED' && (
        <div className="mt-1.5 text-[10px] text-slate-500 text-center">
          {node.status === 'HALFTIME' ? 'HT' : `${node.status}`}
        </div>
      )}
    </div>
  );
}

function ConnectorLine() {
  return (
    <div className="flex items-center justify-center h-full">
      <ChevronRight className="w-5 h-5 text-slate-600" />
    </div>
  );
}

export default function KnockoutBracket() {
  const [brackets, setBrackets] = useState<Record<string, BracketNode[]>>({});
  const [loading, setLoading] = useState(true);

  const loadBracket = useCallback(async () => {
    try {
      const data = await fetchBracket();
      setBrackets(data);
    } catch (e) {
      console.error('Failed to load bracket:', e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadBracket();

    const ws = createWebSocket((data: unknown) => {
      const msg = data as Record<string, unknown>;
      if (msg && msg.stage && msg.nodes) {
        const stage = (msg.stage as string).toLowerCase();
        setBrackets((prev) => ({
          ...prev,
          [stage]: msg.nodes as BracketNode[],
        }));
      }
    });

    return () => ws.close();
  }, [loadBracket]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <RefreshCw className="w-8 h-8 animate-spin text-blue-400" />
      </div>
    );
  }

  const hasBracket = Object.keys(brackets).length > 0;

  if (!hasBracket) {
    return (
      <div className="text-center py-16 text-slate-400">
        <Swords className="w-12 h-12 mx-auto mb-4 opacity-50" />
        <p className="text-lg">No bracket data yet</p>
        <p className="text-sm text-slate-500">
          Knockout brackets will be generated after group stage matches complete
        </p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center gap-2 mb-6">
        <Trophy className="w-5 h-5 text-yellow-400" />
        <h2 className="text-xl font-bold">Knockout Stage</h2>
      </div>

      <div className="overflow-x-auto pb-4">
        <div className="flex gap-8 min-w-max items-start">
          {STAGE_ORDER.map((stage, stageIdx) => {
            const nodes = brackets[stage];
            if (!nodes || nodes.length === 0) return null;

            const isLast = stageIdx === STAGE_ORDER.length - 1;

            return (
              <div key={stage} className="flex items-start gap-4">
                <div>
                  <div className="flex items-center gap-2 mb-4 text-sm font-semibold text-slate-400 uppercase tracking-wider">
                    {STAGE_ICONS[stage]}
                    {STAGE_LABELS[stage]}
                  </div>
                  <div className="space-y-4">
                    {nodes.map((node) => (
                      <MatchCard key={node.bracketId} node={node} />
                    ))}
                  </div>
                </div>

                {!isLast && (
                  <div className="flex items-center h-full pt-10">
                    <ConnectorLine />
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
