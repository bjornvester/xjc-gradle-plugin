package com.github.bjornvester.xjc

import com.sun.codemodel.JCodeModel
import com.sun.tools.xjc.ModelLoader
import com.sun.tools.xjc.Options
import org.gradle.api.GradleException
import org.gradle.workers.WorkAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

abstract class XjcWorker : WorkAction<XjcWorkerParams> {
    private val logger: Logger = LoggerFactory.getLogger(XjcWorker::class.java)

    override fun execute() {
        val options = Options()
        options.disableXmlSecurity = true // Avoids SAXNotRecognizedExceptions in certain places (though not everywhere) - see the note in XjcTask for additional information on this
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

        if (parameters.options.isNotEmpty()) {
            logger.info("Using options: ${parameters.options}")
            // The following may load additional plugins
            val optionsArray = parameters.options.toTypedArray()
            parameters.options.forEachIndexed { i, _ ->
                options.parseArgument(optionsArray, i)
            }
        }

        if (options.activePlugins.isEmpty() || parameters.verbose) {
            logger.info("Active plugins: ${options.activePlugins.map { it.javaClass.name }}")
        }

        val jCodeModel = JCodeModel()

        val model = ModelLoader.load(options, jCodeModel, XjcErrorReceiver())
                ?: throw GradleException("Could not load the XJC model")

        model.generateCode(options, XjcErrorReceiver())
                ?: throw GradleException("Could not generate code from the XJC model")

        jCodeModel.build(parameters.outputJavaDir, parameters.outputResourceDir)

        fixGeneratedEpisodeFile()
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
