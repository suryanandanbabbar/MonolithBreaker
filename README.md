# Monolith Breaker

Monolith Breaker analyzes legacy Java monoliths using deterministic AST and graph analytics, then proposes safe microservice split boundaries.

## Architecture
- **backend/**: Spring Boot 3 + Java 21 analysis API.
- **frontend/**: React + Vite + Tailwind + React Flow UI.
- **docker/**: Runtime reverse proxy config.
- **sample-project/**: Legacy Java project for demo uploads.
- **docs/**: API contract, risk-scoring notes, roadmap.

## Run (Docker)
```bash
docker compose up --build
```
Frontend: http://localhost:5173
Backend health: http://localhost:8080/api/health

## Run (Non-docker)
```bash
cd backend && mvn spring-boot:run
cd frontend && npm install && npm run dev
```

## API Overview
- `POST /api/projects`
- `POST /api/projects/{projectId}/upload`
- `POST /api/projects/{projectId}/analysis`
- `GET /api/analysis/{runId}/status`
- `GET /api/analysis/{runId}/nodes`
- `GET /api/analysis/{runId}/edges`
- `GET /api/analysis/{runId}/risk`
- `GET /api/analysis/{runId}/communities`
- `POST /api/analysis/{runId}/ai/split`

## Demo Walkthrough
1. Create project from UI.
2. Upload zipped `sample-project`.
3. Start analysis.
4. Open Dashboard to inspect risk leaderboard, graph, communities, and split output.
