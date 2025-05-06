# CineReserve Backend

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue)
![License](https://img.shields.io/badge/license-MIT-brightgreen)

## 📋 Overview

CineReserve Backend is a comprehensive cinema management system built with Spring Boot that provides a robust API for managing movie theaters, screenings, reservations and user accounts. The system integrates with The Movie Database (TMDB) API to fetch up-to-date movie information.

## ✨ Features

- **User Authentication & Authorization**
    - JWT-based authentication
    - Role-based access control (User/Admin)
    - User registration and profile management

- **Movie Management**
    - Integration with TMDB API for movie data
    - Automatic fetching of movie details, posters, and trailers
    - Movie categorization by genres

- **Cinema Management**
    - Room administration (creating, updating, deleting)
    - Dynamic seat generation based on room configuration
    - Seat reservation system

- **Screening Management**
    - Schedule movie screenings
    - Associate screenings with rooms and movies

- **Reservation System**
    - Seat selection and reservation
    - User-linked reservations

## 🛠️ Technologies

- **Java 17**
- **Spring Boot 3.4.4**
    - Spring Data JPA
    - Spring Security
    - Spring Validation
    - Spring WebFlux
- **PostgreSQL** - Database
- **JWT** - Authentication mechanism
- **Lombok** - Reduces boilerplate code
- **dotenv-java** - Environment variable management

## 📦 Project Structure

```
CineReserveBackend/
├── src/main/java/org/example/backend/
│   ├── controller/ - REST API endpoints
│   ├── dto/ - Data Transfer Objects
│   ├── model/ - Entity definitions
│   │   └── enums/ - Enumeration types
│   ├── repository/ - Data access layers
│   ├── service/ - Business logic
│   │   └── TMDBService - External movie API integration
│   └── BackendApplication.java - Main application class
└── src/main/resources/
    └── application.properties - Application configuration
```

## 📋 Prerequisites

- Java Development Kit (JDK) 17 or higher
- PostgreSQL Database
- Maven
- TMDB API Key

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/juanmariiaa/CineReserveBackend.git
cd CineReserveBackend
```

### 2. Configure Environment Variables

Create a `.env` file in the project root with:

```
TMDB_API_KEY=your_tmdb_api_key_here
```

### 3. Configure Database

Ensure PostgreSQL is running, then create a database named `cinema`:

```sql
CREATE DATABASE cinema;
```

Update `application.properties` if needed (default credentials are root/root).

### 4. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

The server will start on port 8080 (http://localhost:8080).

## 🔐 Security

The application uses JWT (JSON Web Token) for authentication. Protected endpoints require a valid JWT token in the request header:

```
Authorization: Bearer {token}
```

## 🔄 API Endpoints

### Authentication
- `POST /api/auth/login` - Authenticate user
- `POST /api/auth/register` - Register new user

### Movies
- `GET /api/movies` - List all movies
- `GET /api/movies/{id}` - Get movie details
- `POST /api/movies/import/{tmdbId}` - Import movie from TMDB

### Rooms
- `GET /api/rooms` - List all rooms
- `GET /api/rooms/{id}` - Get room details
- `GET /api/rooms/number/{number}` - Get room by number
- `POST /api/rooms` - Create new room
- `DELETE /api/rooms/{id}` - Delete room (admin only)
- `DELETE /api/rooms/delete-highest` - Delete room with highest number (admin only)

### Screenings
- `GET /api/screenings` - List all screenings
- `GET /api/screenings/{id}` - Get screening details
- `POST /api/screenings` - Create new screening

### Reservations
- `GET /api/reservations` - List user's reservations
- `GET /api/reservations/{id}` - Get reservation details
- `POST /api/reservations` - Create new reservation

## 🔧 Database Schema

The system is built around these primary entities:
- **Users** - Manages authentication and roles
- **Movies** - Stores movie information from TMDB
- **Genres** - Categorizes movies
- **Rooms** - Represents theater rooms
- **Seats** - Represents individual seats in rooms
- **Screenings** - Links movies with rooms at specific times
- **Reservations** - Stores user seat bookings for screenings

## 🧪 Testing

Run the tests with:

```bash
mvn test
```


## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Contact

Juan Maria - [GitHub Profile](https://github.com/juanmariiaa)

Project Link: [https://github.com/juanmariiaa/CineReserveBackend](https://github.com/juanmariiaa/CineReserveBackend)