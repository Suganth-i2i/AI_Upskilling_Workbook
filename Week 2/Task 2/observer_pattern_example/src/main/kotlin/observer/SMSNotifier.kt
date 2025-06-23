package observer

/**
 * Concrete Observer that sends an SMS notification.
 */
class SMSNotifier(private val phoneNumber: String) : Observer {
    override fun update(message: String) {
        println("SMSNotifier: Sending SMS to $phoneNumber with message: '$message'")
        // Add actual SMS sending logic here
    }
} 