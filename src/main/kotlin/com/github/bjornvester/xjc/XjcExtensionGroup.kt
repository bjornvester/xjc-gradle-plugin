package com.github.bjornvester.xjc

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface XjcExtensionGroup {
    val name: String
    val xsdDir: DirectoryProperty
    val includes: ListProperty<String>
    val excludes: ListProperty<String>
    val outputJavaDir: DirectoryProperty
    val outputResourcesDir: DirectoryProperty
    val defaultPackage: Property<String>
    val generateEpisode: Property<Boolean>
    val bindingFiles: ConfigurableFileCollection
    val options: ListProperty<String>
    val markGenerated: Property<Boolean>
}
