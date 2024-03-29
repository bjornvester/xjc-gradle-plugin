package com.github.bjornvester.xjc

import com.github.bjornvester.xjc.XjcPlugin.Companion.XJC_EXTENSION_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
open class XjcTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
    private val objectFactory: ObjectFactory,
    private val archiveOperations: ArchiveOperations,
    private val fileSystemOperations: FileSystemOperations,
    projectLayout: ProjectLayout,
) : DefaultTask() {
    @get:Optional
    @get:Input
    val defaultPackage: Property<String> = objectFactory.property(String::class.java).convention(getXjcExtension().defaultPackage)

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val xsdDir: DirectoryProperty = objectFactory.directoryProperty().convention(getXjcExtension().xsdDir)

    @Optional
    @get:Input
    val includes = objectFactory.listProperty(String::class.java).convention(getXjcExtension().includes)

    @Optional
    @get:Input
    val excludes = objectFactory.listProperty(String::class.java).convention(getXjcExtension().excludes)

    @get:Classpath
    val xjcConfiguration = objectFactory.fileCollection()

    @get:Classpath
    val xjcPluginsConfiguration = objectFactory.fileCollection()

    @get:Classpath
    val xjcBindConfiguration = objectFactory.fileCollection()

    @Optional
    @Input
    val generateEpisode: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(getXjcExtension().generateEpisode)

    @Optional
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val bindingFiles = getXjcExtension().bindingFiles

    @get:Input
    val options: ListProperty<String> = objectFactory.listProperty(String::class.java).convention(getXjcExtension().options)

    @get:Input
    val markGenerated: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(getXjcExtension().markGenerated)

    @get:OutputDirectory
    val outputJavaDir: DirectoryProperty = objectFactory.directoryProperty().convention(getXjcExtension().outputJavaDir)

    @get:OutputDirectory
    val outputResourcesDir: DirectoryProperty = objectFactory.directoryProperty().convention(getXjcExtension().outputResourcesDir)

    @get:Internal
    val tmpBindFilesDir: DirectoryProperty = objectFactory.directoryProperty().convention(projectLayout.buildDirectory.dir("xjc/extracted-bind-files"))

    init {
        group = BasePlugin.BUILD_GROUP
        description = "Generates Java classes from XSD files."
    }

    @TaskAction
    fun doCodeGeneration() {
        delete(outputJavaDir)
        delete(outputResourcesDir)
        delete(tmpBindFilesDir)
        outputJavaDir.get().asFile.mkdirs()
        outputResourcesDir.get().asFile.mkdirs()

        validateOptions()

        val xsdFilesAsFilteredFileTree = xsdDir.asFileTree.matching {
            include(this@XjcTask.includes.getOrElse(listOf()))
            exclude(this@XjcTask.excludes.getOrElse(listOf()))
        }

        logger.info("Loading XSD files ${xsdFilesAsFilteredFileTree.files}")
        logger.debug("XSD files are loaded from {}", xsdDir.get())

        val xjcClasspath = xjcConfiguration + xjcPluginsConfiguration
        logger.debug("Loading JAR files for XJC: {}", xjcClasspath)

        extractBindFilesFromJars()
        val allBindingFiles = tmpBindFilesDir.asFileTree.files + bindingFiles.files

        if (allBindingFiles.isNotEmpty()) {
            logger.info("Loading binding files: $allBindingFiles")
        }

        var episodeFilepathArg = ""

        if (generateEpisode.get()) {
            val episodeDir = outputResourcesDir.dir("META-INF")
            episodeDir.get().asFile.mkdirs()
            episodeFilepathArg = episodeDir.get().file("sun-jaxb.episode").asFile.absolutePath
            logger.info("Generating episode file in $episodeFilepathArg")
        }

        // When debugging, temporarily change processIsolation to classLoaderIsolation and remove the forkOptions
        // There is a forkOptions.debug configuration that can be used instead, but unsure how to make it work
        val workQueue = workerExecutor.processIsolation {
            /*
            All gradle worker processes have Xerces2 on the classpath.
            This version of Xerces does not support checking for external file access (even if not used).
            This causes it to log a bunch of stack traces on the form:
            -- Property "http://javax.xml.XMLConstants/property/accessExternalSchema" is not supported by used JAXP implementation.
            To avoid this, we fork the worker API to a separate process where we can set system properties to select which implementation of a SAXParser to use.
            The JDK comes with an internal implementation of a SAXParser, also based on Xerces, but supports the properties to control external file access.
            */
            forkOptions.systemProperties = mapOf(
                "javax.xml.parsers.DocumentBuilderFactory" to "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
                "javax.xml.parsers.SAXParserFactory" to "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
                "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema" to "org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory",
                "javax.xml.accessExternalSchema" to "all"
            )
            if (logger.isDebugEnabled) {
                // This adds debugging information on the XJC method used to find and load services (plugins)
                forkOptions.systemProperties["com.sun.tools.xjc.Options.findServices"] = ""
            }

            // Set encoding (work-around for https://github.com/gradle/gradle/issues/13843)
            // Might be fixed in Gradle 8.3 (unreleased at the time of this writing) - waiting to test it
            //if (GradleVersion.current() < GradleVersion.version("8.3")) {
            forkOptions.environment("LANG", System.getenv("LANG") ?: "C.UTF-8")
            //}

            classpath.from(xjcClasspath)
        }

        System.setProperty("com.sun.tools.xjc.Options.findServices", "true")

        workQueue.submit(XjcWorker::class.java) {
            val task = this@XjcTask
            xsdFiles = xsdFilesAsFilteredFileTree.files
            outputJavaDir = task.outputJavaDir.get().asFile
            outputResourceDir = task.outputResourcesDir.get().asFile
            defaultPackage = task.defaultPackage
            episodeFilepath = episodeFilepathArg
            bindFiles = allBindingFiles
            verbose = task.logger.isDebugEnabled
            options = task.options.get()
            markGenerated = task.markGenerated.get()
        }
    }

    private fun delete(dir: DirectoryProperty) {
        if (dir.get().asFile.exists()) fileSystemOperations.delete { delete(dir) }
    }

    private fun validateOptions() {
        val prohibitedOptions = mapOf(
            "-classpath" to "Leads to resource leaks. Use the 'xjc', 'xjcBindings' or 'xjcPlugins' configuration instead",
            "-d" to "Configured through the 'outputJavaDir' property",
            "-b" to "Configured through the 'bindingFiles' property",
            "-p" to "Configured through the 'defaultPackage' property",
            "-episode" to "Configured through the 'generateEpisode' property",
            "-mark-generated" to "Configured through the 'markGenerated' property"
        )
        options.get().forEach { option ->
            if (prohibitedOptions.containsKey(option)) {
                throw GradleException("the option '$option' is not allowed in this plugin. Reason: ${prohibitedOptions[option]}")
            }
        }
    }

    /**
     * While XJC supports reading bind files inside jar files, there is a file descriptor leak in Xerces
     * that causes the jar files to be locked on Windows. To avoid this, we extract the bind files ourselves.
     */
    private fun extractBindFilesFromJars() {
        logger.debug("Loading binding JAR files: {}", xjcBindConfiguration)

        xjcBindConfiguration.forEach { bindJarFile ->
            if (bindJarFile.extension == "jar") {
                val episodeFiles = archiveOperations.zipTree(bindJarFile).matching { include("**/sun-jaxb.episode") }.files
                if (episodeFiles.isEmpty()) {
                    logger.warn("No episodes (sun-jaxb.episode) found in bind jar file ${bindJarFile.name}")
                } else {
                    episodeFiles.first().copyTo(
                        tmpBindFilesDir
                            .file(bindJarFile.name.removeSuffix(".jar") + ".episode")
                            .get()
                            .asFile
                    )
                }
            } else {
                logger.warn("Unknown binding file configuration type for ${bindJarFile.name}")
            }
        }
    }

    private fun getXjcExtension() = project.extensions.getByName(XJC_EXTENSION_NAME) as XjcExtension
}
