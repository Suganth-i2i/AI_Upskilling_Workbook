# Observer Pattern: Event Notification System

This project is a demonstration of the Observer design pattern implemented in Kotlin. It models a simple event notification system where multiple listeners (observers) can subscribe to an event source (the subject) and get notified when an event occurs.

## Core Components

- `Observer.kt`: The interface that all observers must implement. It has a single `update` method.
- `Subject.kt`: The interface that the event source must implement. It defines methods for adding, removing, and notifying observers.
- `EventManager.kt`: A concrete implementation of the `Subject` interface. It maintains a list of observers and notifies them of new events.
- `EmailNotifier.kt` and `SMSNotifier.kt`: Concrete implementations of the `Observer` interface.
- `Client.kt`: A sample client that shows how to use the `EventManager` and subscribe observers to it.
- `EventManagerTest.kt`: Unit tests for the `EventManager` to ensure it functions correctly.

## How to Run

1. Open this project in your favorite IDE (e.g., IntelliJ IDEA).
2. Ensure you have Gradle configured.
3. Run the `main` function in `Client.kt` to see the output of the event notifications.
4. Run the tests in `EventManagerTest.kt` to verify the subject's behavior.

## Why the Observer Pattern Fits

The Observer pattern is an excellent choice for this use case for several reasons:

- **Loose Coupling**: The subject (`EventManager`) knows nothing about the concrete observers, other than that they implement the `Observer` interface. This means you can add new observers without changing the subject's code.
- **Dynamic Relationships**: Observers can be added or removed at any time during the application's lifecycle.
- **Broadcast Communication**: A single event can be broadcast to multiple observers without the subject needing to know how many observers there are or what they do.
- **Promotes the Open/Closed Principle**: The system is open to extension (you can create new observer types) but closed for modification (you don't need to change the subject to support them). 