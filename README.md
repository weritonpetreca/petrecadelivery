<div align="center">

# ⚔️ PetrecaDelivery

### *"Evil is evil. Lesser, greater, middling — it's all the same. But a witcher must choose."*
### *— And this witcher chose microservices.*

<br/>

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud_2025-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Resilience4j](https://img.shields.io/badge/Resilience4j_2.3-4CAF50?style=for-the-badge&logo=java&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-4D4D4D?style=for-the-badge&logo=keycloak&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-3C873A?style=for-the-badge&logo=auth0&logoColor=white)

</div>

---

## 📜 The Lore

> *In the Continent, monsters lurk in every shadow. In the world of distributed systems, the monsters have different names: cascading failures, tight coupling, single points of failure. Just as Geralt of Rivia wanders the Continent slaying beasts with silver and steel, **PetrecaDelivery** was forged to slay the complexity of modern delivery logistics — with microservices and resilience patterns.*

**PetrecaDelivery** is a production-grade delivery platform built on a microservices architecture. Each service is an independent witcher school — specialized, autonomous, and battle-hardened. They communicate through a **Service Registry** (the Witchers' notice board), an **API Gateway** (the city gates), and **Apache Kafka** (the ravens carrying messages across the Continent).


---

## 🗺️ The Continent — Architecture Overview

> *"The world doesn't need a hero. It needs a professional."* — The architecture agrees.

```mermaid
graph TD
    Client(["🧙 External Client"]):::client --> KC
    
    subgraph "🔐 Identity Provider"
        KC["🔑 Keycloak\n:8082"]:::security
    end
    
    KC -- "JWT Token" --> Client
    Client -- "Bearer Token" --> GW

    subgraph "🏰 The City Gates"
        GW["🚪 API Gateway\n:9999\n(OAuth2 Resource Server)"]:::gateway
    end

    subgraph "📋 The Notice Board"
        SR["📍 Service Registry\nEureka :8761"]:::registry
    end

    GW -- "discovers services" --> SR

    subgraph "⚔️ The Witcher Schools"
        DT["🚚 Delivery Tracking\n:8080"]:::service
        CM["🛵 Courier Management\n:8081"]:::service
    end

    DT -- "registers" --> SR
    CM -- "registers" --> SR

    GW -- "POST/GET /api/v1/deliveries/**" --> DT
    GW -- "GET/POST /api/v1/couriers/**" --> CM
    GW -- "GET /public/couriers/**\n(no auth required)" --> CM

    subgraph "🐦 The Ravens — Async Events"
        K["Apache Kafka\n:9092"]:::kafka
    end

    DT -- "DeliveryPlacedEvent\nDeliveryPickedUpEvent\nDeliveryFulfilledEvent" --> K
    K -- "consumes events" --> CM

    subgraph "🏗️ Infrastructure"
        PG[("🐘 PostgreSQL :5432\ncourierdb | deliverydb")]:::infra
        PGA["🖥️ pgAdmin :5050"]:::infra
        KUI["📊 Kafka UI :8090"]:::infra
    end

    subgraph "🔭 Observability"
        PROM["📈 Prometheus :9090"]:::observability
        GRAF["📉 Grafana :3000"]:::observability
    end

    DT --- PG
    CM --- PG
    PGA --- PG
    KUI --- K

    DT -- "exposes /actuator/prometheus" --> PROM
    CM -- "exposes /actuator/prometheus" --> PROM
    GW -- "exposes /actuator/prometheus" --> PROM
    SR -- "exposes /actuator/prometheus" --> PROM
    PROM -- "datasource" --> GRAF

    classDef client fill:#8B4513,color:#fff,stroke:#5C2D0A
    classDef security fill:#8B0000,color:#fff,stroke:#5C0000
    classDef gateway fill:#4A0E8F,color:#fff,stroke:#2D0860
    classDef registry fill:#1A5276,color:#fff,stroke:#0E3460
    classDef service fill:#1E8449,color:#fff,stroke:#145A32
    classDef kafka fill:#231F20,color:#fff,stroke:#000
    classDef infra fill:#555,color:#fff,stroke:#333
    classDef observability fill:#E6522C,color:#fff,stroke:#C13C1A
```

---

## 🏰 The Four Schools — Microservices

> *Every witcher school has its own mutations, its own signs, its own purpose. So do our services.*

| School | Port | The Sign It Casts |
| :--- | :---: | :--- |
| 📍 **Service Registry** | `8761` | The **Axii** sign — bends all services to register and be found. The Eureka Server that holds the map of the Continent. |
| 🚪 **API Gateway** | `9999` | The **Quen** shield — the single protective barrier between the outside world and the inner services. Routes traffic and applies **Retry** and **Circuit Breaker** patterns via Resilience4j. Also exposes sanitized `/public/couriers` routes, stripping sensitive fields from responses. |
| 🚚 **Delivery Tracking** | `8080` | The **Igni** flame — ignites the delivery lifecycle. Manages creation, editing, and every checkpoint of a delivery. Publishes domain events to Kafka and calls Courier Management via HTTP (also protected by its own Resilience4j Retry + Circuit Breaker). |
| 🛵 **Courier Management** | `8081` | The **Aard** blast — the force that assigns couriers to deliveries. Manages the full courier lifecycle, calculates payouts, and reacts to Kafka events from Delivery Tracking. |

---

## 🔄 The Delivery Lifecycle — A Witcher Contract

> *Every contract has stages. So does every delivery.*

```
  [DRAFT] ──place()──► [WAITING_FOR_COURIER] ──pickUp()──► [IN_TRANSIT] ──complete()──► [DELIVERED]
```

| Status | Meaning |
| :--- | :--- |
| `DRAFT` | The contract is written but not yet posted on the notice board. |
| `WAITING_FOR_COURIER` | Posted on the board. A `DeliveryPlacedEvent` is fired to Kafka. Couriers are notified. |
| `IN_TRANSIT` | A witcher (courier) accepted the contract. A `DeliveryPickedUpEvent` is fired. |
| `DELIVERED` | The contract is fulfilled. A `DeliveryFulfilledEvent` is fired. The courier's stats are updated. |

---

## ⚡ Resilience Patterns — The Witcher's Armor

> *"Monsters don't play fair. Neither does a distributed network."*

The platform is armored with **Resilience4j** at two levels:

**At the Gateway (protecting inbound traffic):**
- **Retry** on `delivery-tracking-route`: 3 attempts with exponential backoff (`10ms → 20ms → 30ms`) on `5xx` errors for `GET` and `PUT`.
- **Circuit Breaker** (`delivery-tracking-route-circuit-breaker`): Opens after 50% failure rate over 5 calls. Stays open for **5 seconds**, then enters `HALF_OPEN` to probe recovery.

**At Delivery Tracking (protecting outbound HTTP to Courier Management):**
- **Retry** (`Retry_CourierAPIClient_payoutCalculation`): 3 attempts on `ResourceAccessException`.
- **Circuit Breaker** (`CircuitBreaker_CourierAPIClient_payoutCalculation`): Same sliding window policy, opens for **5 seconds**.

---

## 🔐 Security — The Witcher's Medallion

> *"A witcher's medallion vibrates in the presence of magic. Our API Gateway vibrates in the presence of unauthorized requests."*

The platform is secured with **OAuth2 and OpenID Connect** via **Keycloak**, implementing industry-standard authentication and authorization patterns.

### 🎯 Security Architecture

All API requests must carry a valid **JWT (JSON Web Token)** issued by Keycloak. The API Gateway acts as a **Resource Server**, validating tokens before routing requests to downstream services.

```
Client → Keycloak (Authentication) → JWT Token → API Gateway (Validation) → Microservices
```

### 🔑 Keycloak Configuration

**Access:** `http://localhost:8082`

**Admin Console:**
- Username: `admin`
- Password: `admin`

**Realm:** `petreca-realm` (automatically imported on startup)

**Client:** `petreca-api-client`
- Type: Public client
- Grant Types: `password`, `authorization_code`
- Redirect URIs: `*` (development only)

### 🧙 Pre-Configured Test User

The realm comes with a ready-to-use test user:

| Field | Value |
| :--- | :--- |
| Username | `geralt` |
| Password | `witcher123` |
| Email | `geralt@kaermorhen.com` |

### 🔒 How Authentication Works

**Step 1: Obtain Access Token**

Request a JWT from Keycloak using the Resource Owner Password Credentials flow:

```bash
curl -X POST "http://localhost:8082/realms/petreca-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=petreca-api-client" \
  -d "grant_type=password" \
  -d "username=geralt" \
  -d "password=witcher123"
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer"
}
```

**Step 2: Use Token in API Requests**

Include the `access_token` in the `Authorization` header:

```bash
curl -X GET "http://localhost:9999/api/v1/deliveries" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 🛡️ Token Validation

The API Gateway validates every incoming request:

1. **Signature Verification**: Ensures the token was issued by Keycloak
2. **Expiration Check**: Rejects expired tokens (5-minute lifespan)
3. **Issuer Validation**: Confirms `iss` claim matches `http://localhost:8082/realms/petreca-realm`
4. **Audience Validation**: Verifies the token is intended for this API

### 🔓 Public vs Protected Routes

| Route Pattern | Authentication Required | Description |
| :--- | :---: | :--- |
| `/api/v1/deliveries/**` | ✅ Yes | Full delivery management (CRUD + lifecycle) |
| `/api/v1/couriers/**` | ✅ Yes | Full courier management (CRUD + payouts) |
| `/public/couriers` | ❌ No | Public courier list (sanitized, no sensitive data) |
| `/public/couriers/{id}` | ❌ No | Public courier detail (sanitized) |

### 🎯 Security Best Practices Implemented

- ✅ **Centralized Authentication**: Single source of truth (Keycloak)
- ✅ **Stateless Tokens**: No server-side session storage
- ✅ **Short Token Lifespan**: 5-minute access tokens reduce exposure window
- ✅ **Refresh Tokens**: 30-minute refresh tokens for seamless re-authentication
- ✅ **HTTPS Ready**: Configuration supports TLS (use in production)
- ✅ **Realm Isolation**: Separate realm for the application
- ✅ **Automatic Realm Import**: `realm-export.json` ensures consistent setup

### 📚 Further Reading

Want to dive deeper into OAuth2 and Keycloak?
- [OAuth 2.0 RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)

---

## 🏗️ Infrastructure — The Continent's Foundations

> *Every great witcher needs a keep. Ours runs on Docker.*

All infrastructure is provisioned via `docker-compose.yml`. The databases are **automatically created** on first boot via `init-databases.sql` — no manual steps required.

| Service | Port | Purpose |
| :--- | :---: | :--- |
| 🐘 **PostgreSQL 17** | `5432` | Relational persistence. Hosts `courierdb` and `deliverydb`. |
| 🖥️ **pgAdmin 4** | `5050` | Web UI for PostgreSQL management. |
| 📨 **Apache Kafka** (KRaft) | `9092` | Event streaming backbone. No Zookeeper needed. |
| 📊 **Kafka UI** | `8090` | Web UI to inspect topics, partitions, and messages. |
| 📈 **Prometheus** | `9090` | Metrics collection and time-series database. |
| 📉 **Grafana** | `3000` | Metrics visualization and dashboards. |
| 🔐 **Keycloak** | `8082` | Identity and Access Management (IAM). OAuth2/OpenID Connect provider. |

---

## 🛠️ The Witcher's Arsenal — Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java 21 (Virtual Threads ready) |
| **Framework** | Spring Boot 3.5.4 |
| **Service Discovery** | Spring Cloud Netflix Eureka (`2025.0.0`) |
| **API Gateway** | Spring Cloud Gateway (WebFlux) |
| **Security** | OAuth2 + OpenID Connect (Keycloak 24.0) |
| **Async Messaging** | Spring for Apache Kafka |
| **Resilience** | Resilience4j 2.3 (Circuit Breaker, Retry) |
| **Observability** | Prometheus + Grafana (Spring Boot Actuator) |
| **Persistence** | Spring Data JPA + Hibernate |
| **Database** | PostgreSQL 17 |
| **Containerization** | Docker & Docker Compose |
| **Build** | Maven (multi-module from root) |
| **Utilities** | Lombok, Bean Validation |

---

## 📋 Prerequisites — Before You Draw Your Sword

Ensure the following are installed on your machine:

- ☕ **JDK 21+**
- 🐳 **Docker & Docker Compose**
- 📦 **Apache Maven** (or use the included `./mvnw` wrapper)
- 🖥️ An IDE of your choice (IntelliJ IDEA recommended)

---

## ⚡ How to Run — Summoning the Continent

> *"If I'm to choose between one evil and another, I'd rather not choose at all."*
> *— But here, you must follow the order below.*

### Step 1 — Raise the Infrastructure

From the **project root**, start all infrastructure containers:

```bash
docker-compose up -d
```

This will start PostgreSQL, pgAdmin, Kafka, Kafka UI, Prometheus, Grafana, and Keycloak in the background. The databases `courierdb` and `deliverydb` are **automatically created** by the init script. The Keycloak realm `petreca-realm` is **automatically imported** with a pre-configured test user. No manual setup needed.

**Access URLs:**
- **pgAdmin**: `http://localhost:5050` (admin@admin.com / admin)
- **Kafka UI**: `http://localhost:8090`
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000` (admin / admin)
- **Keycloak**: `http://localhost:8082` (admin / admin)
  - **Test User**: geralt / witcher123

---

### Step 2 — Build All Modules

From the **project root**, build the entire platform with a single command:

```bash
./mvnw install -DskipTests
```

---

### Step 3 — Start the Microservices

Start the services in the order below. Open a separate terminal for each.

**A. Service Registry** *(start first — all others depend on it)*

```bash
cd Microservices/Service-Registry
./mvnw spring-boot:run
```

Wait until the Eureka dashboard is available at `http://localhost:8761`.

---

**B. Delivery Tracking & Courier Management** *(order between them doesn't matter)*

```bash
# Terminal 2
cd Microservices/Delivery-Tracking
./mvnw spring-boot:run
```

```bash
# Terminal 3
cd Microservices/Courier-Management
./mvnw spring-boot:run
```

> Delivery Tracking runs on port `8080` (default). Courier Management runs on port `8081` (configured in `application.yml`).

---

**C. API Gateway** *(start last — it discovers already-registered services)*

```bash
# Terminal 4
cd Microservices/Gateway
./mvnw spring-boot:run
```

---

### ✅ The Continent is Alive

All services are up. Send all requests through the Gateway at `http://localhost:9999`.

---

## 🗡️ API Reference — The Witcher's Contracts

### Delivery Tracking Endpoints

| Method | Path | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/deliveries` | Create a new delivery draft |
| `PUT` | `/api/v1/deliveries/{id}` | Edit a draft delivery |
| `GET` | `/api/v1/deliveries` | List all deliveries (paginated) |
| `GET` | `/api/v1/deliveries/{id}` | Get a delivery by ID |
| `POST` | `/api/v1/deliveries/{id}/placement` | Place the delivery (post to notice board) |
| `POST` | `/api/v1/deliveries/{id}/pickups` | Assign a courier (pick up) |
| `POST` | `/api/v1/deliveries/{id}/completion` | Mark delivery as completed |

### Courier Management Endpoints

| Method | Path | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/couriers` | Register a new courier |
| `PUT` | `/api/v1/couriers/{id}` | Update courier data |
| `GET` | `/api/v1/couriers` | List all couriers (paginated) |
| `GET` | `/api/v1/couriers/{id}` | Get a courier by ID |
| `POST` | `/api/v1/couriers/payout-calculation` | Calculate payout for a given distance |
| `GET` | `/public/couriers` | Public courier list (sensitive fields removed) |
| `GET` | `/public/couriers/{id}` | Public courier detail (sensitive fields removed) |

---

## 🔥 Complete End-to-End Test — A Witcher's Full Contract

> *"This is the way."* — Follow the path below to witness the full delivery lifecycle in action, including OAuth2 authentication.

### Option 1: Automated Script (Recommended)

The project includes a ready-to-use test script that handles authentication automatically. Simply run:

```bash
chmod +x test-delivery-flow.sh
./test-delivery-flow.sh
```

This script will automatically:
1. **Authenticate with Keycloak** (obtain JWT token for user `geralt`)
2. Create a courier (Geralt of Rivia)
3. Draft a delivery contract
4. Place the delivery on the notice board (fires `DeliveryPlacedEvent` to Kafka)
5. Assign the courier to the delivery (fires `DeliveryPickedUpEvent` to Kafka)
6. Complete the delivery (fires `DeliveryFulfilledEvent` to Kafka)
7. Display the final state

Expected output:
```
================================================
🐺 PETRECA DELIVERY - END-TO-END DEVSECOPS TEST
================================================
🔑 Requesting JWT from Keycloak (User: geralt)...
✅ Token successfully acquired!
------------------------------------------------

📍 Step 1: Recruiting a witcher...
✅ Courier created: <UUID>
   Name: Geralt of Rivia

📦 Step 2: Drafting a delivery contract...
✅ Delivery drafted: <UUID>
   Status: DRAFT

📋 Step 3: Posting contract on the notice board...
✅ Delivery placed
   Status: WAITING_FOR_COURIER
   Event: DeliveryPlacedEvent → Kafka

🛵 Step 4: Geralt accepts the contract...
✅ Delivery picked up by Geralt
   Status: IN_TRANSIT
   Event: DeliveryPickedUpEvent → Kafka

🏆 Step 5: Contract fulfilled...
✅ Delivery completed
   Status: DELIVERED
   Event: DeliveryFulfilledEvent → Kafka

🔍 Step 6: Inspecting the completed contract...
   Final Status: DELIVERED

========================================
⚔️ Contract complete. Toss a coin to your witcher.

📊 View Kafka events at: http://localhost:8090
🗄️  View database at: http://localhost:5050
📋 View service registry at: http://localhost:8761
📉 View metrics dashboard at: http://localhost:3000
🔐 View Keycloak admin at: http://localhost:8082
```

---

### Option 2: Manual Step-by-Step (For Learning)

> *"Patience is a virtue, especially when hunting monsters."*

**Step 0: Obtain Access Token**

Before making any API calls, authenticate with Keycloak:

```bash
TOKEN=$(curl -s -X POST "http://localhost:8082/realms/petreca-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=petreca-api-client" \
  -d "grant_type=password" \
  -d "username=geralt" \
  -d "password=witcher123" | jq -r '.access_token')

echo "Token: $TOKEN"
```

**📝 Save this token — you'll use it in all subsequent requests.**

---

**Step 1: Create a Courier First**

Before any delivery can be assigned, you need a witcher on the notice board:

```bash
curl -X POST http://localhost:9999/api/v1/couriers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Geralt of Rivia",
    "phone": "11987654321"
  }'
```

**📝 Copy the `id` from the response — this is your `COURIER_ID`.**

Example response:
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Geralt of Rivia",
  "phone": "11987654321",
  ...
}
```

---

**Step 2: Draft a Delivery**

```bash
curl -X POST http://localhost:9999/api/v1/deliveries \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": {
      "zipCode": "12345-000",
      "street": "Rua do Remetente",
      "number": "10",
      "name": "Empresa A",
      "phone": "11999999999"
    },
    "recipient": {
      "zipCode": "54321-000",
      "street": "Avenida do Destinatário",
      "number": "20",
      "name": "Cliente B",
      "phone": "11888888888"
    },
    "items": [{ "name": "Silver Sword", "quantity": 1 }]
  }'
