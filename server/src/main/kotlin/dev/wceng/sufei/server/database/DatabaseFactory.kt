package dev.wceng.sufei.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    private lateinit var config: HikariConfig
    private var _dataSource: HikariDataSource? = null

    fun init(appConfig: ApplicationConfig) {
        config = HikariConfig().apply {
            jdbcUrl = appConfig.property("database.url").getString()
            driverClassName = appConfig.property("database.driver").getString()
            username = appConfig.property("database.user").getString()
            password = appConfig.property("database.password").getString()
            maximumPoolSize = appConfig.property("database.maxPoolSize").getString().toInt()
            isAutoCommit = true
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        _dataSource = HikariDataSource(config)

        Database.connect(_dataSource!!)

        Flyway.configure()
            .dataSource(_dataSource!!)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }

    fun getDataSource(): HikariDataSource {
        return _dataSource ?: error("DatabaseFactory 尚未初始化")
    }

    fun shutdown() {
        _dataSource?.close()
    }
}