package com.github.bjornvester.xjc

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface XjcExtensionGroup {
    val name: String
    val xsdDir: DirectoryProperty
    var xsdFiles: FileCollection // Due to backwards-compatibility concerns, we cannot yet use "val" and ConfigurableFileCollection
    val outputJavaDir: DirectoryProperty
    val outputResourcesDir: DirectoryProperty
    val defaultPackage: Property<String>
    val generateEpisode: Property<Boolean>
    var bindingFiles: FileCollection // Due to backwards-compatibility concerns, we cannot yet use "val" and ConfigurableFileCollection
    val options: ListProperty<String>
    val markGenerated: Property<Boolean>
}
