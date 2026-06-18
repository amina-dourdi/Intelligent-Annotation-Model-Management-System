<div align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white" alt="Tailwind CSS">
  <img src="https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white" alt="Python">
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">

  <h1>Intelligent Annotation & Model Management System</h1>
  <p>A secure Spring Boot platform for large-scale NLP data annotation, model training, and workforce management.</p>
</div>

---

## 📖 Overview

The **Intelligent Annotation & Model Management System** is a full-stack platform designed to streamline the Natural Language Processing (NLP) data annotation lifecycle. It bridges the gap between raw data and machine learning by providing a robust environment where administrators can manage datasets, distribute workloads to annotators, and directly train Python-based Machine Learning models using the annotated data.

## ✨ Key Features

### 🛡️ Role-Based Access Control (RBAC)
- **Administrators**: Full control over datasets, users, metrics, and ML training pipelines.
- **Annotators**: Focused workspace for annotating assigned text pairs with deadline tracking.

### 📊 Advanced Dataset Management
- Import datasets via CSV with automated text pair extraction.
- Define dynamic NLP classes (e.g., *Entails, Neutral, Contradiction* for NLI).
- Export fully annotated datasets for external use.

### 🧠 Intelligent Workload Distribution
- **Automated Assignment**: Randomly distributes text pairs to ensure every pair is annotated by exactly 3 different annotators.
- **Rebalancing**: Automatically redistributes tasks when an annotator is removed or deactivated.

### 📈 Quality Control & Metrics
- **Inter-Annotator Agreement**: Automatically calculates **Fleiss' Kappa** to measure the reliability of annotations.
- **Spammer Detection**: Identifies and flags annotators who consistently disagree with the majority (low Kappa score).

### 🤖 Integrated Machine Learning Pipeline
- **Python Integration**: Seamlessly triggers Python scripts (Scikit-Learn) directly from the Spring Boot backend.
- **Model Training**: Train models (e.g., Logistic Regression with TF-IDF) on the newly annotated data.
- **Metrics Dashboard**: View Accuracy, F1-Score, and Confusion Matrices directly in the web interface.

### ⏰ Automated Notifications
- **Cron Jobs**: Background scheduler runs periodically to check deadlines.
- **Email Reminders**: Automatically sends email alerts to annotators 24 hours before their annotation deadline.

## 🛠️ Technology Stack

| Layer | Technology |
| --- | --- |
| **Backend** | Java 23, Spring Boot, Spring Security, Spring Data JPA |
| **Frontend** | Thymeleaf, Tailwind CSS, FontAwesome |
| **Database** | MySQL |
| **Machine Learning** | Python, Pandas, Scikit-Learn |
| **Mail Server** | JavaMailSender (SMTP) |

## 🚀 Getting Started

### Prerequisites
- **Java 23** or higher
- **Maven**
- **MySQL** Server
- **Python 3.8+** (with `pandas` and `scikit-learn` installed)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/nlp-annotation-platform.git
   cd nlp-annotation-platform
   ```

2. **Configure the Database & Email**
   Open `src/main/resources/application.properties` and update:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/nlp_annotation_db
   spring.datasource.username=root
   spring.datasource.password=yourpassword

   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   ```

3. **Install Python Dependencies**
   Ensure the `train.py` script has its required libraries:
   ```bash
   pip install pandas scikit-learn
   ```

4. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the Platform**
   Open your browser and navigate to `http://localhost:8080`.
   - **Default Admin Login**: `admin` / `admin` (Configurable in `DataInitializer.java`)

## 🎨 UI Showcase

The platform features a modern, responsive, and intuitive interface built with Tailwind CSS, ensuring a premium user experience for both administrators and annotators. Features include:
- Interactive Dashboards
- Password Visibility Toggles
- Dynamic Progress Bars
- Real-time Status Badges

---
*Developed with ❤️ for NLP Data Scientists and Annotators.*
