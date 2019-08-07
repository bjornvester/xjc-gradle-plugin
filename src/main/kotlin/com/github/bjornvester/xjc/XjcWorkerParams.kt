package com.github.bjornvester.xjc

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters
import java.io.File

interface XjcWorkerParams : WorkParameters {
    var xsdFiles: Set<File>
    var outputJavaDir: DirectoryProperty
    var outputResourceDir: DirectoryProperty
    var defaultPackage: Property<String>
    var episodeFilepath: String
    var bindFiles: Set<File>
}