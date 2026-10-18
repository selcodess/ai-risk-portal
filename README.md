# AI RiskPilot — Enterprise AI Risk & Governance Portal

[![CI/CD Pipeline](https://github.com/selca/ai-risk-portal/actions/workflows/ci.yml/badge.svg)](https://github.com/selca/ai-risk-portal/actions)
![Angular](https://img.shields.io/badge/Angular-22-DD0031?logo=angular)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk)
![License](https://img.shields.io/badge/License-Proprietary_Swisscom_Portfolio-blue)

An enterprise-grade, human-in-the-loop AI risk governance and compliance portal built for the Swisscom technical portfolio. It provides structured risk assessment, explainable AI guidance, and legally defensible human sign-offs with full audit trails.

---

## 🏛 Architecture Overview

RiskPilot follows a decoupled microfrontend and microservices pattern designed to operate within memory-conscious production environments:

```
[ Angular 22 Shell (Port 4200) ]
        │
        ├── Features (Lazy-Loaded Chunks)
        │     ├── /dashboard      (Metrics, signal-computed filters, risk register)
        │     ├── /risks/:id      (Full CRUD, decision package review, AI assistant)
        │     ├── /reports        (Departmental coverage, severity distribution, JSON export)
        │     └── /login          (Role-based JWT login: ADMIN, REVIEWER, VIEWER)
        │
        ├── Core API Services & Interceptors (Bearer JWT, Error Handling)
        │
        ├─── REST HTTP (Port 8080) ──► [ Spring Boot: Risk Service ]
        │                              ├── Risk Catalog & CRUD
        │                              ├── Spring Security & JWT Filter
        │                              └── In-memory H2 DB (riskdb)
        │
        └─── REST HTTP (Port 8081) ──► [ Spring Boot: Decision Service ]
                                       ├── Expert Review Workflow & Justification
                                       ├── Immutable Audit Trail Logging
                                       └── In-memory H2 DB (decisiondb)
```

---

## ⚡ Lightweight Local Execution (4 GB RAM Optimized)

Each service can be started and evaluated individually without requiring Docker or Kubernetes:

### 1. Angular Microfrontend (Frontend)
```powershell
cd C:\Users\selca\Projects\ai-risk-portal
npm install --legacy-peer-deps
npm start
# Available at http://localhost:4200
```

### 2. Spring Boot Risk Service (Backend Microservice 1)
```powershell
cd C:\Users\selca\Projects\ai-risk-portal\backend\risk-service
.\mvnw.cmd spring-boot:run
# REST API: http://localhost:8080/api/risks
# Swagger UI: http://localhost:8080/swagger-ui/index.html
# H2 Console: http://localhost:8080/h2-console
```

### 3. Spring Boot Decision Service (Backend Microservice 2)
```powershell
cd C:\Users\selca\Projects\ai-risk-portal\backend\decision-service
.\mvnw.cmd spring-boot:run
# REST API: http://localhost:8081/api/decisions
# Swagger UI: http://localhost:8081/swagger-ui/index.html
# H2 Console: http://localhost:8081/h2-console
```

---

## 🔐 Credentials & Role-Based Access Control

The services are pre-seeded with three demo roles:

| Username | Password | Role | Permissions |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ADMIN` | Full permissions, risk deletion, audit log access |
| `reviewer` | `reviewer123` | `REVIEWER` | Create risks, edit details, record formal human decisions |
| `viewer` | `viewer123` | `VIEWER` | Read-only access to risk register and reports |

*Note: If the backend services are not running, the frontend gracefully activates **Offline LocalStorage Sync** mode with instant one-click demo logins.*

---

## 🧪 Automated Testing

### Frontend Tests (Vitest)
```powershell
npm test -- --no-watch
```

### Backend Microservices Tests (JUnit 5 + MockMvc)
```powershell
# Risk Service Test Suite
cd backend\risk-service
.\mvnw.cmd test

# Decision Service Test Suite
cd backend\decision-service
.\mvnw.cmd test
```

---

## 🔄 Human-in-the-Loop Governance Workflow

1. **Risk Identification**: Risk logged with title, category, owner, and severity.
2. **Completeness & AI Package**: Decision package prepared evaluating completeness and testing against Swisscom risk guidelines.
3. **Expert Justification**: The human reviewer provides mandatory written rationale (min 20 chars) and records self-assessed confidence.
4. **Audit Immutability**: Every status update and formal decision creates an immutable audit trail entry recording actor, action, and timestamp.
