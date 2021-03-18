package com.github.bjornvester.xjc

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.util.GradleVersion
import java.io.Serializable


@Suppress("unused")
class XjcPlugin : Plugin<Project> {
    companion object {
        const val MINIMUM_GRADLE_VERSION = "6.0"
        const val PLUGIN_ID = "com.github.bjornvester.xjc"
        const val XJC_TASK_NAME = "xjc"
        const val XJC_EXTENSION_NAME = "xjc"
        const val XJC_CONFIGURATION_NAME = "xjc"
        const val XJC_BIND_CONFIGURATION_NAME = "xjcBindings"
        const val XJC_PLUGINS_CONFIGURATION_NAME = "xjcPlugins"

        val DO_NOTHING: Action<*> = object : Action<Any>, Serializable {
            override fun execute(t: Any) {}
        }
    }

    override fun apply(project: Project) {
        project.logger.info("Applying $PLUGIN_ID to project ${project.name}")
        verifyGradleVersion()
        project.plugins.apply(JavaPlugin::class.java)
        val extension = project.extensions.create(XJC_EXTENSION_NAME, XjcExtension::class.java, project)
        val xjcConfiguration = createConfiguration(project, XJC_CONFIGURATION_NAME)
        createConfiguration(project, XJC_BIND_CONFIGURATION_NAME)
        createConfiguration(project, XJC_PLUGINS_CONFIGURATION_NAME)

        xjcConfiguration.defaultDependencies {
            addLater(extension.xjcVersion.map { project.dependencies.create("org.glassfish.jaxb:jaxb-xjc:$it") })
        }

        project.dependencies.addProvider(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, extension.xjcVersion.map { "jakarta.xml.bind:jakarta.xml.bind-api:$it" }, DO_NOTHING)

        project.tasks.register(XJC_TASK_NAME, XjcTask::class.java) {
            val sourceSets = project.properties["sourceSets"] as SourceSetContainer

            sourceSets.named(MAIN_SOURCE_SET_NAME) {
                java.srcDir(outputJavaDir)
                resources.srcDir(outputResourcesDir)
            }
        }

        project.tasks.named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME) {
            dependsOn(XJC_TASK_NAME)
        }

        project.tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
            dependsOn(XJC_TASK_NAME)
        }
    }

    private fun verifyGradleVersion() {
        if (GradleVersion.current() < GradleVersion.version(MINIMUM_GRADLE_VERSION)) {
            throw UnsupportedOperationException("Plugin $PLUGIN_ID requires at least Gradle $MINIMUM_GRADLE_VERSION, but you are using ${GradleVersion.current().version}")
        }
    }

    private fun createConfiguration(project: Project, name: String): Configuration {
        return project.configurations.maybeCreate(name).apply {
            isCanBeConsumed = false
            isCanBeResolved = true
            isVisible = false
        }
    }

}
