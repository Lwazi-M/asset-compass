# Asset-Compass 🧭

**Professional Portfolio Tracking Application**

AssetCompass is a full-stack financial tracking application designed to help users monitor their net worth across various asset classes (Stocks, Crypto, Real Estate, Cash). It features real-time price updates, secure passwordless authentication, and interactive data visualization.

🔗 **Live Demo:** [https://asset-compass-beta.vercel.app](https://asset-compass-beta.vercel.app)

---

## 🚀 Features

* **📊 Real-Time Dashboard:** Visualize your total net worth and asset distribution.
* **🔐 Secure Authentication:** Stateless JWT authentication with email-based verification codes (OTP).
* **📈 Live Market Data:** Integrated with **AlphaVantage API** for real-time stock and crypto pricing.
* **📜 Transaction History:** Automatically logs value changes and price refreshes for every asset.
* **📱 Responsive Design:** Built with **Tailwind CSS** for a seamless mobile and desktop experience.
* **☁️ Cloud Native:** Fully deployed on **Vercel** (Frontend), **Railway** (Backend), and **Neon** (Database).

---

## 🛠️ Tech Stack

### **Frontend**
* **Framework:** Next.js 15 (App Router)
* **Language:** TypeScript
* **Styling:** Tailwind CSS
* **State Management:** React Hooks
* **Visualization:** Recharts
* **HTTP Client:** Axios

### **Backend**
* **Framework:** Spring Boot 3
* **Language:** Java 21
* **Security:** Spring Security & JWT (Stateless)
* **Database:** PostgreSQL (Neon Tech)
* **ORM:** Spring Data JPA (Hibernate)
* **Build Tool:** Maven

---
## 🏗️ Architecture

The application follows a **Monorepo** structure:

```text
asset-compass/
├── frontend/          # Next.js Client Application
│   ├── app/           # App Router Pages & Components
│   └── public/        # Static Assets
│
├── backend/           # Spring Boot Server Application
│   ├── src/           # Java Source Code
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── repositories/
│   │   ├── security/
│   │   └── services/
│   └── pom.xml        # Maven Dependencies
```
---

## ⚙️ Getting Started

Follow these steps to run AssetCompass locally on your machine.

### **Prerequisites**
Ensure you have the following installed:
* **Node.js** (v18 or higher)
* **Java JDK** (v21)
* **Maven**
* **PostgreSQL** (Local or a Cloud URL)

### **1. Clone the Repository**
```bash
git clone [https://github.com/Lwazi-M/asset-compass.git](https://github.com/Lwazi-M/asset-compass.git)
cd asset-compass
```

### **2. Backend Setup**
Navigate to the backend directory and configure your environment.

```bash
cd backend
```

### Configuration:
Locate src/main/resources/application.properties and update your database credentials, or set them as environment variables:
```bash
spring.datasource.url=jdbc:postgresql://localhost:5432/assetcompass
spring.datasource.username=your_postgres_user
spring.datasource.password=your_postgres_password
app.alphavantage.key=YOUR_API_KEY
```

### Run the Spring Boot application:

```bash
mvn spring-boot:run
```
The backend will start on http://localhost:8080.

### **3. Frontend Setup**
Open a new terminal and navigate to the frontend directory.

```bash
cd frontend
```

### Install dependencies:

```bash
npm install
```

### Create a .env.local file:

```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### Run the development server:

```bash
npm run dev
```
The frontend will start on http://localhost:3000.

---

### **🌍 Environment Variables**
To deploy this app, you need to set the following environment variables in your cloud provider (Vercel/Railway).

| Variable | Description | Location |
| :--- | :--- | :--- |
| `NEXT_PUBLIC_API_URL` | URL of the backend API (e.g., `https://your-app.up.railway.app`) | **Frontend (Vercel)** |
| `SPRING_DATASOURCE_URL` | PostgreSQL Connection String | **Backend (Railway)** |
| `SPRING_DATASOURCE_USERNAME` | Database Username | **Backend (Railway)** |
| `SPRING_DATASOURCE_PASSWORD` | Database Password | **Backend (Railway)** |
| `APP_ALPHAVANTAGE_KEY` | AlphaVantage API Key for stock data | **Backend (Railway)** |
| `RESEND_API_KEY` | Resend API key Token | **Backend (Railway)** |

### **📄 License**
Distributed under the MIT License. See LICENSE for more information.

### **Contact**
Created by Lwazi M. - GitHub Profile
