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
                                         private val outputResourceDir: File) : Runnable {
    override fun run() {
        val options = Options()

        xsdInputFiles.forEach {
            options.addGrammar(it)
        }

        val jCodeModel = JCodeModel()
        val model = ModelLoader.load(options, jCodeModel, XjcErrorReceiver())
                ?: throw GradleException("Could not load the XJC model")
        model.generateCode(options, ErrorReceiverFilter())
        jCodeModel.build(outputJavaDir, outputResourceDir)
    }
}
