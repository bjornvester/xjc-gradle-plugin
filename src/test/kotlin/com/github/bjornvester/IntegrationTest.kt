package com.github.bjornvester

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.lang.management.ManagementFactory
import java.util.stream.Stream

open class IntegrationTest {
    @ParameterizedTest(name = "Test plugin with Java version {0} and Gradle version {1}")
    @MethodSource("provideVersions")
    fun thePluginWorks(javaVersion: String, gradleVersion: String, @TempDir tempDir: File) {
        runGenericBuild(javaVersion, gradleVersion, tempDir)
    }

    private fun runGenericBuild(javaVersion: String, gradleVersion: String, tempDir: File) {
        copyIntegrationTestProject(tempDir)

        // Remove the "includedBuild" declaration from the settings file
        tempDir.resolve(SETTINGS_FILE).writeText(tempDir.resolve(SETTINGS_FILE).readText().replace("includeBuild(\"..\")", ""))

        if (GradleVersion.version(gradleVersion) < GradleVersion.version("7.6")) {
            // The Gradle toolchain provisioning is not supported in older versions
            tempDir.resolve(SETTINGS_FILE)
                .writeText(tempDir.resolve(SETTINGS_FILE).readText().replace("""id\("org.gradle.toolchains.foojay-resolver-convention"\).*""".toRegex(), ""))
        }

        if (javaVersion.equals("8")) {
            // XJC 4 requires Java 11
            tempDir.resolve(SETTINGS_FILE)
                .writeText(tempDir.resolve(SETTINGS_FILE).readText().replace(""""test-consumer-4x",""".toRegex(), ""))

            tempDir.resolve(SETTINGS_FILE)
                .writeText(tempDir.resolve(SETTINGS_FILE).readText().replace(""",\s*"test-producer-4x"""".toRegex(), ""))
        }

        // Set the toolchain Java version for the test project
        tempDir.resolve(JAVA_CONVENTIONS_FILE)
            .writeText(tempDir.resolve(JAVA_CONVENTIONS_FILE).readText().replace("JavaLanguageVersion.of\\(\\d+\\)".toRegex(), "JavaLanguageVersion.of($javaVersion)"))

        GradleRunner
            .create()
            .forwardOutput()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("clean", "check", "-i", "--no-build-cache")
            .withGradleVersion(gradleVersion)
            .withDebug(isDebuggerAttached())
            .build()
    }

    private fun copyIntegrationTestProject(tempDir: File) {
        val rootFolder = File(System.getProperty("GRADLE_ROOT_FOLDER"))
        val integrationTestDir = rootFolder.resolve("integration-test")
        val ignoredDirNames = arrayOf("out", ".gradle", "build")

        FileUtils.copyDirectory(integrationTestDir, tempDir) { copiedResource ->
            ignoredDirNames.none { ignoredDir ->
                copiedResource.isDirectory && copiedResource.name.toString() == ignoredDir
            }
        }
    }

    private fun isDebuggerAttached(): Boolean {
        return ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0
    }

    companion object {
        const val SETTINGS_FILE = "settings.gradle.kts"
        const val JAVA_CONVENTIONS_FILE = "buildSrc/src/main/kotlin/com.github.bjornvester.xjc.internal.java-conventions.gradle.kts"

        @JvmStatic
        @Suppress("unused")
        fun provideVersions(): Stream<Arguments?>? {
            return Stream.of(
                // TODO: There are currently issues with automatically testing with different versions of Java and Gradle
                // For now, we only automatically test with some fairly common versions, and leave the "edge" versions
                // for manual testing
                //Arguments.of("8", "7.3"), // Minimum supported version of Java/Gradle
                //Arguments.of("25", "9.2.1") // Latest tested version of Java/Gradle
                Arguments.of("17", "8.1.1") // Latest tested version of Java/Gradle
            )
        }
    }
}
