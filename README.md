# 🚀 InfraMind Backend

A Spring Boot backend for **InfraMind**, an AI-powered Public Infrastructure Reporting Platform that enables citizens to report civic issues and allows government authorities to manage, assign, and resolve complaints efficiently.

---

## ✨ Features

- 🔐 JWT Authentication & Authorization
- 👥 Role-Based Access Control (Citizen, Officer, Manager, Admin)
- 📌 Complaint Management
- 📍 Location-Based Complaint Tracking
- 🔄 Real-Time Notifications using WebSocket
- ☁️ Cloudinary Image Integration
- 🤖 AI-assisted Complaint Processing
- 📧 Email Support with Resend SMTP
- 📊 Analytics & Dashboard APIs
- 📄 Swagger API Documentation

---

## 🛠 Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA (Hibernate)
- MySQL
- JWT
- WebSocket (STOMP)
- Maven
- Swagger (SpringDoc OpenAPI)
- Cloudinary
- Resend SMTP

---

## 📂 Project Structure

```
src
 ├── config
 ├── controller
 ├── dto
 ├── entity
 ├── enums
 ├── exception
 ├── mapper
 ├── repository
 ├── security
 ├── service
 ├── websocket
 └── resources
```

---

## ⚙️ Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8+
- Git

---

## 🔧 Environment Variables

Create a `.env` file (or configure environment variables in your IDE):

```env
DB_URL=your_database_url
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

JWT_SECRET=your_jwt_secret

SMTP_HOST=smtp.resend.com
SMTP_PORT=587
SMTP_USERNAME=resend
RESEND_API_KEY=your_resend_api_key

FRONTEND_URL=http://localhost:5173
CORS_ORIGINS=http://localhost:5173

HF_TOKEN=your_huggingface_token
HF_ENABLED=false
```

---

## ▶️ Running Locally

Clone the repository

```bash
git clone https://github.com/yourusername/inframind-backend.git
```

Go to project directory

```bash
cd inframind-backend
```

Build the project

```bash
mvn clean install
```

Run the application

```bash
mvn spring-boot:run
```

Application will start at

```
http://localhost:8080
```

---

## 📚 API Documentation

Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

---

## 🚀 Deployment

The backend can be deployed on:

- Railway
- Render

Configure the required environment variables before deployment.

---

## 🔮 Future Enhancements

- Push Notifications
- Heatmap Analytics
- AI-based Duplicate Detection
- Performance Monitoring
- Complaint Recommendation System

---

## 👨‍💻 Author

**Akash Singh**

- GitHub: https://github.com/AkashSingh8391
- LinkedIn: https://www.linkedin.com/in/your-linkedin-profile

---

## 📄 License

This project is developed for learning and portfolio purposes.
