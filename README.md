# CustomAuthenticationSPI

This project demonstrates how to implement and register a **custom SPI (Service Provider Interface)** in Keycloak 26,
using Docker Compose for full environment orchestration.

Included in this setup:
- 🔐 A custom Keycloak SPI that authenticates users via a custom token in the X-Konneqt-Token header
- 🧾 A pre-configured Keycloak realm: `test`
- 🐘 PostgreSQL database
- 🌐 React-based frontend for testing token-based login flow
- ⚙️ Fully automated flow using Makefile commands

---

## 📁 Project Structure

```
CustomAuthenticationSPI/
├── konneqt-front/              # React frontend to input email and receive user profile token
├── realm-export.json           # Predefined Keycloak realm
├── docker-compose.yml          # All-in-one service orchestrator
├── Makefile                    # Automation of SPI build/deploy
├── src/                        # Java SPI source code
└── target/                     # Compiled .jar after build
```

---

## 🧰 Prerequisites

- Docker
- Docker Compose
- Java 17+
- Maven 3.8+
- GNU Make

---

## 🚀 How to Start

```bash
# 1. Clone the repository
git clone https://github.com/leonardombr89/CustomAuthenticationSPI.git
cd CustomAuthenticationSPI

# 2. Run the full setup with Make
make start-all
```

This command will:
- Build the SPI with Maven
- Start PostgreSQL and Keycloak
- Copy the SPI jar to the container
- Register and build the provider
- Import the realm `test`
- Start the React frontend

---

## 🌍 Access URLs

- **Frontend:** http://localhost:3000
- **Keycloak:** http://localhost:8080
- **Admin Console:** http://localhost:8080/admin (user: admin / password: admin)

---

## 🧪 How It Works

1. User enters their email on the frontend
2. A JWT is generated and sent in the X-Konneqt-Token header to the SPI
3. If the user doesn't exist, they're automatically created in the Keycloak `test` realm
4. The SPI returns a new token, redirecting the user to `/profile` with the token as a query param

---

## 🧪 Testing the Integration

### ✅ Option 1: Test via Frontend (Recommended)

1. Navigate to http://localhost:3000
2. Enter a valid email
3. The frontend will:
    - Generate a JWT
    - Send it to the SPI
    - Redirect you to `/profile` with a valid token
4. The page will decode and display the token content.

### 🧪 Option 2: Test via Postman or curl

You can manually test the SPI endpoint using a generated JWT:

```bash
curl --location --request POST 'http://localhost:8080/realms/test/authenticator/konneqt' \
  --header 'X-Konneqt-Token: PUT_YOUR_TOKEN_HERE'
```

**JWT Configuration:**
- Tool: http://jwtbuilder.jamiekurtz.com/
- Secret key: `super-strong-key-that-is-very-secure!`
- Algorithm: `HS256`

**Example Payload:**

```json
{
  "iss": "Online JWT Builder",
  "iat": CURRENT_UNIX_TIMESTAMP,
  "exp": FUTURE_UNIX_TIMESTAMP,
  "aud": "",
  "sub": "user@example.com"
}
```

Use the generated token in the `X-Konneqt-Token` header and you'll receive a redirect with a valid token from Keycloak.

---

## 🔧 Makefile Commands

| Command              | Description                                  |
|----------------------|----------------------------------------------|
| `make build-spi`     | Build the SPI `.jar` with Maven              |
| `make copy-spi`      | Copy the `.jar` into the Keycloak container  |
| `make rebuild-keycloak` | Rebuild Keycloak with the new SPI         |
| `make up`            | Start all Docker services                    |
| `make start-all`     | Full flow: build SPI, register it, up all    |

---

## 🧼 Clean Up

To stop and remove all containers, networks and volumes:

```bash
docker-compose down -v
```