```

**📝 Copy the `id` from the response — this is your `DELIVERY_ID`.**

---

**Step 3: Place the Delivery** *(fires `DeliveryPlacedEvent` to Kafka)*

Replace `YOUR_DELIVERY_ID_HERE` with the actual UUID:

```bash
curl -X POST http://localhost:9999/api/v1/deliveries/YOUR_DELIVERY_ID_HERE/placement \
  -H "Authorization: Bearer $TOKEN"
```

The delivery is now on the notice board. Check Kafka UI at `http://localhost:8090` — you'll see the `DeliveryPlacedEvent` in the `deliveries.v1.events` topic.

---

**Step 4: Assign the Courier** *(fires `DeliveryPickedUpEvent` to Kafka)*

Replace both `YOUR_DELIVERY_ID_HERE` and `YOUR_COURIER_ID_HERE`:

```bash
curl -X POST http://localhost:9999/api/v1/deliveries/YOUR_DELIVERY_ID_HERE/pickups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "courierId": "YOUR_COURIER_ID_HERE" }'
```

Geralt has accepted the contract. The delivery is now `IN_TRANSIT`.

---

**Step 5: Complete the Delivery** *(fires `DeliveryFulfilledEvent` to Kafka)*

```bash
curl -X POST http://localhost:9999/api/v1/deliveries/YOUR_DELIVERY_ID_HERE/completion \
  -H "Authorization: Bearer $TOKEN"
```

