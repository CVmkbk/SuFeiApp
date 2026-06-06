// server/build.gradle.kts
plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("io.ktor.plugin") version "3.0.3"
    id("org.flywaydb.flyway") version "10.22.0"
}

group = "dev.wceng.sufei"
version = "1.0.0"

application {
    mainClass.set("dev.wceng.sufei.server.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")

    // Kotlin Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // MySQL + HikariCP
    implementation("com.mysql:mysql-connector-j:8.4.0")
    implementation("com.zaxxer:HikariCP:6.2.1")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.57.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.57.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.57.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.57.0")

    // Flyway (数据库迁移)
    implementation("org.flywaydb:flyway-core:10.22.0")
    implementation("org.flywaydb:flyway-mysql:10.22.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // BCrypt (密码哈希)
    implementation("at.favre.lib:bcrypt:0.10.2")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}