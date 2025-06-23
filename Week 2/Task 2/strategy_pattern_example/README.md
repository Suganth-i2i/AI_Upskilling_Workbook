# Strategy Pattern: Payment Processing

This project is an example of the Strategy design pattern implemented in Kotlin. It demonstrates how to encapsulate a family of algorithms (in this case, payment methods) and make them interchangeable.

## Core Components

- `PaymentStrategy.kt`: The interface that defines the common operation for all payment strategies.
- `CreditCardPayment.kt`, `PayPalPayment.kt`, `BankTransferPayment.kt`: Concrete implementations of the `PaymentStrategy` interface.
- `PaymentContext.kt`: The context class that uses a payment strategy.
- `Client.kt`: A sample client that shows how to use the `PaymentContext` with different strategies.
- `PaymentContextTest.kt`: Unit tests for the `PaymentContext`.

## How to Run

1. Open this project in your favorite IDE (e.g., IntelliJ IDEA).
2. Make sure you have Gradle configured.
3. Run the `main` function in `Client.kt` to see the output.
4. Run the tests in `PaymentContextTest.kt` using the Gradle test runner.

## Why the Strategy Pattern Fits

The Strategy pattern is ideal for this use case because:

- **It decouples the client from the implementation of the payment algorithms.** The client only needs to know about the `PaymentStrategy` interface.
- **It makes it easy to add new payment methods** without changing the client or the context.
- **It eliminates long `if/else` or `when` statements** for selecting a payment method.
- **It follows the Open/Closed Principle**, as the system is open for extension (new strategies) but closed for modification. 