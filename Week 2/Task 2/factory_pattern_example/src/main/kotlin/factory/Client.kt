package factory

fun main() {
    println("Client: Requesting a PostgreSQL connection.")
    val postgresConnection = DatabaseConnectionFactory.createConnection(DatabaseType.POSTGRES)
    postgresConnection.connect()
    val postgresResult = postgresConnection.query("SELECT * FROM users")
    println("Client: Received result: $postgresResult")
    postgresConnection.disconnect()

    println()

    println("Client: Requesting a MySQL connection.")
    val mysqlConnection = DatabaseConnectionFactory.createConnection(DatabaseType.MYSQL)
    mysqlConnection.connect()
    val mysqlResult = mysqlConnection.query("SELECT * FROM products")
    println("Client: Received result: $mysqlResult")
    mysqlConnection.disconnect()
} 