# CI/CD Pipeline Guide

This repository utilizes GitHub Actions to automate Continuous Integration (CI) and Continuous Deployment (CD).

## Environments
We explicitly enforce separation of environments through our deployment strategy:
- **Development**: Local environment utilizing `docker-compose up` via `.env`.
- **Staging**: Automatically deployed when code is pushed/merged to `main`. This is used for pre-production integration testing.
- **Production**: Requires **manual** deployment. We deliberately DO NOT deploy to production automatically from every commit to adhere to robust release engineering and QA standards.

## CI/CD Workflows

### 1. Pull Request Pipeline (`.github/workflows/pr.yml`)
When a Pull Request is opened against `main`, a rigorous suite of checks is enforced to protect the primary branch.
- **Frontend Checks**: Executes `npm ci`, ESLint linting, and TypeScript type checking.
- **Backend Checks**: Compiles the Spring Boot monolith and executes the complete Maven test suite (unit, integration, and E2E databases).
- **Security Scan**: Utilizes Aqua Security's `trivy` to scan the repository filesystem for vulnerable packages (Critical & High CVEs).
- **Docker Build Test**: Verifies that both Frontend and Backend Dockerfiles still compile successfully via `docker compose build`.

### 2. Main Branch Pipeline (`.github/workflows/main.yml`)
When a PR is merged or code is pushed to `main`, the CD pipeline executes:
- **Build and Test**: Re-verifies compilation and tests to ensure no integration regressions occurred during merge.
- **Publish Images**: Builds and pushes Docker images to GitHub Container Registry (GHCR). Images are tagged with `latest` and the unique commit SHA.
- **Deploy to Staging**: Automatically rolls out the new images to the Staging environment using environment-specific secrets.

## Secrets Management
**Crucial Rule**: Never store plaintext secrets, credentials, or `.env` files in Git.

The pipeline relies on GitHub Actions Secrets configured under `Settings > Secrets and variables > Actions`.
- `GITHUB_TOKEN`: Inherited natively to authenticate pushes to GHCR.
- `STAGING_SSH_KEY`: The private key used by the runner to trigger rolling updates on the staging server.

Production deployments will pull the images built during the CD pipeline and inject production configurations directly from a secure vault or managed secrets manager.
