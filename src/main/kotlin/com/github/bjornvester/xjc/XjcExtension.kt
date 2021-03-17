package com.github.bjornvester.xjc

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class XjcExtension @Inject constructor(project: Project) {
    val xsdDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.projectDirectory.dir("src/main/resources"))
    var xsdFiles: FileCollection = xsdDir.asFileTree.matching { include("**/*.xsd") }
    val outputJavaDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("generated/sources/xjc/java"))
    val outputResourcesDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("generated/sources/xjc/resources"))
    val xjcVersion: Property<String> = project.objects.property(String::class.java).convention("2.3.3")
    val defaultPackage: Property<String> = project.objects.property(String::class.java)
    val generateEpisode: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)
    var bindingFiles: FileCollection = project.objects.fileCollection()
    val options: ListProperty<String> = project.objects.listProperty(String::class.java)
    val markGenerated: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)
}
