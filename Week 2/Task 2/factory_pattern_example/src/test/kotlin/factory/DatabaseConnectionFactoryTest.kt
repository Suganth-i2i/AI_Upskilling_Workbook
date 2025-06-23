package factory

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DatabaseConnectionFactoryTest {

    @Test
    fun `should create a PostgresConnection`() {
        // When
        val connection = DatabaseConnectionFactory.createConnection(DatabaseType.POSTGRES)

        // Then
        assertTrue(connection is PostgresConnection, "The factory should return an instance of PostgresConnection")
    }

    @Test
    fun `should create a MySqlConnection`() {
        // When
        val connection = DatabaseConnectionFactory.createConnection(DatabaseType.MYSQL)

        // Then
        assertTrue(connection is MySqlConnection, "The factory should return an instance of MySqlConnection")
    }
} 