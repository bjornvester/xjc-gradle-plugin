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
            /*
            All gradle worker processes has XERCES is the classpath.
            This version of XERCES does not support checking for external file access (even if not used).
            This causes it to log a whole bunch of stack traces on the form:
            -- Property "http://javax.xml.XMLConstants/property/accessExternalSchema" is not supported by used JAXP implementation.
            To avoid this, we fork the worker API to a separate process where we can set system properties to select which implementation of a SAXParser to use.
            JDK 8 comes with an internal implementation of a SAXParser also based on XERCES, but supports the properties to control external file access.
            */
            config.isolationMode = IsolationMode.PROCESS
            config.forkOptions.systemProperties = mapOf(
                    "org.xml.sax.parser" to "com.sun.org.apache.xerces.internal.parsers.SAXParser",
                    "javax.xml.parsers.DocumentBuilderFactory" to "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
                    "javax.xml.parsers.SAXParserFactory" to "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"
            )
            config.params(
                    xsdInputFiles,
                    outputJavaDir.get().asFile,
                    outputResourcesDir.get().asFile,
                    defaultPackage.getOrElse(""))
            config.classpath(dependentFiles)
        }

    }

    private fun getXjcExtension() = project.extensions.getByName(XJC_EXTENSION_NAME) as XjcExtension
}
