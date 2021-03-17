plugins {
    kotlin("jvm") version "1.3.50"
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "com.github.bjornvester"
version = "1.4"

allprojects {
    repositories {
        jcenter()
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("org.glassfish.jaxb:jaxb-xjc:2.3.2")
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.8.3"
}

gradlePlugin {
    plugins {
        create("xjcPlugin") {
            id = "com.github.bjornvester.xjc"
            implementationClass = "com.github.bjornvester.xjc.XjcPlugin"
            displayName = "Gradle XJC plugin"
            description = "A plugin that generates Java source code for XML schemas (xsd files) using the XJC tool. Supports the Gradle build cache and has been tested with Java 8 and 11. Please see the Github project page for details."
        }
    }
}

pluginBundle {
    website = "https://github.com/bjornvester/xjc-gradle-plugin"
    vcsUrl = "https://github.com/bjornvester/xjc-gradle-plugin"
    tags = listOf("xjc", "jaxb", "xsd")
    (plugins) {
        "xjcPlugin" {
            description = "Changes:\n" +
                    "- Upgrade default version of XJC from version 2.3.2 to 2.3.3"
        }
    }
}
