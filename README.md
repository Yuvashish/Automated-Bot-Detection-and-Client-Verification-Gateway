**Automated Bot Detection and Client Verification**

**Automated Bot Detection and Client Verification Gateway** project is a custom Web Application Firewall (WAF) layer designed to protect backend services against automated attacks (like credential stuffing, scraping, and DDoS) without degrading performance or relying on intrusive visual CAPTCHAs

I built a lightweight security gateway that acts as a gatekeeper in front of sensitive API endpoints. Instead of asking users to click on traffic lights, it uses invisible Proof-of-Work crypto challenges and JS fingerprinting to force automated bots to spend expensive CPU cycles before gaining access. It backs this up with sub-millisecond Redis rate-limiting and anomaly detection to automatically block suspicious traffic patterns."

**Download the Repositories and run these commands in Integrated Bash Terminal.**

***Run Docker containers for Redis and PostgreSQL before launching Spring Boot:***

docker run -d --name redis-local -p 6379:6379 redis:alpine

docker run -d --name postgres-local -e POSTGRES_DB=bot_gateway -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:alpine

***Now you can start your project using Maven:***
./mvnw spring-boot:run

**Test Verification Flow:**

Open a new terminal tab (or use tools like Postman / cURL) to verify your endpoints:
***1. Request a PoW Challenge***
curl -X GET "http://localhost:8080/api/challenge?clientId=user123"

Expected Response: {"nonce":"<generated-uuid>","difficulty":"4","algorithm":"SHA-256"}

***2. Test Access to a Protected Resource Without a Solution***
curl -i -X GET "http://localhost:8080/api/protected/resource" \  -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)"

Expected Response: HTTP/1.1 401 Unauthorized with {"error": "Challenge verification failed or missing."}

***3. Test Anomaly Detection (Low Entropy / Bot Header)***
curl -i -X GET "http://localhost:8080/api/challenge?clientId=test" \  -H "User-Agent: bot"

Expected Response: HTTP/1.1 403 Forbidden with {"error": "Suspicious Request Signature"}
