# Hotel Management System - Notes & Architecture

## Overview
This project manages hotels and their rooms using Java, Javalin, and Hibernate.  
The application is structured to follow **Separation of Concerns (SoC)** principles, with distinct layers for **Entities**, **DTOs**, **Mappers**, **DAO**, **Service**, and **Controller**.  

---

## Layers & Responsibilities

### 1. **Entity Layer**
- Represents database tables using JPA entities.
- Example: `Hotel` and `Room`.
- Contains only **database-related mappings**.
- No business logic or conversion logic.

### 2. **DTO Layer**
- Represents objects exchanged between client and server.
- Contains **no conversion logic** or database logic.
- Example: `HotelDTO`, `RoomDTO`.

### 3. **Mapper Layer**
- Converts between Entities and DTOs.
- Example: `HotelMapper`, `RoomMapper`.
- **Single responsibility:** only mapping.

### 4. **DAO Layer**
- Handles all database interactions using JPA/Hibernate.
- Example: `HotelDAO`.
- Methods use **JPQL queries** with intentional eager/lazy fetching.
- No business logic or conversion to DTO.

### 5. **Service Layer**
- Handles business logic.
- Calls DAO methods to fetch or modify data.
- Converts entities to DTOs via Mapper.
- Example: `HotelService`.
- **Responsibilities:**  
  - Validate input data  
  - Apply business rules  
  - Call DAOs  
  - Map Entities to DTOs

### 6. **Controller Layer**
- Handles HTTP requests/responses.
- Calls Service methods.
- No direct DB access or conversion logic.
- Example: `HotelController`.

### 7. **Routes**
- Defines HTTP endpoints and maps them to Controller methods.
- Example: `HotelRoutes`.

---

## Important Design Decisions

- **Eager vs Lazy Loading:**  
  - Use `JOIN FETCH` explicitly in DAO queries for relationships needed immediately.  
  - Avoid relying on lazy loading in Service/Controller to prevent `LazyInitializationException`.

- **Separation of Concerns:**  
  - DAO only accesses DB.  
  - Mapper only converts Entities ↔ DTOs.  
  - Service contains business logic and calls Mapper.  
  - Controller handles HTTP layer only.

- **Handling Non-existent Resources:**  
  - Service throws exceptions or returns `null` for missing entities.  
  - Controller handles this and responds with appropriate HTTP status (e.g., 404).

- **Thread Safety / EntityManager:**  
  - Always use `try-with-resources` for `EntityManager` to ensure session closure.  

---

## Example Query with Intent
```java
// Fetch hotel with rooms eagerly
TypedQuery<Hotel> query = em.createQuery(
    "SELECT h FROM Hotel h LEFT JOIN FETCH h.rooms WHERE h.id = :id", Hotel.class
);
query.setParameter("id", hotelId);
Hotel hotel = query.getSingleResult();
