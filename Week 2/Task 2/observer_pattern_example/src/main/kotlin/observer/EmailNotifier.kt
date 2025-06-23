package observer

/**
 * Concrete Observer that sends an email notification.
 */
class EmailNotifier(private val email: String) : Observer {
    override fun update(message: String) {
        println("EmailNotifier: Sending email to $email with message: '$message'")
        // Add actual email sending logic here
    }
} 