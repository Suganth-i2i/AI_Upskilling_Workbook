package observer

/**
 * The Subject interface declares a set of methods for managing subscribers.
 */
interface Subject {
    fun addObserver(observer: Observer)
    fun removeObserver(observer: Observer)
    fun notifyObservers(message: String)
} 