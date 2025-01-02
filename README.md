# **Simple Wallet API**

## **Overview**

The **Simple Wallet API** is a Java-based RESTful API designed to simulate a wallet management system. It allows users to:

- Create wallets.
- Deposit and withdraw funds.
- Transfer money between wallets or other users’ wallets.


This project is built using Java 17 and the Spring Boot framework for the back-end API. It uses JWT for secure authentication and supports database management through MySQL or PostgreSQL.

The API features JWT-based authentication to ensure that only authorized users can access protected endpoints. It also supports multiple currencies and integrates with a database to persist user and wallet data.

---

## **Technologies Used**

- **Java 17**
- **Spring Boot**
- **Maven**
- **MySQL**
- **JWT (JSON Web Tokens)**
- **Swagger UI web interface**

---

## **Features**

- **User Authentication**: Secure login via JWT, ensuring that only authenticated users can perform operations.
- **Wallet Management**: Users can create, view, and manage wallets, including deposits and withdrawals.
- **Currency Conversion**: Supports currency conversion between wallets during transactions.
- **Transfer Funds**: Allows transfers between wallets, both within the same user and between different users.
- **Transaction History**: All wallet transactions are recorded and can be retrieved via an endpoint.
- **Preloaded Test Users**: Test users are included for initial testing of the API.

---

## **Running the Application**

After setting up the application, you can run the API with Maven or directly through your IDE:

- **Via IDE**: Рun the main class `SimpleWalletApiApplication` in your IDE (e.g., IntelliJ IDEA).
- **Via Maven**: In the terminal, run the following commands:

```bash
mvn clean install
mvn spring-boot:run
