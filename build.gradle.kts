plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "org.itmo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.rabbitmq:amqp-client:5.27.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}

tasks.register("printClasspath") {
    doLast {
        println(sourceSets["main"].runtimeClasspath.asPath)
    }
}

tasks.register("writeClasspath") {
    doLast {
        val cp = sourceSets["main"].runtimeClasspath
            .joinToString(System.getProperty("path.separator")) { it.absolutePath }
        file("classpath.txt").writeText(cp)
        println("Записан classpath.txt")
    }
}
