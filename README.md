# CareVault HMS 🏥

A **Hospital Management System** built using **Microservices Architecture** with **Spring Boot**. This project demonstrates a scalable, distributed backend system featuring AI-powered medical triage, role-based access control, event-driven notifications, and centralized configuration management.

---

## 🏗 Architecture Overview

The system is composed of 7 independent microservices communicating through REST and Kafka:

| Service | Description | Port |
| :--- | :--- | :--- |
| **Discovery Server** | Service registry using Netflix Eureka | `8761` |
| **Config Server** | Centralized configuration for all services | `8888` |
| **API Gateway** | Single entry point — handles routing, JWT validation, and role-based access control | `4004` |
| **Auth Service** | User authentication — login, JWT generation with role claim | Dynamic |
| **Patient Service** | Patient registration and profile management | Dynamic |
| **Appointment Service** | Slot management, appointment booking, approval/rejection workflow | Dynamic |
| **AI Service** | Conversational medical triage using Google Gemini AI | Dynamic |
| **Notification Service** | Event-driven email notifications via Kafka | Dynamic |
| **Analytics Service** | Consumes Kafka events for hospital analytics | Dynamic |

---

## 🧠 Key Features

- **Conversational AI Triage** — Multi-turn chatbot using Google Gemini that collects symptoms through follow-up questions and recommends the appropriate department and urgency level
- **Role-Based Access Control** — JWT-based authentication with PATIENT, DOCTOR, and ADMIN roles enforced at the API Gateway level
- **Appointment Management** — Full booking workflow with slot availability, urgency-based pending queue sorting, and staff approval/rejection flow
- **Event-Driven Notifications** — Kafka-based async email notifications to patients on appointment approval or rejection
- **Microservices Data Isolation** — Each service owns its own PostgreSQL database, communicating only via REST or Kafka
- **Centralized Security** — JWT validated directly at the gateway, role and identity headers forwarded to downstream services

---

## 🛠 Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.4, Spring Cloud 2024 |
| **Service Discovery** | Netflix Eureka |
| **API Gateway** | Spring Cloud Gateway |
| **Database** | PostgreSQL (separate DB per service) |
| **Messaging** | Apache Kafka |
| **Serialization** | Protocol Buffers (Protobuf) |
| **AI Integration** | Google Gemini API (`gemini-2.5-flash`) |
| **Security** | JWT (JJWT library), BCrypt password hashing |
| **Containerization** | Docker |
| **Configuration** | Spring Cloud Config Server (native mode) |

---

## ⚙️ Prerequisites

- **Java 21**
- **Docker Desktop**
- **Apache Kafka** (Docker container)
- **PostgreSQL** (Docker container)
- **Google Gemini API Key**
- **Gmail App Password** (for email notifications)

---

## 🚀 Getting Started

### 1. Environment Setup

Create a `.env` file in the project root with:

```env
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret_base64
JWT_EXPIRATION=86400000
GEMINI_API_KEY=your_gemini_api_key
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

### 2. Start Services

Start in the exact order below to ensure dependencies are available:

**1. Databases**
```
Start auth-service-db and services-db Docker containers
```

**2. Kafka**
```
Start kafka Docker container
```

**3. Discovery Server**
```bash
cd discovery-server
./mvnw spring-boot:run
```

**4. Config Server**
```bash
cd config-server
./mvnw spring-boot:run
```

**5. Core Services** (open separate terminal for each)
```bash
cd auth-service && ./mvnw spring-boot:run
```
```bash
cd patient-service && ./mvnw spring-boot:run
```
```bash
cd appointment-service && ./mvnw spring-boot:run
```
```bash
cd ai-service && ./mvnw spring-boot:run
```
```bash
cd notification-service && ./mvnw spring-boot:run
```
```bash
cd analytics-service && ./mvnw spring-boot:run
```

**6. API Gateway** (start last)
```bash
cd api-gateway
./mvnw spring-boot:run
```

### 3. Verify

Open Eureka Dashboard at `http://localhost:8761` — all services should show `UP` status.

All API requests go through: `http://localhost:4004`

---

## 📂 Project Structure

```
medi_sync_microservices and gateway/
├── ai-service/              # Gemini AI triage chatbot
├── analytics-service/       # Kafka event consumer for analytics
├── api-gateway/             # Spring Cloud Gateway + JWT/Role filters
├── appointment-service/     # Appointment + slot management
├── auth-service/            # Authentication + JWT generation
├── config-server/           # Centralized configuration
├── discovery-server/        # Netflix Eureka service registry
├── notification-service/    # Kafka consumer + email sender
└── patient-service/         # Patient profile management
```

---

## 🔒 Security Design

- **JWT validation** at gateway level — individual services trust gateway headers
- **Role-based routing** — each route restricted to specific roles via `RoleValidationGatewayFilterFactory`
- **BCrypt** password hashing in auth-service
- **Prompt injection detection** on every AI chat turn
- **Medical input validation** via dedicated Gemini YES/NO call before main chat

---

## 🚧 Planned Improvements

- Gateway-direct JWT validation — removing per-request auth-service dependency
- Server-side session ID generation for chat security
- Docker Compose for simplified orchestration
- Migrate selected microservices to MongoDB for flexible schema management and document-based storage.

---

## 👨‍💻 Author

**Tanishkumar Patel**
