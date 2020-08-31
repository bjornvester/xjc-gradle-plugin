package com.github.bjornvester.xjc

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
open class XjcTask @Inject constructor(private val workerExecutor: WorkerExecutor) : DefaultTask() {

    @get:Optional
    @get:Input
    val defaultPackage: Property<String> = project.objects.property(String::class.java)

    // Only used for up-to-date checking
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val xsdDir: DirectoryProperty = project.objects.directoryProperty()
            .convention(project.layout.projectDirectory.dir("src/main/resources"))

    @Optional
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val xsdFiles: ConfigurableFileCollection = project.objects.fileCollection()
            .from(xsdDir.asFileTree.matching { it.include("**/*.xsd") })

    @get:Classpath
    val xjcConfiguration: NamedDomainObjectProvider<Configuration> = project.configurations
            .named(XjcPlugin.XJC_CONFIGURATION_NAME)

    @get:Classpath
    val xjcPluginsConfiguration: NamedDomainObjectProvider<Configuration> = project.configurations
            .named(XjcPlugin.XJC_PLUGINS_CONFIGURATION_NAME)

    @get:Classpath
    val xjcBindConfiguration: NamedDomainObjectProvider<Configuration> = project.configurations
            .named(XjcPlugin.XJC_BIND_CONFIGURATION_NAME)

    @Optional
    @Input
    val generateEpisode: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    @Optional
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val bindingFiles: ConfigurableFileCollection = project.objects.fileCollection()

    @get:Input
    val options: ListProperty<String> = project.objects.listProperty(String::class.java)

    @get:Input
    val markGenerated: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    @get:OutputDirectory
    val outputJavaDir: DirectoryProperty = project.objects.directoryProperty()
            .convention(project.layout.buildDirectory.dir("generated/sources/xjc/java"))

    @get:OutputDirectory
    val outputResourcesDir: DirectoryProperty = project.objects.directoryProperty()
            .convention(project.layout.buildDirectory.dir("generated/sources/xjc/resources"))

    @get:Internal
    val tmpBindFiles: DirectoryProperty = project.objects.directoryProperty()
            .convention(project.layout.buildDirectory.dir("xjc/extracted-bind-files"))

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

        validateOptions()

        logger.info("Loading XSD files ${xsdFiles.files}")
        logger.debug("XSD files are loaded from ${xsdDir.get()}")

        val xjcClasspath = xjcConfiguration.get().resolve() + xjcPluginsConfiguration
        logger.debug("Loading JAR files for XJC: $xjcClasspath")

        extractBindFilesFromJars()
        val allBindingFiles = tmpBindFiles.asFileTree.files + bindingFiles.files

        if (allBindingFiles.isNotEmpty()) {
            logger.info("Loading binding files: $allBindingFiles")
        }

        var episodeFilepath = ""

        if (generateEpisode.get()) {
            val episodeDir = outputResourcesDir.dir("META-INF")
            project.mkdir(episodeDir)
            episodeFilepath = episodeDir.get().file("sun-jaxb.episode").asFile.absolutePath
            logger.info("Generating episode file in $episodeFilepath")
        }

        val workQueue = workerExecutor.processIsolation { config ->
            /*
            All gradle worker processes have Xerces2 on the classpath.
            This version of Xerces does not support checking for external file access (even if not used).
            This causes it to log a whole bunch of stack traces on the form:
            -- Property "http://javax.xml.XMLConstants/property/accessExternalSchema" is not supported by used JAXP implementation.
            To avoid this, we fork the worker API to a separate process where we can set system properties to select which implementation of a SAXParser to use.
            The JDK comes with an internal implementation of a SAXParser, also based on Xerces, but supports the properties to control external file access.
            */
            config.forkOptions.systemProperties = mapOf(
                    "javax.xml.parsers.DocumentBuilderFactory" to "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
                    "javax.xml.parsers.SAXParserFactory" to "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
                    "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema" to "org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory",
                    "javax.xml.accessExternalSchema" to "all"
            )

            if (logger.isDebugEnabled) {
                // This adds debugging information on the XJC method used to find and load services (plugins)
                config.forkOptions.systemProperties["com.sun.tools.xjc.Options.findServices"] = ""
            }

            config.classpath.from(xjcClasspath)
        }

        System.setProperty("com.sun.tools.xjc.Options.findServices", "true")

        workQueue.submit(XjcWorker::class.java) { params ->
            params.xsdFiles = xsdFiles.files
            params.outputJavaDir = outputJavaDir.get().asFile
            params.outputResourceDir = outputResourcesDir.get().asFile
            params.defaultPackage = defaultPackage
            params.episodeFilepath = episodeFilepath
            params.bindFiles = allBindingFiles
            params.verbose = logger.isDebugEnabled
            params.options = options.get()
            params.markGenerated = markGenerated.get()
        }
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
    }
}
