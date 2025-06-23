package factory

enum class DatabaseType {
    POSTGRES,
    MYSQL
}

/**
 * The Creator class declares the factory method that is supposed to return an object of a Product class.
 */
object DatabaseConnectionFactory {
    fun createConnection(type: DatabaseType): DatabaseConnection {
        return when (type) {
            DatabaseType.POSTGRES -> PostgresConnection()
            DatabaseType.MYSQL -> MySqlConnection()
        }
    }
} 