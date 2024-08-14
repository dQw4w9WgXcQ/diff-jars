plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "asdf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.bcel:bcel:6.6.0")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.slf4j:slf4j-simple:2.0.7")
}

tasks {
    build {
        finalizedBy("shadowJar")
    }

    jar {
        manifest {
            attributes["Main-Class"] = "asdf.Main"
        }
    }
}
