# Super League Hub

A live-score tournament platform built with Spring Boot (Java) and Next.js (React). Features real-time match simulation, multi-tier league standings (Star/Gold/Blue divisions), group stages, and knockout brackets.

## Tech Stack

- **Backend**: Spring Boot 3.2.4, JPA/Hibernate, WebSocket, H2/PostgreSQL
- **Frontend**: Next.js 16, Tailwind CSS, Lucide React, WebSocket client
- **Build**: Maven, npm

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+
- (Optional) PostgreSQL and Redis for production

### Run the Backend

```bash
cd backend
mvn spring-boot:run
```

The server starts on `http://localhost:8080`. Mock data (24 teams, 264 players, group fixtures) is seeded automatically. Live match simulation begins immediately with goals/cards every 8 seconds.

### Run the Frontend

```bash
cd client
npm install
npm run dev
```

Open `http://localhost:3000` in your browser.

### H2 Console

Access the in-memory database at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:super_league_hub`).

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/fixtures` | All fixtures |
| GET | `/api/fixtures/live` | Live matches only |
| GET | `/api/fixtures/{id}` | Fixture with events |
| GET | `/api/standings` | All division standings |
| GET | `/api/standings/{division}` | Single division standings |
| GET | `/api/bracket` | Full knockout bracket tree |
| GET | `/api/bracket/{stage}` | Bracket stage (quarter_final, semi_final, final) |
| POST | `/api/admin/events/goal` | Inject a goal event |
| POST | `/api/admin/bracket/initialize` | Generate knockout bracket |

## WebSocket

Connect to `ws://localhost:8080/ws/live` for real-time fixture, standings, and bracket updates.

## Architecture

### Database Schema

- **teams** — id, name, short_name, division (STAR/GOLD/BLUE)
- **players** — id, name, position, number, team_id
- **fixtures** — id, home_team_id, away_team_id, division, group_name, stage, status (NOT_STARTED → FIRST_HALF → HALFTIME → SECOND_HALF → FINISHED), home_score, away_score, match_minute
- **live_events** — id, fixture_id, type (GOAL/YELLOW_CARD/RED_CARD/SUBSTITUTION), team_id, player_id, event_minute
- **standings** — id, team_id, division, group_name, points, played, won, drawn, lost, goals_for, goals_against, goal_difference, position
- **knockout_brackets** — id, fixture_id, stage, position, parent_winner_id, parent_loser_id

### Real-time Pipeline

1. `LiveSimulationScheduler` runs every 8 seconds
2. Updates match minutes and randomly triggers goals/cards
3. `LiveEventService` persists to DB and caches in Redis (optional)
4. `LiveScoreWebSocketHandler` broadcasts JSON to all connected clients
5. Frontend updates scores in-place without page reload

### Aggregation Engine

When a match finishes:
- `StandingService` recalculates points, GD, and positions for the division
- `KnockoutBracketService` propagates winners through the bracket tree

## Project Structure

```
super-league-hub/
├── backend/
│   └── src/main/java/com/superleague/
│       ├── config/          # WebSocket, Redis, CORS config
│       ├── model/           # JPA entities + enums
│       ├── repository/      # Spring Data repositories
│       ├── service/         # Business logic
│       ├── controller/      # REST endpoints
│       ├── dto/             # WebSocket message DTOs
│       ├── ingestion/       # Data seeder + match simulator
│       └── websocket/       # LiveScore WebSocket handler
└── client/
    └── src/
        ├── app/             # Next.js pages + layout
        ├── components/      # LiveFeed, StandingsTable, KnockoutBracket
        └── lib/             # API client + WebSocket factory
```
