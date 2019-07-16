package com.github.bjornvester.xjc

import com.github.bjornvester.xjc.XjcPlugin.Companion.XJC_CONFIGURATION_NAME
import com.github.bjornvester.xjc.XjcPlugin.Companion.XJC_EXTENSION_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.*
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
open class XjcTask @Inject constructor(private val workerExecutor: WorkerExecutor) : DefaultTask() {
    @get:Optional
    @get:Input
    val defaultPackage = getXjcExtension().defaultPackage

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val xsdInputDir: DirectoryProperty = getXjcExtension().xsdDir

    @Optional
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val xsdFiles = getXjcExtension().xsdFiles

    @get:OutputDirectory
    val outputJavaDir: DirectoryProperty = getXjcExtension().outputJavaDir

    @get:OutputDirectory
    val outputResourcesDir: DirectoryProperty = getXjcExtension().outputResourcesDir

    init {
        group = BasePlugin.BUILD_GROUP
        description = "Generates Java classes from XSD files."
        dependsOn(project.configurations.named(XJC_CONFIGURATION_NAME))
    }

    @TaskAction
    fun doCodeGeneration() {
        project.delete(outputJavaDir)
        project.delete(outputResourcesDir)
        project.mkdir(outputJavaDir)
        project.mkdir(outputResourcesDir)

        val xsdInputFiles = when {
            xsdFiles.isEmpty -> xsdInputDir.asFileTree.matching { it.include("**/*.xsd") }.files
            else -> xsdFiles.toSet()
        }

        var dependentFiles = project.configurations.named(XJC_CONFIGURATION_NAME).get().resolve()
        project.logger.debug("Loading JAR files: $dependentFiles")

        workerExecutor.submit(XjcWorker::class.java) { config ->
            config.isolationMode = IsolationMode.CLASSLOADER
            config.params(xsdInputFiles,
                    outputJavaDir.get().asFile,
                    outputResourcesDir.get().asFile,
                    defaultPackage.getOrElse(""))
            config.classpath(dependentFiles)
        }

    }

    private fun getXjcExtension() = project.extensions.getByName(XJC_EXTENSION_NAME) as XjcExtension
}
