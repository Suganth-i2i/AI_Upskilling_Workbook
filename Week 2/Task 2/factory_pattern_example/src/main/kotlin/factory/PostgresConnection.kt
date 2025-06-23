package factory

/**
 * Concrete Product for PostgreSQL connections.
 */
class PostgresConnection : DatabaseConnection {
    override fun connect() {
        println("Connecting to PostgreSQL database...")
    }

    override fun disconnect() {
        println("Disconnecting from PostgreSQL database.")
    }

    override fun query(sql: String): String {
        println("Executing PostgreSQL query: $sql")
        return "Result from PostgreSQL"
    }
} 