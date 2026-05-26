const API_BASE = 'http://localhost:8080/api';
const WS_URL = 'ws://localhost:8080/ws/live';

export interface FixtureData {
  id: string;
  homeTeam: string;
  awayTeam: string;
  homeTeamShort: string;
  awayTeamShort: string;
  homeScore: number;
  awayScore: number;
  status: string;
  minute: number;
  division: string;
  stage: string;
  groupName?: string;
}

export interface StandingRow {
  teamId: string;
  teamName: string;
  teamShort: string;
  position: number;
  points: number;
  played: number;
  won: number;
  drawn: number;
  lost: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDifference: number;
}

export interface BracketNode {
  bracketId: string;
  fixtureId: string;
  homeTeam: string;
  awayTeam: string;
  homeTeamShort: string;
  awayTeamShort: string;
  homeScore: number;
  awayScore: number;
  position: number;
  parentWinnerId: string | null;
  parentLoserId: string | null;
  status: string;
}

export async function fetchLiveFixtures(): Promise<FixtureData[]> {
  const res = await fetch(`${API_BASE}/fixtures/live`);
  if (!res.ok) throw new Error('Failed to fetch live fixtures');
  return res.json();
}

export async function fetchAllFixtures(): Promise<FixtureData[]> {
  const res = await fetch(`${API_BASE}/fixtures`);
  if (!res.ok) throw new Error('Failed to fetch fixtures');
  return res.json();
}

export async function fetchStandings(): Promise<Record<string, StandingRow[]>> {
  const res = await fetch(`${API_BASE}/standings`);
  if (!res.ok) throw new Error('Failed to fetch standings');
  return res.json();
}

export async function fetchBracket(): Promise<Record<string, BracketNode[]>> {
  const res = await fetch(`${API_BASE}/bracket`);
  if (!res.ok) throw new Error('Failed to fetch bracket');
  return res.json();
}

export function createWebSocket(
  onMessage: (data: unknown) => void,
  onConnect?: () => void,
  onDisconnect?: () => void
): WebSocket {
  const ws = new WebSocket(WS_URL);

  ws.onopen = () => {
    console.log('[WS] Connected');
    onConnect?.();
  };

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      onMessage(data);
    } catch (e) {
      console.error('[WS] Parse error:', e);
    }
  };

  ws.onclose = () => {
    console.log('[WS] Disconnected');
    onDisconnect?.();
  };

  ws.onerror = (error) => {
    console.error('[WS] Error:', error);
  };

  return ws;
}
