plugins {
    kotlin("jvm") version "1.3.72"
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.12.0"
}

version = "1.5"

allprojects {
    repositories {
        jcenter()
    }
}

dependencies {
    compileOnly("org.glassfish.jaxb:jaxb-xjc:2.3.3")
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "6.6.1"
    }

    test {
        useJUnitPlatform()
    }
}

gradlePlugin {
    plugins {
        create("xjcPlugin") {
            id = "com.github.kad-leeuwg1.xjc"
            implementationClass = "com.github.bjornvester.xjc.XjcPlugin"
            displayName = "Gradle XJC plugin"
            description = "A plugin that generates Java source code for XML schemas (xsd files) using the XJC tool. Supports the Gradle build cache and has been tested with Java 8 and 11. Please see the Github project page for details."
        }
    }
}

pluginBundle {
    website = "https://github.com/kad-leeuwg1/xjc-gradle-plugin"
    vcsUrl = "https://github.com/kad-leeuwg1/xjc-gradle-plugin"
    tags = listOf("xjc", "jaxb", "xsd")
    (plugins) {
        "xjcPlugin" {
            description = "Changes:\n" +
                    "- Move configuration to XJC task instead of the extension" +
                    "- Allow multiple XJC tasks to be configured"
        }
    }
}
