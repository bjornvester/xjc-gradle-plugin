plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "com.github.bjornvester"
version = "1.7.0-SNAPSHOT"

repositories {
    mavenCentral()
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
    gradleVersion = "latest"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
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
                            |- Support grouping XSDs and generate them with different configurations""".trimMargin()
        }
    }
}
