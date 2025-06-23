package observer

/**
 * The Observer interface declares the update method, used by subjects.
 */
interface Observer {
    fun update(message: String)
} 