import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.14.0"
}

group = "com.github.bjornvester"
version = "1.6.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    compileOnly("org.glassfish.jaxb:jaxb-xjc:2.3.3")
    testImplementation("commons-io:commons-io:2.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("GRADLE_ROOT_FOLDER", projectDir.absolutePath)
    systemProperty("GRADLE_PLUGIN_VERSION", version)
}

tasks.withType<Wrapper> {
    gradleVersion = "7.3.3"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

gradlePlugin {
    plugins {
        create("xjcPlugin") {
            id = "com.github.bjornvester.xjc"
            implementationClass = "com.github.bjornvester.xjc.XjcPlugin"
            displayName = "Gradle XJC plugin"
            description = """A plugin that generates Java source code for XML schemas (xsd files) using the XJC tool.
                            |Please see the Github project page for details.""".trimMargin()
        }
    }
}

pluginBundle {
    website = "https://github.com/bjornvester/xjc-gradle-plugin"
    vcsUrl = "https://github.com/bjornvester/xjc-gradle-plugin"
    tags = listOf("xjc", "jaxb", "xsd")
    (plugins) {
        "xjcPlugin" {
            description = """Changes:
                            |- Support grouping XSDs and generate them with different configurations""".trimMargin()
        }
    }
}
