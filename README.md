# 🎉 Event Management Portal

A full-stack Event Management Portal that allows organizers to create and manage events and attendees to browse, book, and manage their bookings. Built with **Spring Boot (Java 17)** for the backend and **HTML/CSS/JavaScript** for the frontend.

---

## 🚀 Features

### 👤 User Authentication
- Secure JWT-based login
- BCrypt password encryption
- Role-based access control (Attendee, Organizer)

### 🎟️ Event Management (Organizer)
- Create, edit, cancel events
- Set event capacity
- View all bookings per event
- Auto-close bookings when full


### 📅 Event Booking (Attendee)
- View event list with availability
- Book event slots
- View booking history

---

## 🛠️ Tech Stack

### Backend
- Java 17 + Spring Boot 3.1.5
- Spring Security 6.1 (JWT Auth)
- PostgreSQL

### Frontend
- HTML, CSS, JavaScript
- Dynamic UI based on user role
- API integration with backend

---
## 🔐 Authentication Details

- JWT token stored in `localStorage`
- Role and email decoded from token
- Frontend dynamically updates UI based on role

---

## 📬 Postman Collection

You can test all available API endpoints using our Postman collection:

👉 **[Download Postman Collection](./Event-Management-Portal.postman_collection)**

### How to Use:
1. Download the collection file
2. Import it into Postman:
    - Open Postman
    - Click **Import** > Choose **File**
    - Select the downloaded `.json` file
3. Set the base URL (e.g., `http://localhost:8080`)
4. Test endpoints like registration, login, event creation, booking, etc.

> ⚠️ Make sure the backend server is running before making API calls.
---

## 🧪 Running Locally

### Backend

1. Clone the repo
2. Configure PostgreSQL DB in `application.properties`
3. Run the Spring Boot application

