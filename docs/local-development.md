# Local Development Guide

This guide explains how to spin up the entire Siva Machine Works platform locally using Docker Compose.

## Prerequisites
- Docker and Docker Compose installed.
- Ensure ports `8080`, `3000`, `5432`, `6379`, `9000`, and `9001` are available on your machine.

## Configuration
All sensitive configurations and credentials have been moved to environment variables.
1. Copy the `.env.example` file to create your local `.env` file:
   ```bash
   cp .env.example .env
   ```
2. Modify `.env` with your secure credentials if needed. **Never commit `.env` to version control.**

## Starting the Application
The entire system can be started using a single documented command from the root directory:

```bash
docker-compose up -d --build
```

### What this does:
1. **PostgreSQL (pgvector)**: Starts on port `5432` with a persistent volume.
2. **Redis**: Starts on port `6379` for caching.
3. **MinIO (Object Storage)**: Starts on port `9000` (API) and `9001` (Console) with a persistent volume.
4. **Backend (Spring Boot)**: Builds and starts on port `8080`. It waits for DB, Redis, and MinIO to be healthy before starting.
5. **Frontend (Next.js)**: Builds and starts on port `3000`. It waits for the backend to be healthy before starting.

## Health Checks & Security
- All Dockerfiles run services as non-root users (`spring` and `nextjs`).
- Docker Compose utilizes native `healthcheck` configurations to ensure rigorous startup ordering.
- No hardcoded credentials exist in Dockerfiles; everything is injected via `.env`.

## Stopping the Application
To stop all services and keep the volumes (data preserved):
```bash
docker-compose down
```

To stop all services and **delete all data** (clean slate):
```bash
docker-compose down -v
```

## Accessing the Services
- **Frontend App**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/v1/...
- **MinIO Console**: http://localhost:9001


## Architecture Diagram

```mermaid
flowchart TD
    F[Next.js Frontend\n:3000]
    B[Spring Boot Backend\n(Integrated AI Engine)\n:8080]
    P[(PostgreSQL + pgvector\n:5432)]
    S[MinIO Object Storage\n:9000]
    R[(Redis Cache\n:6379)]

    F -- REST API --> B
    B -- JDBC / Flyway --> P
    B -- S3 API --> S
    B -- Spring Data Redis --> R
```
