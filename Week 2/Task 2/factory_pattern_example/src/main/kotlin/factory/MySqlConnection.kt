package factory

/**
 * Concrete Product for MySQL connections.
 */
class MySqlConnection : DatabaseConnection {
    override fun connect() {
        println("Connecting to MySQL database...")
    }

    override fun disconnect() {
        println("Disconnecting from MySQL database.")
    }

    override fun query(sql: String): String {
        println("Executing MySQL query: $sql")
        return "Result from MySQL"
    }
} 