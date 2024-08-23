plugins {
    id("java")
}

group = "asdf.diffjars"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

apply<MavenPublishPlugin>()

dependencies {
    implementation("org.ow2.asm:asm:9.5")
    implementation("org.ow2.asm:asm-tree:9.5")
    implementation("org.ow2.asm:asm-util:9.5")
    implementation("org.ow2.asm:asm-commons:9.5")
    implementation("org.apache.bcel:bcel:6.6.0")
    implementation("org.slf4j:slf4j-api:2.0.7")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks {
    java {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

configure<PublishingExtension> {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}
