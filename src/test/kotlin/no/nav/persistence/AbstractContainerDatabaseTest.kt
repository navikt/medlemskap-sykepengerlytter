package no.nav.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.containers.JdbcDatabaseContainer
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import javax.sql.DataSource


abstract class AbstractContainerDatabaseTest {
    @Throws(SQLException::class)
    protected open fun performQuery(container: JdbcDatabaseContainer<*>, sql: String?): ResultSet? {
        val ds: DataSource = getDataSource(container)
        val statement: Statement = ds.getConnection().createStatement()
        statement.execute(sql)
        val resultSet: ResultSet = statement.getResultSet()
        resultSet.next()
        return resultSet
    }

    protected open fun getDataSource(container: JdbcDatabaseContainer<*>): DataSource {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = container.jdbcUrl
        hikariConfig.username = container.username
        hikariConfig.password = container.password
        hikariConfig.driverClassName = container.driverClassName
        return HikariDataSource(hikariConfig)
    }
}