The contract is fulfilled. The delivery status is now `DELIVERED`, and Geralt's stats are updated.

---

**Step 6: Verify the Final State**

```bash
curl http://localhost:9999/api/v1/deliveries/YOUR_DELIVERY_ID_HERE \
  -H "Authorization: Bearer $TOKEN"
```

You should see:
- `"status": "DELIVERED"`
- `"fulfilledAt"` timestamp populated
- `"courierId"` matching Geralt's UUID

Check the courier's updated stats:
```bash
curl http://localhost:9999/api/v1/couriers/YOUR_COURIER_ID_HERE \
  -H "Authorization: Bearer $TOKEN"
```

You should see:
- `"fulfilledDeliveriesQuantity": 1`
- `"pendingDeliveriesQuantity": 0`
- `"lastFulfilledDeliveryAt"` timestamp

---

### Option 3: Using jq for Automatic ID Extraction

If you have `jq` installed, use this cleaner approach:

```bash
# Obtain token
TOKEN=$(curl -s -X POST "http://localhost:8082/realms/petreca-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=petreca-api-client" \
  -d "grant_type=password" \
  -d "username=geralt" \
  -d "password=witcher123" | jq -r '.access_token')

echo "Token obtained: ${TOKEN:0:20}..."

# Create courier and capture ID
COURIER_ID=$(curl -s -X POST http://localhost:9999/api/v1/couriers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Geralt of Rivia", "phone": "11987654321"}' | jq -r '.id')

echo "Courier ID: $COURIER_ID"

# Create delivery and capture ID
DELIVERY_ID=$(curl -s -X POST http://localhost:9999/api/v1/deliveries \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": {"zipCode": "12345-000", "street": "Rua do Remetente", "number": "10", "name": "Empresa A", "phone": "11999999999"},
    "recipient": {"zipCode": "54321-000", "street": "Avenida do Destinatário", "number": "20", "name": "Cliente B", "phone": "11888888888"},
    "items": [{"name": "Silver Sword", "quantity": 1}]
  }' | jq -r '.id')

echo "Delivery ID: $DELIVERY_ID"

# Place delivery
curl -X POST http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/placement \
  -H "Authorization: Bearer $TOKEN"

# Assign courier
curl -X POST http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/pickups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"courierId\": \"$COURIER_ID\"}"

# Complete delivery
curl -X POST http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/completion \
  -H "Authorization: Bearer $TOKEN"

# Verify
curl http://localhost:9999/api/v1/deliveries/$DELIVERY_ID \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

## 🛡️ Testing the Circuit Breaker — Watching the Shield Break

> *"The sword of destiny has two edges. One of them is you."* — So is the Circuit Breaker.

1. Stop the `Delivery-Tracking` service.
2. Fire several `GET` requests to `http://localhost:9999/api/v1/deliveries`.
3. The first requests will fail with `502 BAD_GATEWAY` (Retry exhausted).
4. After **5 failed calls**, the circuit **opens**. Subsequent requests fail instantly with `503 Service Unavailable` — no attempt is made to reach the dead service.
5. After **5 seconds**, the circuit enters `HALF_OPEN` and probes for recovery.

