plugins {
    kotlin("jvm") version "2.0.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val nettyVersion: String by project
    val kodeinVersion: String by project

    implementation("io.netty:netty-all:$nettyVersion")
    implementation("org.kodein.di:kodein-di:$kodeinVersion")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}