plugins {
    kotlin("jvm") version "1.3.41"
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "com.github.bjornvester"
version = "1.1"

allprojects {
    repositories {
        jcenter()
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("org.glassfish.jaxb:jaxb-xjc:2.3.2")
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "5.5.1"
}

gradlePlugin {
    plugins {
        create("xjcPlugin") {
            id = "com.github.bjornvester.xjc"
            implementationClass = "com.github.bjornvester.xjc.XjcPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/bjornvester/xjc-gradle-plugin"
    vcsUrl = "https://github.com/bjornvester/xjc-gradle-plugin"
    tags = listOf("xjc", "jaxb", "xsd")
    description = "Adds the XJC tool to your project for generating Java source code for XML schemas (xsd files). Supports the Gradle build cache and has been tested with Java 8 and 11. Please see the Github project page for details."
    (plugins) {
        "xjcPlugin" {
            displayName = "Gradle XJC plugin"
            description = "Changes: Added a property to specify the default package for the generated Java classes"
        }
    }
}
