package com.github.bjornvester

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.lang.management.ManagementFactory
import java.util.stream.Stream
import kotlin.text.RegexOption.DOT_MATCHES_ALL

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

        if (gradleVersion == "6.0") {
            // If we test with an old version of Gradle that does not support toolchains, remove it
            // Unfortunately, this means we have to test with whatever we are running the build with
            tempDir.resolve(JAVA_CONVENTIONS_FILE)
                .writeText(tempDir.resolve(JAVA_CONVENTIONS_FILE).readText().replace("toolchain \\{.*?}".toRegex(DOT_MATCHES_ALL), ""))
        } else {
            // Set the Java version
            tempDir.resolve(JAVA_CONVENTIONS_FILE)
                .writeText(tempDir.resolve(JAVA_CONVENTIONS_FILE).readText().replace("JavaLanguageVersion.of(8)", "JavaLanguageVersion.of($javaVersion)"))
        }
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
                Arguments.of("8", "7.0"),
                Arguments.of("11", "6.0"),
                Arguments.of("11", "7.0"),
                Arguments.of("16", "7.0")
            )
        }
    }
}
