package com.github.bjornvester.xjc

import com.sun.codemodel.JCodeModel
import com.sun.tools.xjc.BadCommandLineException
import com.sun.tools.xjc.ModelLoader
import com.sun.tools.xjc.Options
import org.gradle.api.GradleException
import org.gradle.workers.WorkAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

abstract class XjcWorker : WorkAction<XjcWorkerParams> {
    private val logger: Logger = LoggerFactory.getLogger(XjcWorker::class.java)

    override fun execute() {
        val options = Options()
        // The disableXmlSecurity option below avoids SAXNotRecognizedExceptions in certain places (though not everywhere)
        // - see the note in XjcTask for additional information on this
        options.disableXmlSecurity = true
        configureGeneratedEpisodeFile(options)

        if (parameters.bindFiles.isNotEmpty()) {
            options.compatibilityMode = Options.EXTENSION
            parameters.bindFiles.forEach { bindFile ->
                options.addBindFile(bindFile)
            }
        }

        if (parameters.defaultPackage.isPresent) {
            options.defaultPackage = parameters.defaultPackage.get()
        }

        parameters.xsdFiles.forEach {
            options.addGrammar(it)
        }

        if (parameters.markGenerated) {
            options.parseArgument(arrayOf("-mark-generated"), 0)
        }

        if (parameters.verbose) {
            options.verbose = true

            logger.debug("All plugins found: " + options.allPlugins.joinToString(separator = ", \n", prefix = "\n") {
                "Class: ${it.javaClass.name}; Option name: ${it.optionName}; Usage description ${it.usage ?: "<N/A>"}"
            })
        }

        if (logger.isDebugEnabled) {
            options.debugMode = true
        }

        if (parameters.options.isNotEmpty() || parameters.verbose) {
            logger.info("Using options: ${parameters.options}")
            setCustomOptions(options)
        }

        if (options.activePlugins.isNotEmpty() || parameters.verbose) {
            logger.info("Active plugins: ${options.activePlugins.map { it.javaClass.name }}")
        }

        val jCodeModel = JCodeModel()

        val model = ModelLoader.load(options, jCodeModel, XjcErrorReceiver())
            ?: throw GradleException("Could not load the XJC model")

        model.generateCode(options, XjcErrorReceiver())
            ?: throw GradleException("Could not generate code from the XJC model")

        if (options.quiet) {
            // Provide a printstream but never read its contents
            PrintStream(ByteArrayOutputStream()).use {
                jCodeModel.build(parameters.outputJavaDir, parameters.outputResourceDir, it)
            }
        } else {
            jCodeModel.build(parameters.outputJavaDir, parameters.outputResourceDir)
        }

        fixGeneratedEpisodeFile()
    }

    private fun setCustomOptions(options: Options) {
        val optionsArray = parameters.options.toTypedArray()
        var i = 0

        while (i < optionsArray.size) {
            val arg = optionsArray[i]

            if (arg.isEmpty()) {
                throw BadCommandLineException("Empty value found in option with index $i")
            } else if (!arg.startsWith('-')) {
                throw BadCommandLineException("Option must start with '-': $arg")
            }

            // The following may load additional plugins
            val consumed = options.parseArgument(optionsArray, i)

            if (consumed == 0) {
                throw BadCommandLineException("Unrecognized option: $arg")
            }

            i += consumed
        }
    }

    private fun configureGeneratedEpisodeFile(options: Options) {
        if (parameters.episodeFilepath.isNotBlank()) {
            options.parseArgument(arrayOf("-episode", parameters.episodeFilepath), 0)
            options.compatibilityMode = Options.EXTENSION
        }
    }

    private fun fixGeneratedEpisodeFile() {
        if (parameters.episodeFilepath.isNotBlank()) {
            // Strip the first comment as it contains a timestamp that messes with the build cache
            val episodeFile = File(parameters.episodeFilepath)
            episodeFile.writeText(
                episodeFile.readText().replaceFirst(
                    Regex(" *<!--.+?-->", RegexOption.DOT_MATCHES_ALL), ""
                )
            )
        }
    }
}
