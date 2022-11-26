import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.AppendingTransformer

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "ru.yakovlevdmv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val scalaBinaryVersion: String by project
    val akkaVersion: String by project
    val akkaHttpVersion: String by project
    val typesafeConfigVersion: String by project
    val config4kVersion: String by project

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
//    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.slf4j:slf4j-simple:2.0.5")

    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("io.github.config4k:config4k:$config4kVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation(platform("com.typesafe.akka:akka-bom_$scalaBinaryVersion:$akkaVersion"))
    implementation(platform("com.typesafe.akka:akka-http-bom_$scalaBinaryVersion:$akkaHttpVersion"))

    implementation("com.typesafe.akka:akka-actor-typed_$scalaBinaryVersion")
    implementation("com.typesafe.akka:akka-stream_$scalaBinaryVersion")
    implementation("com.typesafe.akka:akka-http_$scalaBinaryVersion")
    implementation("com.typesafe.akka:akka-http-jackson_$scalaBinaryVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

// https://doc.akka.io/docs/akka/current/additional/packaging.html#gradle-the-jar-task-from-the-java-plugin
tasks.withType<ShadowJar> {
    val newTransformer = AppendingTransformer()
    newTransformer.resource = "reference.conf"
    transformers.add(newTransformer)

    manifest {
        attributes(
            "Main-Class" to "ru.yakovlevdmv.nordvpn.scanner.MainKt"
        )
    }
}
