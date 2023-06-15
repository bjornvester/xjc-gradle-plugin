plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "com.github.bjornvester"
version = "1.7.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.glassfish.jaxb:jaxb-xjc:2.3.8")
    testImplementation("commons-io:commons-io:2.13.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
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
                |- Added support for the Gradle configuration cache.
                |- It now defaults to XJC 3.x, generating source code using the jakarta namespace. This can be changed back to using javax with a configuration change.
                |- Minor bump of default versions.
                |- Added configurations "useJakarta" and "addCompilationDependencies".
                |- Minimum required of version of Gradle is now 6.7 (up from 6.0).""".trimMargin()
        }
    }
}
