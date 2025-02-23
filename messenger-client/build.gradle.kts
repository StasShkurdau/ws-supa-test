plugins {
    kotlin("jvm")
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
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}