package com.github.bjornvester.xjc

import com.sun.codemodel.JCodeModel
import com.sun.tools.xjc.ModelLoader
import com.sun.tools.xjc.Options
import com.sun.tools.xjc.addon.episode.PluginImpl
import org.gradle.api.GradleException
import java.io.File
import javax.inject.Inject

open class XjcWorker @Inject constructor(private val xsdInputFiles: Set<File>,
                                         private val outputJavaDir: File,
                                         private val outputResourceDir: File,
                                         private val defaultPackage: String,
                                         private val episodeFilepath: String) : Runnable {
    override fun run() {
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

        if (defaultPackage.isNotBlank()) {
            options.defaultPackage = defaultPackage
        }

        xsdInputFiles.forEach {
            options.addGrammar(it)
        }

        val jCodeModel = JCodeModel()

        val model = ModelLoader.load(options, jCodeModel, XjcErrorReceiver())
                ?: throw GradleException("Could not load the XJC model")

        model.generateCode(options, XjcErrorReceiver())
                ?: throw GradleException("Could not generate code from the XJC model")

        jCodeModel.build(outputJavaDir, outputResourceDir)

        fixGeneratedEpisodeFile()
    }

    private fun configureGeneratedEpisodeFile(options: Options) {
        if (episodeFilepath.isNotBlank()) {
            val episodePlugin = Options().allPlugins.single { it::class.java == PluginImpl::class.java } as PluginImpl
            episodePlugin.parseArgument(options, arrayOf("-episode", episodeFilepath), 0)
            options.activePlugins.add(episodePlugin)
            options.compatibilityMode = Options.EXTENSION
        }
    }

    private fun fixGeneratedEpisodeFile() {
        if (episodeFilepath.isNotBlank()) {
            // Strip the first comment as it contains a timestamp that messes with the build cache
            val episodeFile = File(episodeFilepath)
            episodeFile.writeText(
                    episodeFile.readText().replaceFirst(
                            Regex(" *<!--.+?-->", RegexOption.DOT_MATCHES_ALL), ""
                    )
            )
        }
    }
}
