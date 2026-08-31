# **Automated Bot Detection and Client Verification Gateway**

**Automated Bot Detection and Client Verification Gateway** project is a custom Web Application Firewall (WAF) layer designed to protect backend services against automated attacks (like credential stuffing, scraping, and DDoS) without degrading performance or relying on intrusive visual CAPTCHAs

I built a lightweight security gateway that acts as a gatekeeper in front of sensitive API endpoints. Instead of asking users to click on traffic lights, it uses invisible Proof-of-Work crypto challenges and JS fingerprinting to force automated bots to spend expensive CPU cycles before gaining access. It backs this up with sub-millisecond Redis rate-limiting and anomaly detection to automatically block suspicious traffic patterns."

**Tech Stack:** Python (FastAPI) or Java (Spring Boot), Redis, PostgreSQL, Docker, Hashlib/Crypto libraries.

### Prerequisites

* **Java JDK 17** or higher
* **Docker Desktop** (or Homebrew services for Redis & PostgreSQL)
* **cURL** or **Postman** for API testing

---

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-username/bot-detection-gateway.git
cd bot-detection-gateway/gateway
```

---

### Step 2: Start Infrastructure Services

Run Redis and PostgreSQL using Docker:

```bash
# Spin up Redis Container
docker run -d --name redis-local -p 6379:6379 redis:alpine

# Spin up PostgreSQL Container
docker run -d --name postgres-local -e POSTGRES_DB=bot_gateway -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:alpine
```

*(Alternatively, run native macOS background services via Homebrew: `brew services start redis` and `brew services start postgresql@14`)*

---

### Step 3: Configure Database Connection

Verify settings in `src/main/resources/application.properties`:

```properties
spring.application.name=gateway

# Database Setup
spring.datasource.url=jdbc:postgresql://localhost:5432/bot_gateway
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update

# Redis Setup
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

---

### Step 4: Build and Run

Grant execution permissions to the Maven wrapper and start the application:

```bash
chmod +x mvnw
./mvnw clean spring-boot:run
```

The application will start on `http://localhost:8080`.

---

## API Usage & Testing

### 1. Request a Proof-of-Work (PoW) Challenge

Issues a single-use nonce for client challenge computation:

```bash
curl -X GET "http://localhost:8080/api/challenge?clientId=user123"
```

**Sample Response (`200 OK`):**
```json
{
  "nonce": "e3b0c442-98fc-4c14-92e0-24da6e336e8b",
  "difficulty": "4",
  "algorithm": "SHA-256"
}
```

---

### 2. Access Protected Resource (Without Challenge Headers)

Attempting to access protected endpoints without solving the challenge triggers a `401 Unauthorized`:

```bash
curl -i -X GET "http://localhost:8080/api/protected/resource"   -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"
```

**Sample Response (`401 Unauthorized`):**
```json
{
  "error": "Challenge verification failed or missing."
}
```

---

### 3. Test Anomaly Detection (Low Header Entropy)

Requests sent with suspicious, simple, or bot-like `User-Agent` strings are blocked by entropy filters:

```bash
curl -i -X GET "http://localhost:8080/api/challenge?clientId=test"   -H "User-Agent: bot"
```

**Sample Response (`403 Forbidden`):**
```json
{
  "error": "Suspicious Request Signature"
}
```

---

## Proof-of-Work Protocol Logic

1. **Server Challenge:** Server issues a random UUID `nonce` with a expiration TTL (e.g., 60 seconds) in Redis.
2. **Client Execution:** Client calculates a string `solution` such that:
   $$	ext{SHA-256}(	ext{nonce} + 	ext{solution}) 	ext{ starts with } 	ext{"0000"}$$
3. **Verification:** Client passes `X-POW-Nonce` and `X-POW-Solution` headers in the request. The gateway computes the hash, verifies the prefix match, and invalidates the nonce in Redis to prevent replay attacks.
