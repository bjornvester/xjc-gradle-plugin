package com.github.bjornvester.xjc

import com.sun.codemodel.JCodeModel
import com.sun.tools.xjc.ModelLoader
import com.sun.tools.xjc.Options
import com.sun.tools.xjc.util.ErrorReceiverFilter
import org.gradle.api.GradleException
import java.io.File
import javax.inject.Inject

open class XjcWorker @Inject constructor(private val xsdInputFiles: Set<File>,
                                         private val outputJavaDir: File,
                                         private val outputResourceDir: File,
                                         private val defaultPackage: String) : Runnable {
    override fun run() {
        val options = Options()

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
    }
}
