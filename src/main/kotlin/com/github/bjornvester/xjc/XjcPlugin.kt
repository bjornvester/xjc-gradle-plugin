package com.github.bjornvester.xjc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.util.GradleVersion

@Suppress("unused")
class XjcPlugin : Plugin<Project> {
    companion object {
        const val MINIMUM_GRADLE_VERSION = "5.6"
        const val PLUGIN_ID = "com.github.bjornvester.xjc"
        const val XJC_EXTENSION_NAME = "xjcPlugin"
        const val XJC_CONFIGURATION_NAME = "xjc"
        const val XJC_TASK_NAME = "xjc"
        const val XJC_BIND_CONFIGURATION_NAME = "xjcBindings"
        const val XJC_PLUGINS_CONFIGURATION_NAME = "xjcPlugins"
    }

    override fun apply(project: Project) {
        project.logger.info("Applying $PLUGIN_ID to project ${project.name}")
        verifyGradleVersion()
        project.plugins.apply(JavaPlugin::class.java)
        val extension = project.extensions.create(XJC_EXTENSION_NAME, XjcExtension::class.java, project)
        val xjcConfiguration = project.configurations.maybeCreate(XJC_CONFIGURATION_NAME)
        project.configurations.maybeCreate(XJC_BIND_CONFIGURATION_NAME)
        project.configurations.maybeCreate(XJC_PLUGINS_CONFIGURATION_NAME)

        xjcConfiguration.defaultDependencies {
            it.add(project.dependencies.create("org.glassfish.jaxb:jaxb-xjc:${extension.xjcVersion.get()}"))
        }

        project.configurations.named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME) {
            it.dependencies.add(project.dependencies.create("jakarta.xml.bind:jakarta.xml.bind-api:${extension.xjcVersion.get()}"))
        }

        project.tasks.register(XJC_TASK_NAME, XjcTask::class.java) { xjcTask ->
            val sourceSets = project.properties["sourceSets"] as SourceSetContainer

            sourceSets.named(MAIN_SOURCE_SET_NAME) {
                it.java.srcDir(xjcTask.outputJavaDir)
                it.resources.srcDir(xjcTask.outputResourcesDir)
            }
        }

        project.tasks.named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME) {
            it.dependsOn(XJC_TASK_NAME)
        }

        project.tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
            it.dependsOn(XJC_TASK_NAME)
        }
    }

    private fun verifyGradleVersion() {
        if (GradleVersion.current() < GradleVersion.version(MINIMUM_GRADLE_VERSION)) {
            throw UnsupportedOperationException("Plugin $PLUGIN_ID requires at least Gradle $MINIMUM_GRADLE_VERSION, " +
                    "but you are using ${GradleVersion.current().version}")
        }
    }
}
