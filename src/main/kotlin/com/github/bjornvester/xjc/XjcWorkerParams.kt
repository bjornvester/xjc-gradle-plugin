package com.github.bjornvester.xjc

import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters
import java.io.File

interface XjcWorkerParams : WorkParameters {
    var xsdFiles: Set<File>
    var outputJavaDir: File
    var outputResourceDir: File
    var defaultPackage: Property<String>
    var episodeFilepath: String
    var bindFiles: Set<File>
    var verbose: Boolean
    var options: List<String>
    var markGenerated: Boolean
}