# Factory Pattern: Database Connection Factory

This project provides a Kotlin implementation of the Factory design pattern. It demonstrates how to create objects without exposing the instantiation logic to the client, referring to the newly created object through a common interface.

## Core Components

- `DatabaseConnection.kt`: The interface (product) that all concrete database connection classes implement.
- `PostgresConnection.kt` and `MySqlConnection.kt`: Concrete implementations (products) of the `DatabaseConnection` interface.
- `DatabaseConnectionFactory.kt`: The factory class that contains the method for creating `DatabaseConnection` objects.
- `DatabaseType.kt`: An enum to specify the type of database connection to create.
- `Client.kt`: A sample client that demonstrates how to use the factory to get database connection objects.
- `DatabaseConnectionFactoryTest.kt`: Unit tests for the factory.

## How to Run

1. Open this project in a Kotlin-compatible IDE like IntelliJ IDEA.
2. Make sure Gradle is set up correctly.
3. Run the `main` function in `Client.kt` to see how the factory creates different database connections.
4. Run the tests in `DatabaseConnectionFactoryTest.kt` to verify the factory's behavior.

## Why the Factory Pattern Fits

The Factory pattern is particularly well-suited for this scenario because:

- **It decouples the client from concrete classes.** The client code works with the `DatabaseConnection` interface and doesn't need to know the specific class names of the database connectors.
- **It centralizes object creation.** All the logic for creating database connections is in one place, making the code easier to maintain and understand.
- **It enhances extensibility.** Adding a new database type (e.g., `OracleConnection`) only requires creating the new class and adding a case to the factory method. The client code remains unchanged.
- **It adheres to the Open/Closed Principle**, as the system is open for extension with new connection types but closed for modification of the client code. 