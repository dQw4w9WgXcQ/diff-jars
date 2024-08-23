plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "asdf.diffjars"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(rootProject)
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    build {
        finalizedBy("shadowJar")
    }

    jar {
        manifest {
            attributes["Main-Class"] = "asdf.diffjars.Main"
        }
    }
}