Watch the Gateway logs for state transitions: `CLOSED → OPEN → HALF_OPEN → CLOSED`.

---

## 🐦 Inspecting Kafka Events

Access **Kafka UI** at `http://localhost:8090` to explore the `deliveries.v1.events` topic.

After placing a delivery, you will see the `DeliveryPlacedEvent` message published by Delivery Tracking and consumed by Courier Management — the ravens have delivered their message.

---

## 🗄️ Inspecting the Database

> *"Knowledge is power. Guard it well."*

Access **pgAdmin** at `http://localhost:5050` to inspect the PostgreSQL databases.

**Login credentials:**
- Email: `admin@admin.com`
- Password: `admin`

The PostgreSQL server is **automatically configured** when you start the containers. You'll see:

**Server: PetrecaDelivery** (already connected)
- **Database: `courierdb`** → Courier Management data
  - Tables: `courier`, `assigned_delivery`
- **Database: `deliverydb`** → Delivery Tracking data
  - Tables: `delivery`, `item`, `contact_point`

### Viewing Data

**Option 1: Using the GUI**
1. Expand: `Servers` → `PetrecaDelivery` → `Databases` → `deliverydb` → `Schemas` → `public` → `Tables`
2. Right-click on `delivery` → "View/Edit Data" → "All Rows"

