plugins {
    `kotlin-dsl`
    `jvm-test-suite`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = "com.github.bjornvester"
version = "1.9.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

dependencies {
    compileOnly("org.glassfish.jaxb:jaxb-xjc:2.3.9")
    testImplementation("commons-io:commons-io:2.21.0")
    testImplementation(platform("org.junit:junit-bom:5.14.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()

            targets {
                all {
                    testTask.configure {
                        systemProperty("GRADLE_ROOT_FOLDER", projectDir.absolutePath)
                        systemProperty("GRADLE_PLUGIN_VERSION", version)
                    }
                }
            }
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "latest"
}

gradlePlugin {
    website.set("https://github.com/bjornvester/xjc-gradle-plugin")
    vcsUrl.set("https://github.com/bjornvester/xjc-gradle-plugin")
    plugins {
        create("xjcPlugin") {
            id = "com.github.bjornvester.xjc"
            implementationClass = "com.github.bjornvester.xjc.XjcPlugin"
            displayName = "Gradle XJC plugin"
            tags.set(listOf("xjc", "jaxb", "xsd"))
            description = """Changes:
                |- New minimum version of Gradle is now 7.3 (previously 6.7)
                |- XJC now defaults to JAXB 4.0 (previously 3.0)
                |- Fixed an issue with parsing custom options containing multiple arguments.""".trimMargin()
        }
    }
}
