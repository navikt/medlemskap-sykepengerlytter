val ktorVersion = "2.1.2"
val jacksonVersion = "2.10.5"
val konfigVersion = "1.6.10.0"
val kotlinLoggerVersion = "1.8.3"
val resilience4jVersion = "1.5.0"
val logstashVersion = "7.2"
val logbackVersion = "1.2.9"
val flywayVersion = "6.5.0"
val hikariVersion = "3.4.5"
val kotliqueryVersion = "1.3.1"
val httpClientVersion = "4.5.13"
val mainClass = "no.nav.medlemskap.sykepenger.lytter.ApplicationKt"

plugins {
    kotlin("jvm") version "1.7.10"
    application
    id("com.github.davidmc24.gradle.plugin.avro") version "1.2.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "no.nav.medlemskap"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
    maven("https://repo.adeo.no/repository/maven-releases")
    maven("https://repo.adeo.no/repository/nexus2-m2internal")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.github.resilience4j:resilience4j-retry:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion") {
        exclude(group = "io.netty", module = "netty-codec")
        exclude(group = "io.netty", module = "netty-codec-http")
    }
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("org.apache.httpcomponents:httpclient:$httpClientVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("org.apache.httpcomponents:httpclient:$httpClientVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.0")
    implementation("com.natpryce:konfig:$konfigVersion")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggerVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:2.1.2")
    // 2.8.0 er tilgjengelig, burde kanskje oppdatere
    implementation("org.apache.kafka:kafka-clients:2.5.0")
    implementation("org.apache.avro:avro:1.10.2")
    implementation("io.confluent:kafka-avro-serializer:5.2.2")
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("org.testcontainers:kafka:1.16.0")
    testImplementation ("org.testcontainers:postgresql:1.16.0")
    testImplementation ("org.testcontainers:junit-jupiter:1.16.0")
    //Database
    implementation("org.postgresql:postgresql:42.2.5")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "15"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    shadowJar {
        archiveBaseName.set("app")
        archiveClassifier.set("")
        archiveVersion.set("")
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to mainClass
                )
            )
        }
    }

    test {
        useJUnitPlatform()
        //Trengs inntil videre for bytebuddy med java 16, som brukes av mockk.
        jvmArgs = listOf("-Dnet.bytebuddy.experimental=true")
    }
}

application {
    mainClass.set("no.nav.medlemskap.sykepenger.lytter.ApplicationKt")
}
