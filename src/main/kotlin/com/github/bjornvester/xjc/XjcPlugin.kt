package com.github.bjornvester.xjc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.util.GradleVersion

class XjcPlugin : Plugin<Project> {
    companion object {
        const val MINIMUM_GRADLE_VERSION = "6.7"
        const val MINIMUM_GRADLE_VERSION_GROUPING = "7.0"
        const val PLUGIN_ID = "com.github.bjornvester.xjc"
        const val XJC_TASK_NAME = "xjc"
        const val XJC_EXTENSION_NAME = "xjc"
        const val XJC_CONFIGURATION_NAME = "xjc"
        const val XJC_BIND_CONFIGURATION_NAME = "xjcBindings"
        const val XJC_PLUGINS_CONFIGURATION_NAME = "xjcPlugins"
    }

    override fun apply(project: Project) {
        project.logger.info("Applying $PLUGIN_ID to project ${project.name}")
        verifyGradleVersion()
        project.plugins.apply(JavaPlugin::class.java)
        val extension = project.extensions.create(XJC_EXTENSION_NAME, XjcExtension::class.java)
        val xjcConfiguration = createConfiguration(project, XJC_CONFIGURATION_NAME)
        createConfiguration(project, XJC_BIND_CONFIGURATION_NAME)
        createConfiguration(project, XJC_PLUGINS_CONFIGURATION_NAME)

        xjcConfiguration.defaultDependencies {
            addLater(extension.xjcVersion.map { project.dependencies.create("org.glassfish.jaxb:jaxb-xjc:$it") })
            addLater(extension.xjcVersion.map { project.dependencies.create("org.glassfish.jaxb:jaxb-runtime:$it") })
        }

        project.configurations.named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME) {
            // The listProperty thing is a work-around for https://github.com/gradle/gradle/issues/13255
            dependencies.addAllLater(project.objects.listProperty(Dependency::class.java).convention(extension.addCompilationDependencies.map {
                val deps = listOf<Dependency>().toMutableList()
                if (it) {
                    val xmlBindApiVersion = if (extension.useJakarta.get()) "3.0.1" else "2.3.3"
                    deps.add(project.dependencies.create("jakarta.xml.bind:jakarta.xml.bind-api:$xmlBindApiVersion"))
                    if (extension.markGenerated.get()) {
                        val annotationsApiVersion = if (extension.useJakarta.get()) "2.1.1" else "1.3.5"
                        deps.add(project.dependencies.create("jakarta.annotation:jakarta.annotation-api:$annotationsApiVersion"))
                    }
                }
                deps
            }))
        }

        val defaultTask = addXjcTask(XJC_TASK_NAME, project, extension, null, null)

        extension.groups.all {
            if (GradleVersion.current() < GradleVersion.version(MINIMUM_GRADLE_VERSION_GROUPING)) {
                throw UnsupportedOperationException("Plugin $PLUGIN_ID requires at least Gradle $MINIMUM_GRADLE_VERSION_GROUPING when using the 'groups' property, but you are using ${GradleVersion.current().version}")
            }

            defaultTask.configure {
                enabled = false
            }

            addXjcTask(XJC_TASK_NAME + name.replaceFirstChar(Char::titlecase), project, this, extension.xsdFiles, extension.bindingFiles)
        }
    }

    private fun addXjcTask(name: String, project: Project, group: XjcExtensionGroup, baseXsdFiles: FileCollection?, baseBindingFiles: FileCollection?): TaskProvider<XjcTask> {
        val task = project.tasks.register(name, XjcTask::class.java) {
            defaultPackage.convention(group.defaultPackage)
            xsdDir.convention(group.xsdDir)
            generateEpisode.convention(group.generateEpisode)
            options.convention(group.options)
            markGenerated.convention(group.markGenerated)
            outputJavaDir.convention(group.outputJavaDir)
            outputResourcesDir.convention(group.outputResourcesDir)
            xjcConfiguration.from(project.configurations.named(XJC_CONFIGURATION_NAME))
            xjcBindConfiguration.from(project.configurations.named(XJC_BIND_CONFIGURATION_NAME))
            xjcPluginsConfiguration.from(project.configurations.named(XJC_PLUGINS_CONFIGURATION_NAME))

            // TODO: Once xsdFiles and bindingFiles are a ConfigurableFileCollection, just use them as a convention
            xsdFiles = if (baseXsdFiles == null || !group.xsdFiles.isEmpty) {
                group.xsdFiles
            } else {
                baseXsdFiles
            }

            bindingFiles = if (baseBindingFiles == null || !group.bindingFiles.isEmpty) {
                group.bindingFiles
            } else {
                baseBindingFiles
            }

            val sourceSets = project.properties["sourceSets"] as SourceSetContainer

            sourceSets.named(MAIN_SOURCE_SET_NAME) {
                java.srcDir(outputJavaDir)
                resources.srcDir(outputResourcesDir)
            }
        }

        project.tasks.named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME) {
            dependsOn(task)
        }

        project.tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
            dependsOn(task)
        }

        return task
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
