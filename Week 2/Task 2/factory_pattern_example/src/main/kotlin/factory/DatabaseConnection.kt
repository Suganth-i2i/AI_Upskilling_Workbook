package factory

/**
 * The Product interface declares the operations that all concrete products must implement.
 */
interface DatabaseConnection {
    fun connect()
    fun disconnect()
    fun query(sql: String): String
} 