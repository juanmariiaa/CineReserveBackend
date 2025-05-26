# CineReserve Backend

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue)
![Stripe](https://img.shields.io/badge/Stripe-Payments-6772E5)
![iText](https://img.shields.io/badge/iText-PDF-yellow)
![ZXing](https://img.shields.io/badge/ZXing-QR-black)
![License](https://img.shields.io/badge/license-MIT-brightgreen)

## 📋 Overview

CineReserve Backend is a comprehensive cinema management system built with Spring Boot that provides a robust API for managing movie theaters, screenings, reservations, and user accounts. The system integrates with The Movie Database (TMDB) API to fetch up-to-date movie information and offers advanced features like Stripe payment processing and PDF ticket generation with unique QR codes.

## ✨ Features

- **User Authentication & Authorization**

  - JWT-based authentication
  - Role-based access control (User/Admin)
  - User registration and profile management

- **Movie Management**

  - Integration with TMDB API for movie data
  - Automatic fetching of movie details, posters, and trailers
  - Movie categorization by genres and tags

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
  - Reservation status tracking (PENDING, CONFIRMED, CANCELLED, COMPLETED)

- **Stripe Payment Processing**

  - Secure payment integration with Stripe
  - Automatic reservation confirmation after successful payment
  - Webhook management for transaction status updates

- **QR-Coded Ticket Generation**

  - Automatic PDF ticket creation after payment
  - Unique QR codes for each ticket with reservation information
  - Email delivery of tickets

- **Email Notifications**
  - Reservation confirmations
  - Digital tickets
  - Screening reminders

## 🛠️ Technologies

- **Java 17**
- **Spring Boot 3.4.4**
  - Spring Data JPA
  - Spring Security
  - Spring Validation
  - Spring WebFlux
  - Spring Mail
- **PostgreSQL** - Database
- **JWT** - Authentication mechanism
- **Stripe API** - Payment processing
- **iText 7** - PDF document generation
- **ZXing** - QR code generation
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
│   ├── service/
│   │   ├── TMDBService/ - External movie API integration
│   │   ├── StripeService/ - Payment processing services
│   │   ├── TicketService/ - PDF ticket generation with QR codes
│   │   ├── EmailService/ - Email sending service
│   │   └── PaymentService/ - Payment process coordination
│   └── BackendApplication.java - Main application class
└── src/main/resources/
    ├── application.properties - Application configuration
    ├── templates/ - Email and PDF templates
    └── static/ - Static resources (images, CSS)
```

## 📋 Prerequisites

- Java Development Kit (JDK) 17 or higher
- PostgreSQL Database
- Maven
- TMDB API Key
- Stripe account with API keys
- SMTP email account for sending emails

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/juanmariiaa/CineReserveBackend.git
cd CineReserveBackend
```

### 2. Configure Environment Variables

Create a `.env` file in the project root with:

```
# API Keys
TMDB_API_KEY=your_tmdb_api_key_here
STRIPE_SECRET_KEY=your_stripe_secret_key_here
STRIPE_PUBLIC_KEY=your_stripe_public_key_here
STRIPE_WEBHOOK_SECRET=your_stripe_webhook_secret_here

# Email configuration
MAIL_HOST=smtp.yourserver.com
MAIL_PORT=587
MAIL_USERNAME=your_email@domain.com
MAIL_PASSWORD=your_password
MAIL_SENDER=CineReserve <noreply@cinereserve.com>

# Base URL for redirects and QR generation
BASE_URL=http://localhost:4200
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

### Protected Routes

The following routes require authentication:

- All `/api/reservations/**` endpoints - Require USER role
- All `/api/payments/**` endpoints - Require USER role
- All `/api/user/**` endpoints - Require USER role
- All `/api/admin/**` endpoints - Require ADMIN role
- POST, PUT, DELETE on `/api/movies/**` - Require ADMIN role
- POST, PUT, DELETE on `/api/rooms/**` - Require ADMIN role
- POST, PUT, DELETE on `/api/screenings/**` - Require ADMIN role

Public routes (no authentication required):

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/movies/**`
- `GET /api/screenings/**`
- `GET /api/rooms/**`

## 🔄 API Endpoints

### Authentication

- `POST /api/auth/login` - Authenticate user
- `POST /api/auth/register` - Register new user
- `GET /api/auth/verify` - Verify JWT token
- `POST /api/auth/refresh` - Refresh JWT token

### Users

- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/profile` - Update user profile
- `PUT /api/users/password` - Change password
- `GET /api/admin/users` - List all users (ADMIN only)
- `GET /api/admin/users/{id}` - Get user details (ADMIN only)
- `PUT /api/admin/users/{id}/role` - Update user role (ADMIN only)

### Movies

- `GET /api/movies` - List all movies
- `GET /api/movies/{id}` - Get movie details
- `GET /api/movies/search` - Search movies by title, genre, etc.
- `GET /api/movies/trending` - Get trending movies
- `POST /api/movies/import/{tmdbId}` - Import movie from TMDB (ADMIN only)
- `POST /api/movies` - Create new movie (ADMIN only)
- `PUT /api/movies/{id}` - Update movie (ADMIN only)
- `DELETE /api/movies/{id}` - Delete movie (ADMIN only)

### Genres

- `GET /api/genres` - List all genres
- `GET /api/genres/{id}` - Get genre details
- `POST /api/genres` - Create new genre (ADMIN only)
- `PUT /api/genres/{id}` - Update genre (ADMIN only)
- `DELETE /api/genres/{id}` - Delete genre (ADMIN only)

### Rooms

- `GET /api/rooms` - List all rooms
- `GET /api/rooms/{id}` - Get room details
- `GET /api/rooms/number/{number}` - Get room by number
- `POST /api/rooms` - Create new room (ADMIN only)
- `PUT /api/rooms/{id}` - Update room (ADMIN only)
- `DELETE /api/rooms/{id}` - Delete room (ADMIN only)
- `DELETE /api/rooms/delete-highest` - Delete room with highest number (ADMIN only)

### Seats

- `GET /api/rooms/{roomId}/seats` - Get all seats for a room
- `GET /api/seats/{id}` - Get seat details
- `POST /api/rooms/{roomId}/seats/generate` - Generate seats for a room (ADMIN only)
- `PUT /api/seats/{id}` - Update seat (ADMIN only)
- `DELETE /api/seats/{id}` - Delete seat (ADMIN only)

### Screenings

- `GET /api/screenings` - List all screenings
- `GET /api/screenings/{id}` - Get screening details
- `GET /api/screenings/movie/{movieId}` - Get screenings for a movie
- `GET /api/screenings/date/{date}` - Get screenings for a date
- `POST /api/screenings` - Create new screening (ADMIN only)
- `PUT /api/screenings/{id}` - Update screening (ADMIN only)
- `DELETE /api/screenings/{id}` - Delete screening (ADMIN only)

### Reservations

- `GET /api/reservations` - List user's reservations
- `GET /api/reservations/{id}` - Get reservation details
- `GET /api/reservations/screening/{screeningId}` - Get reservations for a screening
- `POST /api/reservations` - Create new reservation
- `PUT /api/reservations/{id}/cancel` - Cancel reservation
- `GET /api/admin/reservations` - List all reservations (ADMIN only)

### Payments

- `POST /api/payments/create-session` - Create Stripe payment session
- `GET /api/payments/success` - Endpoint for successful payment redirect
- `GET /api/payments/cancel` - Endpoint for payment cancellation redirect
- `POST /api/payments/webhook` - Stripe webhook endpoint
- `GET /api/payments/download-ticket/{reservationId}` - Download ticket PDF
- `GET /api/payments/history` - Get user's payment history
- `GET /api/admin/payments` - List all payments (ADMIN only)

## 💳 Stripe Integration

### Stripe Setup

1. **Create a Stripe Account**: Sign up at [Stripe Dashboard](https://dashboard.stripe.com/register).

2. **Get your API Keys**: Located in Dashboard > Developers > API keys.

   - Use test keys for development (`sk_test_...` and `pk_test_...`)
   - Configure keys in the `.env` file

3. **Set up Webhook**:
   - In Stripe dashboard: Developers > Webhooks > Add endpoint
   - URL: `https://your-domain.com/api/payments/webhook`
   - Events to listen for: `checkout.session.completed`, `payment_intent.succeeded`, `payment_intent.payment_failed`
   - Save the webhook secret key in your `.env` file

### Payment Flow

1. Customer selects seats and confirms the reservation
2. Backend creates a Stripe payment session with reservation details
3. Customer is redirected to Stripe's payment page
4. After payment, Stripe redirects the customer to the success/cancel URL
5. Stripe sends an event to the webhook that updates the reservation status
6. After a successful payment, the system automatically generates a PDF ticket with QR code and sends it via email

### Integration Details

Our Stripe integration uses Stripe Checkout to provide a secure, pre-built payment page. The flow works as follows:

1. **Session Creation**: When a user confirms a reservation, the frontend calls `/api/payments/create-session` with the reservation details.

2. **Payment Processing**: The backend creates a Checkout Session with the following parameters:

   - Line items (movie title, screening time, selected seats)
   - Success and cancel URLs for redirects
   - Metadata containing the reservation ID and user information
   - Payment intent data capture method

3. **Session ID Return**: The backend returns the Stripe Session ID to the frontend.

4. **Redirect to Checkout**: The frontend redirects the user to the Stripe Checkout page using the Session ID.

5. **Webhook Processing**: Once payment is completed, Stripe sends an event to our webhook endpoint. The backend:

   - Verifies the webhook signature for security
   - Processes the event based on its type
   - Updates the reservation status to CONFIRMED
   - Generates the ticket PDF with QR code
   - Sends the ticket via email to the user

6. **Success Page**: The user is redirected to a success page that shows the ticket information and provides a download link.

### Implementation Example

```java
// Example code for creating a payment session with Stripe
@PostMapping("/create-session")
public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody ReservationPaymentRequest request) {
    try {
        String sessionId = stripeService.createCheckoutSession(
            request.getReservationId(),
            request.getSuccessUrl(),
            request.getCancelUrl()
        );

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

// Webhook processing
@PostMapping("/webhook")
public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
    try {
        Event event = stripeService.constructEvent(payload, sigHeader);

        // Handle the event based on its type
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().get();
            stripeService.processSuccessfulPayment(session);
        }

        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook Error: " + e.getMessage());
    }
}
```

## 🎟️ QR-Coded Ticket Generation

The system automatically generates PDF tickets with unique QR codes when a payment is completed. These PDFs are emailed to the user and can also be downloaded from the platform.

### Ticket Features

- **Complete Information**: Movie title, room, seats, date and time
- **Unique QR Code**: Generated with encrypted reservation information
- **Customizable Design**: Adaptable PDF template with cinema branding
- **Security**: Verification at the door by scanning the QR code

### Generation Process

1. When a payment is confirmed, the `PaymentService` notifies the `TicketService`
2. The `TicketService` retrieves the reservation details, including movie, room, seats, and user
3. A unique code for the reservation is generated and encoded in the QR
4. A PDF document is created with iText 7, including all information and the QR code
5. The PDF is emailed to the user using the `EmailService`

### Implementation Example

```java
// Simplified example of PDF generation with QR code
public byte[] generateTicket(Reservation reservation) {
    // Create PDF document
    PdfDocument pdf = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
    Document document = new Document(pdf);

    // Add reservation information
    document.add(new Paragraph("MOVIE TICKET")
        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
        .setFontSize(24));

    document.add(new Paragraph("Movie: " + reservation.getScreening().getMovie().getTitle()));
    document.add(new Paragraph("Date: " + formatDate(reservation.getScreening().getStartTime())));
    document.add(new Paragraph("Room: " + reservation.getScreening().getRoom().getNumber()));

    // Generate QR code information
    String qrContent = "ID:" + reservation.getId() +
                      "|USER:" + reservation.getUser().getId() +
                      "|DATE:" + reservation.getScreening().getStartTime().getTime();

    // Generate QR code
    BarcodeQRCode qrCode = new BarcodeQRCode(qrContent);
    PdfFormXObject qrCodeObject = qrCode.createFormXObject(ColorConstants.BLACK, pdf);
    Image qrCodeImage = new Image(qrCodeObject).setWidth(100).setHeight(100);

    document.add(qrCodeImage);
    document.close();

    return ((ByteArrayOutputStream) pdf.getWriter().getOutputStream()).toByteArray();
}
```

## 📧 Email Service Configuration

### Configuration in application.properties

```properties
# JavaMailSender Configuration
spring.mail.host=${MAIL_HOST}
spring.mail.port=${MAIL_PORT}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Default sender
app.email.sender=${MAIL_SENDER}
```

## 🔧 Database Schema

The system is built around these primary entities:

- **users** - Stores user accounts and authentication details
- **roles** - Defines system roles (USER, ADMIN)
- **user_role** - Junction table linking users to roles
- **movie** - Stores movie information from TMDB
- **genre** - Defines movie genres
- **movie_genre** - Junction table linking movies to genres
- **movie_tags** - Stores tags associated with movies
- **room** - Represents cinema rooms
- **seat** - Represents individual seats in rooms
- **screening** - Links movies with rooms at specific times
- **reservation** - Stores user seat bookings for screenings
- **seat_reservation** - Junction table linking reservations to seats
- **payment** - Stores payment transaction information

### Entity Relationships

- A **user** can have multiple **roles**
- A **movie** can have multiple **genres** and **tags**
- A **room** has multiple **seats**
- A **screening** belongs to one **movie** and one **room**
- A **reservation** belongs to one **user** and one **screening**
- A **reservation** can include multiple **seats**
- A **payment** is associated with one **reservation**

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Contact

Juan Maria - [GitHub Profile](https://github.com/juanmariiaa)

Project Link: [https://github.com/juanmariiaa/CineReserveBackend](https://github.com/juanmariiaa/CineReserveBackend)
