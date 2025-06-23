package observer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EventManagerTest {

    // A mock observer for testing purposes
    class MockObserver : Observer {
        var notified = false
        var message: String? = null

        override fun update(message: String) {
            notified = true
            this.message = message
        }
    }

    @Test
    fun `should notify all subscribed observers`() {
        // Given
        val eventManager = EventManager()
        val observer1 = MockObserver()
        val observer2 = MockObserver()
        val message = "Test event"

        eventManager.addObserver(observer1)
        eventManager.addObserver(observer2)

        // When
        eventManager.notifyObservers(message)

        // Then
        assertTrue(observer1.notified)
        assertEquals(message, observer1.message)
        assertTrue(observer2.notified)
        assertEquals(message, observer2.message)
    }

    @Test
    fun `should not notify an unsubscribed observer`() {
        // Given
        val eventManager = EventManager()
        val observer1 = MockObserver()
        val observer2 = MockObserver()
        val message = "Another test event"

        eventManager.addObserver(observer1)
        eventManager.addObserver(observer2)

        // When
        eventManager.removeObserver(observer2)
        eventManager.notifyObservers(message)

        // Then
        assertTrue(observer1.notified)
        assertEquals(message, observer1.message)
        assertTrue(!observer2.notified, "Unsubscribed observer should not be notified")
    }
} 