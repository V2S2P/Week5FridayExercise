# 🔐 JSON Web Token (JWT)

## Overview
**JWT (JSON Web Token)** is a compact and secure way to transmit information between a client and a server as a **digitally signed token**.  
It is widely used for **authentication** and **authorization** in web applications.

---

## 🧩 Structure of a JWT

A JWT consists of **three parts**, separated by dots:

xxxxx.yyyyy.zzzzz

These parts are:
1. **Header**
2. **Payload (Body)**
3. **Signature**

### 1️⃣ Header
The **header** contains metadata about the token, such as the algorithm used for signing and the token type.

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
This is then Base64URL encoded.
```

### 2️⃣ Payload (Body)
The payload carries the claims, which are statements about the user or entity (e.g., user ID, roles, expiration time).

```json
{
  "sub": "johndoe",
  "roles": ["USER"],
  "iat": 1696520000,
  "exp": 1696523600
}
```
```plaintext
Common claims:

sub → Subject (usually the username or user ID)

iat → Issued At (timestamp)

exp → Expiration Time

roles → User roles or permissions

This section is also Base64URL encoded.
```

### 3️⃣ Signature
```plaintext
The signature ensures that the token hasn’t been modified.

It’s generated using a secret key known only to the server:

HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  SECRET_KEY
)

The resulting JWT looks like this:

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJqb2huZG9lIiwicm9sZXMiOlsiVVNFUiJdLCJpYXQiOjE2OTY1MjAwMDAsImV4cCI6MTY5NjUyMzYwMH0.
abc123XYZsignature
```
### 🔄 How JWT Works
#### Step 1: User Logs In
When a user logs in successfully, the server generates a JWT and sends it back to the client.

```java
String token = tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
ctx.json(Map.of("token", token, "username", user.getUsername()));
```
Example response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
  "username": "johndoe"
}
```
#### Step 2: Client Stores the Token
The client stores the token (e.g., in localStorage or a cookie) and includes it in the Authorization header for future requests.

```plaintext
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...
```
#### Step 3: Server Verifies the Token
For each protected endpoint, the server:
```plaintext
Extracts the token from the header.

Verifies the signature using the secret key.

Checks the expiration time (exp).

Retrieves user info (e.g., username, roles) from the token payload.
```

Example (Java/Javalin):

```java
app.beforeMatched(securityController.authenticate());

public Handler authenticate() {
    return ctx -> {
        String authHeader = ctx.header("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            DecodedJWT jwt = tokenSecurity.verifyToken(token, SECRET_KEY);
            ctx.attribute("user", jwt.getClaim("sub").asString());
        } else {
            ctx.status(401).result("Unauthorized");
        }
    };
}
```
### ✅ Advantages of JWT
```plaintext
Stateless Authentication – No need to store session data on the server.

Tamper-Proof – Signed with a secret key.

Compact & URL-Safe – Easy to send in HTTP headers or cookies.

Self-Contained – Contains all necessary user info (like roles and expiry).
```

### ⚠️ Common Pitfalls
```plaintext
Not encrypted, only encoded. Anyone can decode the payload with tools like jwt.io.
→ Never store sensitive information (like passwords) inside the token.

Token expiration is important. Always include exp.

Secret key management: Keep it safe and private.
```

### 🧠 Summary
```plaintext
Component	Purpose	Example
Header	Metadata (algorithm, type)	{ "alg": "HS256", "typ": "JWT" }
Payload	Claims (user data, expiration)	{ "sub": "johndoe", "exp": 1696523600 }
Signature	Verifies integrity	HMACSHA256(header + payload, secret)
```
### 🔒 Typical JWT Flow
```plaintext
[1] Client sends login credentials → Server verifies user
[2] Server creates and signs JWT → Sends it to client
[3] Client stores JWT (localStorage/cookie)
[4] Client includes JWT in Authorization header → Server verifies on each request
[5] If valid → Access granted
````
### 🧰 Tools & References
```plaintext
jwt.io – Decode and verify JWTs online

RFC 7519 – JWT specification

Auth0 JWT Guide
```