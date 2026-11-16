# PaisaTrack – Personal Finance Tracker

A mobile-first Personal Finance Tracker (PWA) built with **Java 17**, **Spring Boot 3**, **Spring MVC**, **Spring Data JPA**, **Spring Security**, **Thymeleaf**, **Chart.js**, and **MySQL 8.x Community Edition**.

---

## 1. Prerequisites (Windows Local Setup)

Install the following tools on Windows:

1. **Java JDK 17 or later** (e.g., Eclipse Temurin JDK 17 LTS).
   Verify in Windows PowerShell:
   ```powershell
   java -version
   ```
2. **Apache Maven 3.9+** (added to Windows `PATH`).
   Verify in Windows PowerShell:
   ```powershell
   mvn -version
   ```
3. **MySQL 8.0.16+ Community Edition** and **MySQL Workbench**.
4. **Visual Studio Code** with **Extension Pack for Java** (`vscjava.vscode-java-pack`) and **Spring Boot Extension Pack** (`vmware.vscode-boot-dev-pack`, optional).

---

## 2. Database Setup in MySQL Workbench

1. Ensure the **MySQL80** Windows service is running (check `services.msc`).
2. Open **MySQL Workbench** and connect to your local MySQL 8.x instance.
3. Open and execute the three SQL scripts inside `paisatrack/sql/` in exact order:
   - `sql/01_create_database.sql` — creates `paisatrack_db` with `utf8mb4`.
   - `sql/02_create_tables.sql` — creates `users`, `categories`, and `transactions` with PKs, FKs, `CHECK` constraints, `DECIMAL(12,2)`, and indexes.
   - `sql/03_seed_categories.sql` — inserts the 8 expense categories and 5 income categories.
4. Run this verification query in MySQL Workbench:
   ```sql
   USE paisatrack_db;
   SHOW TABLES;
   SELECT category_type, COUNT(*) AS total FROM categories GROUP BY category_type;
   ```
   Expected result: `EXPENSE = 8`, `INCOME = 5`.

---

## 3. Configure Environment Variables Manually

> **Important about `.env.example`:**  
> `paisatrack/.env.example` is a **reference template only**. Spring Boot does **not** automatically load `.env` files. You must set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` manually in your terminal or VS Code launch configuration before running or testing the application.

### Option A: Windows PowerShell (Recommended)

Open a PowerShell terminal in the `paisatrack/` folder and set the variables for your current session:

```powershell
cd path\to\paisatrack
$env:DB_URL = "jdbc:mysql://localhost:3306/paisatrack_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&characterEncoding=UTF-8"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_MYSQL_PASSWORD"
```

### Option B: Visual Studio Code Setup

1. In VS Code, click **File → Open Folder...** and select the **`paisatrack`** folder directly (so `pom.xml` is at the workspace root in VS Code).
2. Wait for the **Extension Pack for Java** to import the Maven project.
3. Open the integrated PowerShell terminal (**Terminal → New Terminal**) and set the environment variables from **Option A** above before running `mvn test` or `mvn spring-boot:run`.
4. *(Optional)* If you run `PaisaTrackApplication.java` via VS Code's **Run and Debug** panel instead of PowerShell, create `.vscode/launch.json` with an `"env"` block:
   ```json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "Run PaisaTrackApplication",
         "request": "launch",
         "mainClass": "com.paisatrack.PaisaTrackApplication",
         "env": {
           "DB_URL": "jdbc:mysql://localhost:3306/paisatrack_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&characterEncoding=UTF-8",
           "DB_USERNAME": "root",
           "DB_PASSWORD": "YOUR_MYSQL_PASSWORD"
         }
       }
     ]
   }
   ```

---

## 4. Build, Test, and Run Commands (Windows PowerShell)

Make sure MySQL is running, the SQL scripts have been executed, and `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` are set in your PowerShell session:

### Compile
```powershell
mvn clean compile
```

### Run Unit & Real-MySQL Integration Tests
Integration tests connect directly to MySQL `paisatrack_db` (never an in-memory database) and clean up test records automatically:
```powershell
mvn test
```

### Run the Application
```powershell
mvn spring-boot:run
```
Or package and run the executable JAR:
```powershell
mvn clean package
java -jar target/paisatrack-1.0.0.jar
```

Open in your browser:
- **Desktop:** `http://localhost:8080`
- **Android Browser (same Wi-Fi):** `http://<YOUR_WINDOWS_IP>:8080`

---

## 5. Architecture & Technical Notes

- **Entry Point:** `src/main/java/com/paisatrack/PaisaTrackApplication.java` (sets JVM default time zone to `Asia/Kolkata`).
- **OOP Principles:**
  - **Encapsulation:** Private entity/DTO fields with Jakarta Bean Validation and getters/setters.
  - **Abstraction:** `FinancialEntry` interface, abstract `Transaction` entity, and Service/Repository interfaces.
  - **Inheritance:** `IncomeTransaction` and `ExpenseTransaction` extend `Transaction` (`@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` on `transactions.transaction_type`).
  - **Polymorphism:** Subclasses override `getType()`, `getSignedAmount()`, `getFormattedPrefix()`, and `getAmountCssClass()`.
- **Database Persistence:** MySQL 8.x is the sole persistent store. No JSON, CSV, `localStorage`, `sessionStorage`, `IndexedDB`, or Firebase is used for financial data.
- **PWA Behavior:** `manifest.webmanifest` and `sw.js` cache static assets (`/css/`, `/js/`, `/icons/`, `offline.html`) for installability only. Financial pages and `/api/*` data always require an active network connection to MySQL (offline transaction synchronization is not supported).

---

## 6. Troubleshooting Guide

1. **`Access denied for user 'root'@'localhost'`**
   - Verify `$env:DB_PASSWORD` in your active PowerShell session matches your MySQL password (`$env:DB_PASSWORD`).
2. **`Unknown database 'paisatrack_db'`**
   - Execute `sql/01_create_database.sql`, `sql/02_create_tables.sql`, and `sql/03_seed_categories.sql` in MySQL Workbench first.
3. **Empty category dropdown on Add Transaction page**
   - Confirm `sql/03_seed_categories.sql` ran successfully and `categories` contains 13 rows.
4. **`Public Key Retrieval is not allowed`**
   - Ensure `allowPublicKeyRetrieval=true` is included in `DB_URL`.
5. **Port `8080` already in use**
   - Set `$env:SERVER_PORT = "8081"` before running `mvn spring-boot:run`.
6. **`mvn` is not recognized in PowerShell**
   - Add Maven's `bin` directory (e.g., `C:\Program Files\Apache\maven\bin`) to Windows System `PATH` and restart VS Code.
7. **Old CSS or JS served after updates**
   - In Browser DevTools → **Application → Service Workers → Unregister**, then hard-refresh (`Ctrl + Shift + R`).
