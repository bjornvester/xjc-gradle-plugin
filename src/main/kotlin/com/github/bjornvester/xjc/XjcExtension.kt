package com.github.bjornvester.xjc

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class XjcExtension @Inject constructor(project: Project) {
    var xsdDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.projectDirectory.dir("src/main/resources"))
    var xsdFiles: ConfigurableFileCollection = project.objects.fileCollection()
    var outputJavaDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("generated/sources/xjc/java"))
    var outputResourcesDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("generated/sources/xjc/resources"))
    var xjcVersion: Property<String> = project.objects.property(String::class.java).convention("2.3.2")
}
