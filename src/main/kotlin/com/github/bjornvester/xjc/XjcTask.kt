package com.github.bjornvester.xjc

import com.github.bjornvester.xjc.XjcPlugin.Companion.XJC_EXTENSION_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.*
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import java.io.File
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

    @get:Classpath
    val xjcConfiguration: NamedDomainObjectProvider<Configuration> = project.configurations
            .named(XjcPlugin.XJC_CONFIGURATION_NAME)

    @get:Classpath
    val xjcBindConfiguration: NamedDomainObjectProvider<Configuration> = project.configurations
            .named(XjcPlugin.XJC_BIND_CONFIGURATION_NAME)

    @Optional
    @Input
    val generateEpisode = getXjcExtension().generateEpisode

    @Optional
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val bindingFiles = getXjcExtension().bindingFiles

    @get:OutputDirectory
    val outputJavaDir: DirectoryProperty = getXjcExtension().outputJavaDir

    @get:OutputDirectory
    val outputResourcesDir: DirectoryProperty = getXjcExtension().outputResourcesDir

    @get:OutputDirectory
    val tmpBindFiles: DirectoryProperty = project.objects.directoryProperty().convention(
            project.layout.buildDirectory.dir("xjc/extracted-bind-files")
    )

    init {
        group = BasePlugin.BUILD_GROUP
        description = "Generates Java classes from XSD files."
    }

    @TaskAction
    fun doCodeGeneration() {
        project.delete(outputJavaDir)
        project.delete(outputResourcesDir)
        project.delete(tmpBindFiles)
        project.mkdir(outputJavaDir)
        project.mkdir(outputResourcesDir)

        val xsdInputFiles = when {
            xsdFiles.isEmpty -> xsdInputDir.asFileTree.matching { it.include("**/*.xsd") }.files
            else -> xsdFiles.toSet()
        }

        val xjcClasspath = xjcConfiguration.get().resolve()
        logger.debug("Loading JAR files for XJC: $xjcClasspath")

        val bindFiles = extractBindFilesFromJars()

        if (!bindingFiles.isEmpty) {
            bindFiles.addAll(bindingFiles)
        }

        logger.info("Loading binding files: $bindingFiles")

        var episodeFilepath = ""

        if (generateEpisode.get()) {
            val episodeDir = outputResourcesDir.dir("META-INF")
            project.mkdir(episodeDir)
            episodeFilepath = episodeDir.get().file("sun-jaxb.episode").asFile.absolutePath
        }

        // TODO: See worker API improvements in https://docs.gradle.org/5.6-rc-1/release-notes.html
        workerExecutor.submit(XjcWorker::class.java) { config ->
            /*
            All gradle worker processes have Xerces2 on the classpath.
            This version of Xerces does not support checking for external file access (even if not used).
            This causes it to log a whole bunch of stack traces on the form:
            -- Property "http://javax.xml.XMLConstants/property/accessExternalSchema" is not supported by used JAXP implementation.
            To avoid this, we fork the worker API to a separate process where we can set system properties to select which implementation of a SAXParser to use.
            JDK 8 comes with an internal implementation of a SAXParser, also based on XERCES, but supports the properties to control external file access.
            */
            config.isolationMode = IsolationMode.PROCESS
            config.forkOptions.systemProperties = mapOf(
                    "javax.xml.parsers.DocumentBuilderFactory" to "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
                    "javax.xml.parsers.SAXParserFactory" to "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
                    "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema" to "org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory"
            )
            config.params(
                    xsdInputFiles,
                    outputJavaDir.get().asFile,
                    outputResourcesDir.get().asFile,
                    defaultPackage.getOrElse(""),
                    episodeFilepath,
                    bindFiles
            )
            config.classpath(xjcClasspath)
        }
    }

    /**
     * While XJC supports reading bind files inside jar files, there is a file descriptor leak in Xerces
     * that causes the jar files to be locked on Windows. To avoid this, we extract the bind files ourselves.
     */
    private fun extractBindFilesFromJars(): MutableSet<File> {
        val bindJarFiles = xjcBindConfiguration.get().resolve()
        logger.debug("Loading binding JAR files: $bindJarFiles")

        bindJarFiles.forEach { bindJarFile ->
            if (bindJarFile.extension == "jar") {
                val episodeFiles = project.zipTree(bindJarFile).filter { it.name == "sun-jaxb.episode" }.files
                if (episodeFiles.isEmpty()) {
                    logger.warn("No episodes (sun-jaxb.episode) found in bind jar file ${bindJarFile.name}")
                } else {
                    episodeFiles.first().copyTo(
                            tmpBindFiles
                                    .file(bindJarFile.name.removeSuffix(".jar") + ".episode")
                                    .get()
                                    .asFile
                    )
                }
            } else {
                logger.warn("Unknown binding file configuration type for ${bindJarFile.name}")
            }
        }

        return tmpBindFiles.asFileTree.files
    }

    private fun getXjcExtension() = project.extensions.getByName(XJC_EXTENSION_NAME) as XjcExtension
}
