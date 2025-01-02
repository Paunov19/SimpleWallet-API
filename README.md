Simple Wallet API
Overview
The Simple Wallet API is a Java-based RESTful API designed to simulate a wallet management system. It allows users to create wallets, deposit and withdraw funds, transfer money between wallets or other users’ wallets. The API features JWT-based authentication to ensure that only authorized users can access protected endpoints. It also supports multiple currencies and integrates with a database to persist user and wallet data.
This project was built using Java 17 and the Spring Boot framework for the back-end API. It uses JWT for secure authentication, and supports database management through MySQL or PostgreSQL.
________________________________________
Technologies Used
•	Java 17
•	Spring Boot
•	Maven
•	MySQL
•	JWT (JSON Web Tokens)
•	Swagger UI web interface
________________________________________
Features
•	User Authentication: Secure login via JWT, ensuring that only authenticated users can perform operations.
•	Wallet Management: Users can create, view, and manage wallets, including deposits and withdrawals.
•	Currency Conversion: Supports currency conversion between wallets during transactions.
•	Transfer Funds: Allows transfers between wallets, both within the same user and between different users.
•	Transaction History: All wallet transactions are recorded and can be retrieved via an endpoint.
•	Preloaded Test Users: Test users are included for initial testing of the API.
________________________________________
Running the Application
After setting up the application, you can run the API with Maven or directly through your IDE:
•	Via IDE: Simply run the main class SimpleWalletApiApplication in your IDE (e.g., IntelliJ IDEA).
•	Via Maven: In the terminal, run the following commands:
mvn clean install
mvn spring-boot:run
________________________________________
Testing the API
After running the application, you can test the API endpoints using the Swagger UI interface.
1.	Open your browser and navigate to:
http://localhost:8080/swagger-ui/index.html
2.	Log in using one of the preloaded test users or create a new user.
3.	Use the JWT token to authenticate requests and start interacting with the API endpoints (e.g., create wallets, transfer funds, etc.).
________________________________________
Additional Notes
•	JWT Authentication: Once you log in and get the JWT token, you must use it in the Authorize section of the Swagger UI to interact with protected endpoints.
•	Data Persistence: All user and wallet data is stored in the database, and the system supports data persistence across restarts.
•	Security: The JWT token ensures that users can only access their own wallets and transactions.