**Option 2: Using SQL Queries**

Right-click on `deliverydb` → "Query Tool" and run:

```sql
-- View all deliveries
SELECT id, status, courier_id, placed_at, fulfilled_at 
FROM delivery 
ORDER BY placed_at DESC;

-- View all couriers
SELECT c.id, c.name, c.phone, 
       c.fulfilled_deliveries_quantity, 
       c.pending_deliveries_quantity,
       c.last_fulfilled_delivery_at
FROM courierdb.public.courier c;

-- View pending deliveries for couriers
SELECT * FROM courierdb.public.assigned_delivery;
```

---

## 🔭 Observability — The Witcher's Senses

> *"A witcher's senses are heightened beyond those of ordinary men. So too must our platform see what others cannot."*

The platform is equipped with **production-grade observability** through Prometheus and Grafana, allowing you to monitor every heartbeat of your microservices in real-time.

### 🎯 What We Monitor

Every microservice exposes metrics via **Spring Boot Actuator** at `/actuator/prometheus`:

- **JVM Metrics**: Memory usage, garbage collection, thread pools
- **HTTP Metrics**: Request rates, response times, error rates
- **Resilience4j Metrics**: Circuit breaker states, retry attempts, rate limiter stats
- **Kafka Metrics**: Message production/consumption rates
- **Database Metrics**: Connection pool stats, query performance
- **Custom Business Metrics**: Deliveries created, couriers assigned, contracts fulfilled

