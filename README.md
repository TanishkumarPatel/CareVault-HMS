# MediSync - Microservices Healthcare Platform 🏥

MediSync is a robust healthcare management application built using a **Microservices Architecture** with **Spring Boot**. The system is designed to handle patient data, appointment scheduling, and automated notifications using a distributed, event-driven approach.

## 🏗 Architecture Overview

The backend is composed of several independent microservices communicating via REST and asynchronous messaging:

| Service | Description | Port |
| :--- | :--- | :--- |
| **Discovery Server** | Service Registry using Netflix Eureka. | `8761` |
| **Config Server** | Centralized configuration management for all services. | `8888` |
| **API Gateway** | Entry point for the system; handles routing. | `8080` |
| **Patient Service** | Manages patient profiles and records (PostgreSQL). | *(Dynamic)* |
| **Appointment Service** | Handles scheduling and doctor-patient bookings. | *(Dynamic)* |
| **Notification Service** | Sends automated alerts/emails via Kafka events. | *(Dynamic)* |
| **Analytics Service** | Processes system data for healthcare insights. | *(Dynamic)* |

## 🛠 Tech Stack

* **Language:** Java 17 / 21
* **Framework:** Spring Boot 3.4.1, Spring Cloud 2024.0.0
* **Service Discovery:** Netflix Eureka
* **API Gateway:** Spring Cloud Gateway
* **Databases:** PostgreSQL (Production), H2 (Development/Testing)
* **Messaging:** Apache Kafka (Event-Driven Communication)
* **Serialization:** Google Protobuf
* **Documentation:** SpringDoc OpenAPI (Swagger UI)

## ⚙️ Prerequisites

Before running the project, ensure you have the following installed:

1.  **Java JDK 17** (Minimum required for most services)
2.  **PostgreSQL** (Running locally or via Docker)
3.  **Apache Kafka** (Broker and Zookeeper)
4.  **Maven** (For building dependencies)

## 🚀 Getting Started

Follow these steps to run the MediSync services locally.

### 1. Infrastructure Setup
Ensure your data and messaging layers are active:
* Start **PostgreSQL** (Create databases for `patient_db` and `appointment_db`).
* Start **Kafka** (Ensure the broker is reachable on `localhost:9092`).

### 2. Run Services
Start the microservices in the exact order below to ensure the configuration and discovery layers are ready:

1.  **Discovery Server**
    ```bash
    cd "medi_sync_microservices and gateway/discovery-server"
    ./mvnw spring-boot:run
    ```
2.  **Config Server**
    ```bash
    cd "medi_sync_microservices and gateway/config-server"
    ./mvnw spring-boot:run
    ```
3.  **Core Microservices** (Open separate terminals)
    * **Patient Service**: `cd "medi_sync_microservices and gateway/patient-service" && ./mvnw spring-boot:run`
    * **Appointment Service**: `cd "medi_sync_microservices and gateway/appointment-service" && ./mvnw spring-boot:run`
    * **Notification Service**: `cd "medi_sync_microservices and gateway/notification-service" && ./mvnw spring-boot:run`
4.  **API Gateway**
    ```bash
    cd "medi_sync_microservices and gateway/api-gateway"
    ./mvnw spring-boot:run
    ```

## 🧠 Key Features

* **Centralized Config**: All service-specific properties are managed via the `config-server`.
* **Event-Driven Notifications**: Uses **Apache Kafka** to trigger notifications immediately when appointments are scheduled.
* **High Performance**: Utilizes **Google Protobuf** for efficient data serialization across service boundaries.
* **Service Discovery**: Automated service registration and heartbeats using Eureka.
* **API Documentation**: Integrated Swagger UI for testing endpoints.

## 📂 Project Structure

```bash
├── api_requests             # .http files for testing endpoints
├── medi_sync_microservices and gateway
│   ├── analytics-service    # Data processing & Kafka listener
│   ├── api-gateway          # Central entry point & routing
│   ├── appointment-service  # Booking & schedule management
│   ├── config-server        # Externalized YAML configurations
│   ├── discovery-server     # Eureka service registry
│   ├── notification-service # Kafka consumer for Email/SMS alerts
│   └── patient-service      # CRUD for patient records
└── README.md