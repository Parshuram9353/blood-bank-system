# 🩸 Blood Bank Management System

A full-stack web application designed to manage blood donations, donor registration, blood inventory tracking, and blood requests efficiently. Built with **Java 17**, **Spring Boot**, **MySQL**, **Docker**, and responsive frontend pages.

---

## ✨ Key Features

- 🩸 **Donor Management**: Register donors, record blood types, and track donation histories.
- 🏥 **Blood Stock & Inventory**: Real-time monitoring of available blood units categorized by blood group (A+, A-, B+, B-, AB+, AB-, O+, O-).
- 📋 **Request & Distribution**: Hospital and patient blood request tracking with approval workflows.
- 🐳 **Dockerized Setup**: Includes `Dockerfile` and `docker-compose.yml` for effortless containerized deployment.
- 🔐 **Secure Role-based Access**: Admin and User roles for managing records.

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot, Spring Data JPA
- **Database**: MySQL Server 8.x
- **Frontend**: HTML5, CSS3, JavaScript
- **DevOps**: Docker, Docker Compose, Apache Maven

---

## 🚀 Quick Start

### 1. Run with Docker Compose (Recommended)
```bash
docker-compose up --build
```

### 2. Manual Setup (Local)
1. Initialize MySQL database using `schema.sql` and `data.sql` found in resources.
2. Configure credentials in `src/main/resources/application.properties`.
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Access the web interface at `http://localhost:8080`.

---

## 👤 Author

- **GitHub**: [@Parshuram9353](https://github.com/Parshuram9353)
- **Email**: [parshuramrathod8861743828@gmail.com](mailto:parshuramrathod8861743828@gmail.com)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
