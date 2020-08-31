package com.github.bjornvester.xjc

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class XjcPluginTest {
    @Test
    fun integrationTest() {
        val projectDir = File("integration-test")
        val result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments("clean", "build")
                .withPluginClasspath()
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":test-consumer:xjc")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":test-producer:xjc")?.outcome)

        assertTrue(projectDir.resolve("test-consumer/build/generated/sources/xjc/java/com/github/bjornvester/consumer/MyWrapper.java").exists())
        assertTrue(projectDir.resolve("test-producer/build/generated/sources/xjc/java/com/github/bjornvester/producer/MyElementØ.java").exists())
        assertFalse(projectDir.resolve("test-producer/build/generated/sources/xjc/java/com/github/bjornvester/producer/MyIgnoredElement.java").exists())
    }
}