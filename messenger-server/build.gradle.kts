plugins {
    kotlin("jvm") version "2.1.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val nettyVersion: String by project
    val kodeinVersion: String by project
    val log4jVersion: String by project
    val slf4jVersion: String by project

    // Netty for WebSocket server
    implementation("io.netty:netty-all:$nettyVersion")
    
    // Dependency injection
    implementation("org.kodein.di:kodein-di:$kodeinVersion")
    
    // Logging - SLF4J API (facade)
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    
    // Log4j2 implementation (secure version without vulnerabilities)
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    
    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}