package com.github.bjornvester.xjc

import com.sun.codemodel.JCodeModel
import com.sun.tools.xjc.ModelLoader
import com.sun.tools.xjc.Options
import com.sun.tools.xjc.addon.episode.PluginImpl
import org.gradle.api.GradleException
import org.gradle.workers.WorkAction
import java.io.File

abstract class XjcWorker : WorkAction<XjcWorkerParams> {
    override fun execute() {
        try {
            doWork()
        } finally {
            /*
                There is a file leak in XJC (in class PluginImpl) when generating episodes.
                The leaking resource gets closed in a finalize method, so attempt to do that by nudging the VM to garbage collect it.
                No guarantees of cause.
                A PR to fix the leak has been created here: https://github.com/eclipse-ee4j/jaxb-ri/pull/1339.
                At the time of this writing, it has not been merged in yet (and even if/when it does, it will probably take a while for a new release to come out).
            */
            System.gc()
        }
    }

    private fun doWork() {
        val options = Options()
        options.disableXmlSecurity = true // Avoids SAXNotRecognizedExceptions - see the note in XjcTask for additional information on this
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

        val jCodeModel = JCodeModel()

        val model = ModelLoader.load(options, jCodeModel, XjcErrorReceiver())
                ?: throw GradleException("Could not load the XJC model")

        model.generateCode(options, XjcErrorReceiver())
                ?: throw GradleException("Could not generate code from the XJC model")

        jCodeModel.build(parameters.outputJavaDir.get().asFile, parameters.outputResourceDir.get().asFile)

        fixGeneratedEpisodeFile()
    }

    private fun configureGeneratedEpisodeFile(options: Options) {
        if (parameters.episodeFilepath.isNotBlank()) {
            val episodePlugin = Options().allPlugins.single { it::class.java == PluginImpl::class.java } as PluginImpl
            episodePlugin.parseArgument(options, arrayOf("-episode", parameters.episodeFilepath), 0)
            options.activePlugins.add(episodePlugin)
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
