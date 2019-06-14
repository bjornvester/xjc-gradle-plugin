package com.github.bjornvester.xjc

import com.github.bjornvester.xjc.XjcPlugin.Companion.XJC_CONFIGURATION_NAME
import com.github.bjornvester.xjc.XjcPlugin.Companion.XJC_EXTENSION_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.*
import java.io.File
import java.lang.reflect.Proxy
import java.net.URLClassLoader

@CacheableTask
open class XjcTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    var xsdInputDir: DirectoryProperty = getXjcExtension().xsdDir

    @get:InputFiles
    @Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    var xsdFiles = getXjcExtension().xsdFiles

    @get:OutputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    var outputJavaDir: DirectoryProperty = getXjcExtension().outputJavaDir

    @get:OutputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    var outputResourcesDir: DirectoryProperty = getXjcExtension().outputResourcesDir

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
            else -> xsdFiles
        }

        var dependentFiles = project.configurations.named(XJC_CONFIGURATION_NAME).get().resolve()
        project.logger.debug("Loading JAR files: $dependentFiles")
        val originalClassLoader = Thread.currentThread().contextClassLoader
        val dependencies = dependentFiles.map { it.toURI().toURL() }.toTypedArray()

        URLClassLoader(dependencies).use { classLoader ->
            try {
                Thread.currentThread().contextClassLoader = classLoader

                val errorListenerClass = classLoader.loadClass("com.sun.tools.xjc.api.ErrorListener")
                val errorReceiverClass = classLoader.loadClass("com.sun.tools.xjc.ErrorReceiver")
                val errorReceiverFilterClass = classLoader.loadClass("com.sun.tools.xjc.util.ErrorReceiverFilter")
                val jCodeModelClass = classLoader.loadClass("com.sun.codemodel.JCodeModel")
                val modelClass = classLoader.loadClass("com.sun.tools.xjc.model.Model")
                val modelLoaderClass = classLoader.loadClass("com.sun.tools.xjc.ModelLoader")
                val optionsClass = classLoader.loadClass("com.sun.tools.xjc.Options")

                val errorLoggerInvocationHandler = XjcErrorLoggerInvocationHandler(project.logger)
                val errorHandlerProxy = Proxy.newProxyInstance(classLoader, arrayOf<Class<*>>(errorListenerClass), errorLoggerInvocationHandler)

                val options = optionsClass.newInstance()

                xsdInputFiles.forEach {
                    optionsClass.getMethod("addGrammar", File::class.java).invoke(options, it)
                }

                val jCodeModel = jCodeModelClass.newInstance()
                val errorReceiverFilter = errorReceiverFilterClass.getConstructor(errorListenerClass).newInstance(errorHandlerProxy)

                val loadMethod = modelLoaderClass.getMethod("load", optionsClass, jCodeModelClass, errorReceiverClass)
                val model = loadMethod.invoke(null, options, jCodeModel, errorReceiverFilter)
                        ?: throw GradleException("XJC could not load a model from the XSD file(s)")

                modelClass.getMethod("generateCode", optionsClass, errorReceiverClass).invoke(model, options, errorReceiverFilter)

                jCodeModelClass.getMethod("build", File::class.java, File::class.java).invoke(
                        jCodeModel,
                        outputJavaDir.get().asFile,
                        outputResourcesDir.get().asFile
                )
            } finally {
                Thread.currentThread().contextClassLoader = originalClassLoader
            }
        }
    }

    private fun getXjcExtension() = project.extensions.getByName(XJC_EXTENSION_NAME) as XjcExtension
}
