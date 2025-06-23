package observer

fun main() {
    val eventManager = EventManager()

    // Create observers
    val emailNotifier = EmailNotifier("user@example.com")
    val smsNotifier = SMSNotifier("123-456-7890")

    // Subscribe observers to the event manager
    println("Client: Subscribing email and SMS notifiers.")
    eventManager.addObserver(emailNotifier)
    eventManager.addObserver(smsNotifier)

    println()

    // A new event occurs, and all subscribers are notified
    eventManager.newEvent("User logged in")

    println()

    // Unsubscribe an observer
    println("Client: Unsubscribing SMS notifier.")
    eventManager.removeObserver(smsNotifier)

    println()

    // Another event occurs, only the remaining subscribers are notified
    eventManager.newEvent("User updated profile")
} 