### 📈 Prometheus — The Memory Keeper

**Prometheus** scrapes metrics from all microservices every **5 seconds** and stores them in a time-series database.

**Access:** `http://localhost:9090`

**Configuration:** All targets are automatically configured in `prometheus.yml`:
```yaml
scrape_configs:
  - job_name: 'petrecadelivery-microservices'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'host.docker.internal:8080' # Delivery-Tracking
        - 'host.docker.internal:8081' # Courier-Management
        - 'host.docker.internal:8761' # Service-Registry
        - 'host.docker.internal:9999' # API Gateway
```

**Try it:** Open Prometheus and run this query to see HTTP request rates:
```promql
rate(http_server_requests_seconds_count[1m])
```

### 📉 Grafana — The Storyteller

**Grafana** transforms raw metrics into beautiful, actionable dashboards.

**Access:** `http://localhost:3000`

**Login credentials:**
- Username: `admin`
- Password: `admin`

### ✨ Pre-Configured Dashboard

The platform includes a **pre-loaded Spring Boot dashboard** (ID: 4701) that visualizes:

- 🟢 **System Health**: CPU usage, memory consumption, uptime
- 🔵 **HTTP Performance**: Request throughput, latency percentiles (p50, p95, p99), error rates
- 🟡 **JVM Internals**: Heap/non-heap memory, garbage collection frequency, thread states
- 🟠 **Resilience Patterns**: Circuit breaker states, retry success/failure rates
- 🟣 **Database**: Active connections, query execution times

