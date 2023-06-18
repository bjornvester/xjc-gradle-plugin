package com.github.bjornvester.xjc

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class XjcExtension @Inject constructor(objects: ObjectFactory, layout: ProjectLayout) : XjcExtensionGroup {
    val useJakarta = objects.property(Boolean::class.java).convention(true)
    val xjcVersion = objects.property(String::class.java).convention(useJakarta.map { if (it) "3.0.2" else "2.3.8" })
    val addCompilationDependencies = objects.property(Boolean::class.java).convention(true)

    override val name = "Defaults"
    final override val xsdDir = objects.directoryProperty().convention(layout.projectDirectory.dir("src/main/resources"))
    override val includes = objects.listProperty(String::class.java).convention(listOf("**/*.xsd"))
    override val excludes = objects.listProperty(String::class.java)
    override val outputJavaDir = objects.directoryProperty().convention(layout.buildDirectory.dir("generated/sources/xjc/java"))
    override val outputResourcesDir = objects.directoryProperty().convention(layout.buildDirectory.dir("generated/sources/xjc/resources"))
    override val defaultPackage = objects.property(String::class.java)
    override val generateEpisode = objects.property(Boolean::class.java).convention(false)
    override val bindingFiles = objects.fileCollection().from(xsdDir.asFileTree.matching { include("**/*.xjb") })
    override val options = objects.listProperty(String::class.java)
    override val markGenerated = objects.property(Boolean::class.java).convention(false)

    val groups: NamedDomainObjectContainer<XjcExtensionGroup> = objects.domainObjectContainer(XjcExtensionGroup::class.java)

    init {
        groups.configureEach {
            xsdDir.convention(this@XjcExtension.xsdDir)
            includes.convention(this@XjcExtension.includes)
            excludes.convention(this@XjcExtension.excludes)
            outputJavaDir.convention(layout.buildDirectory.dir("generated/sources/xjc-$name/java"))
            outputResourcesDir.convention(layout.buildDirectory.dir("generated/sources/xjc-$name/resources"))
            defaultPackage.convention(this@XjcExtension.defaultPackage)
            generateEpisode.convention(this@XjcExtension.generateEpisode)
            bindingFiles.setFrom(this@XjcExtension.bindingFiles)
            options.convention(this@XjcExtension.options)
            markGenerated.convention(this@XjcExtension.markGenerated)
        }
    }
}
