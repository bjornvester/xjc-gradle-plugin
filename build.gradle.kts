plugins {
    kotlin("jvm") version "1.3.41"
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "com.github.bjornvester"
version = "1.2"

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
    gradleVersion = "5.6"
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
            description = "Changes: \n" +
                    "- Support for generating episode files\n" +
                    "- Support for consuming episode and binding files\n" +
                    "- Work-around for the annoying SAXNotRecognizedExceptions in the output from XJC\n" +
                    "- The xsdFiles property now needs to be assigned (previously needed to be configured with the 'from' method)\n" +
                    "- The plugin now requires Gradle 5.6 or later (previously 5.4)"
        }
    }
}
