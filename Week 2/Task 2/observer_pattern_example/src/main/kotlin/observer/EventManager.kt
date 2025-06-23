package observer

/**
 * The Concrete Subject owns some important state and notifies observers when the state changes.
 */
class EventManager : Subject {
    private val observers = mutableListOf<Observer>()

    override fun addObserver(observer: Observer) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer) {
        observers.remove(observer)
    }

    override fun notifyObservers(message: String) {
        // A copy is used to prevent issues if an observer is removed during notification
        observers.toList().forEach { it.update(message) }
    }

    fun newEvent(eventName: String) {
        println("EventManager: A new event occurred: $eventName")
        notifyObservers("New event: $eventName")
    }
} 