**How to access:**
1. Open Grafana at `http://localhost:3000`
2. Login with `admin` / `admin`
3. Navigate to **Dashboards** → **General**
4. Select **"JVM (Micrometer)"** dashboard
5. Use the dropdown at the top to switch between services:
   - `delivery-tracking`
   - `courier-management`
   - `gateway`
   - `service-registry`

### 🔍 Example Queries

**1. Total deliveries created (last 5 minutes):**
```promql
increase(http_server_requests_seconds_count{uri="/api/v1/deliveries", method="POST"}[5m])
```

**2. Average response time for delivery placement:**
```promql
rate(http_server_requests_seconds_sum{uri=~".*/placement"}[1m]) 
/ 
rate(http_server_requests_seconds_count{uri=~".*/placement"}[1m])
```

**3. Circuit breaker state (0=CLOSED, 1=OPEN, 2=HALF_OPEN):**
```promql
resilience4j_circuitbreaker_state
```

**4. JVM memory usage percentage:**
```promql
(jvm_memory_used_bytes / jvm_memory_max_bytes) * 100
```

### 🛡️ Automatic Configuration

Everything is **pre-configured** and **auto-provisioned**:

- ✅ **Prometheus datasource** automatically added to Grafana
- ✅ **Dashboard** automatically imported on startup
- ✅ **Scrape targets** automatically configured in Prometheus
- ✅ **Metrics endpoints** automatically exposed by Spring Boot Actuator

