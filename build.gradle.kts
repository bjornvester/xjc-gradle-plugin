plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.13.0"
}

group = "com.github.bjornvester"
version = "1.4.1"

allprojects {
    repositories {
        jcenter()
    }
}

dependencies {
    compileOnly("org.glassfish.jaxb:jaxb-xjc:2.3.3")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
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
                    "- Carry the LANG environment over from the main environment to the XJC worker process to ensure the correct encoding is used"
        }
    }
}
