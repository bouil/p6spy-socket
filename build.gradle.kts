plugins {
    java
    id("maven-publish")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}
dependencies {
    implementation(group = "io.reactivex.rxjava3", name = "rxjava", version = "3.0.6")
    implementation(group = "p6spy", name = "p6spy", version = "3.9.1")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.9.10.4")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.30")
}

group = "net.bouillon"
version = "1.0.0-SNAPSHOT"
description = "net.bouillon p6spy-socket"
//sourceCompatibility = "1.8"

tasks.withType(JavaCompile::class) {
    sourceCompatibility = "11"
    targetCompatibility = "11"
    options.encoding = "UTF-8"
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