**No manual setup required.** Just start the services and open Grafana.

### 🎯 Real-World Use Cases

**Scenario 1: Detecting Performance Degradation**
- Run the test script multiple times
- Watch the "HTTP Request Duration" panel in Grafana
- If p99 latency spikes, investigate slow database queries or external API calls

**Scenario 2: Monitoring Circuit Breaker Behavior**
- Stop the Delivery-Tracking service
- Fire requests through the Gateway
- Watch the circuit breaker transition from `CLOSED` → `OPEN` in real-time
- See retry attempts and failure rates in the dashboard

**Scenario 3: Capacity Planning**
- Monitor JVM heap usage over time
- Track HTTP request rates during peak load
- Identify when to scale horizontally (add more instances)

### 📚 Further Reading

Want to create custom dashboards?
- [Prometheus Query Language (PromQL)](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Grafana Dashboard Best Practices](https://grafana.com/docs/grafana/latest/dashboards/)
- [Spring Boot Actuator Metrics](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics)

---

## 🙏 Acknowledgements

> *"People call it a stigma. I call it a sign of wisdom."*

This platform was born from the **"Mergulho Microsserviços Spring"** immersion by [**Algaworks**](https://www.algaworks.com/). My deepest gratitude to masters [**Alex Augusto**](https://github.com/alexaugustobr) and [**Thiago Faria de Andrade**](https://github.com/thiagofa) — whose knowledge and patience transformed complex concepts into battle-tested skills.

Feel free to explore, test, open issues, or submit pull requests. The notice board is always open.

---

<div align="center">

*"Not all those who wander are lost — some are just tracing delivery routes."*

**⚔️ Toss a coin to your witcher ⚔️**

</div>
