# AWS Deployment & Architecture Strategy

This document outlines the production-ready AWS deployment architecture for the Siva Machine Works platform. It is designed to be highly available, secure, and easily maintainable without introducing unnecessary complexity.

## 1. Deployment Architecture Diagram

```mermaid
flowchart TD
    User([End User]) -->|HTTPS| Route53[AWS Route 53 / WAF]
    Route53 --> ALB[Application Load Balancer]

    subgraph "AWS VPC (Production)"
        subgraph "Public Subnets"
            ALB
            NAT[NAT Gateway]
        end

        subgraph "Private Subnets (Compute)"
            FE[Frontend (ECS Fargate)
Next.js]
            BE[Backend (ECS Fargate)
Spring Boot + AI]
        end

        subgraph "Private Subnets (Data)"
            RDS[(Amazon RDS PostgreSQL
with pgvector)]
            Redis[(Amazon ElastiCache
Redis)]
        end
    end

    ALB -->|Port 3000| FE
    ALB -->|Port 8080| BE
    FE -- Internal API Call --> ALB

    BE -->|JDBC| RDS
    BE -->|Spring Data| Redis
    BE -->|IAM Auth| S3[(Amazon S3
PDM/Docs)]

    %% Operations / Observability
    BE -.-> SM[AWS Secrets Manager]
    FE -.-> SM
    BE -.-> CW[Amazon CloudWatch]
    FE -.-> CW
    GHCR[GitHub Container Registry] -.-> FE
    GHCR -.-> BE
```

## 2. Infrastructure Definition
We recommend using **Terraform** as the Infrastructure-as-Code (IaC) tool to provision this environment. 
- **VPC Module**: Provisions 2 Public Subnets, 2 Private Subnets (across 2 AZs), Internet Gateway, and NAT Gateway.
- **Data Module**: Provisions RDS (PostgreSQL 16+ supporting `pgvector`), ElastiCache (Redis), and an S3 Bucket.
- **Compute Module**: Provisions the ECS Cluster, ALB, Target Groups, Task Definitions, and Fargate Services.

## 3. Environment Configuration
Environment variables and sensitive credentials will NOT be passed via plain text.
- **AWS Secrets Manager**: Will store the `POSTGRES_PASSWORD`, `JWT_SECRET`, and external API keys (if any).
- **ECS Task Definition**: Uses the `secrets` attribute to map Secrets Manager ARNs directly into the container's environment variables at runtime.

## 4. Networking Requirements
- **Public Subnets**: Contain the Application Load Balancer (ALB) and NAT Gateways.
- **Private Subnets**: Contain ECS Fargate Tasks (Compute) and RDS/Redis (Data). 
- **Egress**: Private subnets route outbound traffic through the NAT Gateway (required to pull Docker images from GHCR).
- **Ingress**: Only the ALB is exposed to the internet (ports 80/443).

## 5. Security Requirements
- **Security Groups (SGs)**:
  - `ALB-SG`: Allows inbound 443 (HTTPS) from 0.0.0.0/0.
  - `Frontend-SG`: Allows inbound 3000 only from `ALB-SG`.
  - `Backend-SG`: Allows inbound 8080 only from `ALB-SG` and `Frontend-SG`.
  - `DB-SG`: Allows inbound 5432 only from `Backend-SG`.
  - `Redis-SG`: Allows inbound 6379 only from `Backend-SG`.
- **Encryption**: KMS encryption enabled for RDS, S3, Secrets Manager, and CloudWatch.

## 6. IAM Requirements
ECS requires strict role separation:
- **Task Execution Role**: Used by the ECS agent. Requires permissions to pull images from GHCR (if private), fetch secrets from AWS Secrets Manager, and push logs to CloudWatch.
- **Task Role**: Used by the running application itself. 
  - **Backend Task Role**: Requires `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject` for the PDM documentation bucket. (This eliminates the need for hardcoded S3 access keys in the `.env` file).

## 7. Database Migration Strategy
- **Flyway**: The Spring Boot backend uses Flyway (`spring.flyway.enabled=true`).
- **Execution**: When a new ECS Backend task starts, it automatically acquires a database lock and runs pending migrations before the Spring Application Context completes.
- **Safety**: Because Flyway utilizes advisory locks in PostgreSQL, rolling out multiple ECS tasks simultaneously will not cause race conditions.

## 8. Backup Strategy
- **PostgreSQL**: RDS Automated Backups enabled (7-day retention) + Weekly manual snapshots.
- **S3 (Documents)**: Versioning enabled on the S3 bucket to prevent accidental overwrite/deletion of critical engineering PDM files. Cross-Region Replication (CRR) can be evaluated for disaster recovery.

## 9. Logging
- Uses the `awslogs` log driver in the ECS Task Definition.
- Both Frontend and Backend stdout/stderr streams are aggregated into **Amazon CloudWatch Logs** (e.g., `/ecs/siva-platform-backend`).

## 10. Monitoring
- **ALB Health Checks**: Periodically ping `/actuator/health` (Backend) and `/` (Frontend) to automatically terminate and replace unhealthy Fargate tasks.
- **CloudWatch Metrics**: Monitors CPU and Memory utilization. We will set up Auto Scaling Policies to scale out tasks if CPU > 70%.

## 11. Rollback Strategy
- **Application Rollback**: Using AWS ECS Rolling Updates. If a new deployment fails health checks, ECS automatically stops the deployment and routes traffic back to the old healthy containers.
- **Database Rollback**: Flyway migrations should be strictly **forward-only** (no destructive `DROP COLUMN` without a multi-phase release). In the event of catastrophic data corruption, we restore via RDS Point-in-Time Recovery (PITR).

---

## 12. The Promotion Lifecycle

How code flows from a developer's laptop to Production:

1. **Local Development** 
   - Developer checks out code.
   - Runs `docker-compose up -d --build` (using local Postgres, Redis, and MinIO).
   - Validates changes locally.

2. **GitHub (Pull Request)** 
   - Code is pushed to a feature branch.
   - The **CI Pipeline** runs automatically (Linting, TypeScript checks, Maven tests, Trivy Security Scan).
   - Code Review and Merge to `main`.

3. **Continuous Integration (CI) / Docker Build** 
   - Merge to `main` triggers the **Main Branch Pipeline**.
   - Tests run again to ensure integrity.
   - `docker build` packages the Frontend and Backend into immutable Docker containers.
   - The containers are pushed to the **GitHub Container Registry (GHCR)** tagged with the commit SHA.

4. **Staging Environment** 
   - The CD pipeline automatically triggers an AWS ECS service update for the Staging environment.
   - Staging pulls the newly tagged image from GHCR.
   - Flyway runs migrations against the Staging RDS instance.
   - QA performs end-to-end user acceptance testing.

5. **Production Deployment** 
   - Triggered **manually** via a GitHub Actions Release or Deployment workflow.
   - ECS pulls the exact same, heavily tested Docker image (by SHA) used in Staging.
   - Flyway safely updates the Production RDS schema.
   - Traffic smoothly shifts to the new containers via the ALB without downtime.
