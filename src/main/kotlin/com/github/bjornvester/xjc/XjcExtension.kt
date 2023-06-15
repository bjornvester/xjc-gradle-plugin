package com.github.bjornvester.xjc

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class XjcExtension @Inject constructor(objects: ObjectFactory, layout: ProjectLayout) : XjcExtensionGroup {
    val useJakarta = objects.property(Boolean::class.java).convention(true)
    val xjcVersion = objects.property(String::class.java).convention(useJakarta.map { if (it) "3.0.2" else "2.3.8" })
    val addCompilationDependencies: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    override val name = "Defaults"
    override val xsdDir = objects.directoryProperty().convention(layout.projectDirectory.dir("src/main/resources"))
    override var xsdFiles: FileCollection = xsdDir.asFileTree.matching { include("**/*.xsd") } // TODO: Once a ConfigurableFileCollection, change to: objects.fileCollection().from(...)
    override val outputJavaDir = objects.directoryProperty().convention(layout.buildDirectory.dir("generated/sources/xjc/java"))
    override val outputResourcesDir = objects.directoryProperty().convention(layout.buildDirectory.dir("generated/sources/xjc/resources"))
    override val defaultPackage = objects.property(String::class.java)
    override val generateEpisode = objects.property(Boolean::class.java).convention(false)
    override var bindingFiles: FileCollection = objects.fileCollection()
    override val options = objects.listProperty(String::class.java)
    override val markGenerated = objects.property(Boolean::class.java).convention(false)

    val groups: NamedDomainObjectContainer<XjcExtensionGroup> = objects.domainObjectContainer(XjcExtensionGroup::class.java)

    init {
        groups.configureEach {
            xsdDir.convention(this@XjcExtension.xsdDir)
            xsdFiles = objects.fileCollection() // Once this field is "val", change to a convention
            outputJavaDir.convention(layout.buildDirectory.dir("generated/sources/xjc-$name/java"))
            outputResourcesDir.convention(layout.buildDirectory.dir("generated/sources/xjc-$name/resources"))
            defaultPackage.convention(this@XjcExtension.defaultPackage)
            generateEpisode.convention(this@XjcExtension.generateEpisode)
            bindingFiles = objects.fileCollection() // Once this field is "val", change to a convention
            options.convention(this@XjcExtension.options)
            markGenerated.convention(this@XjcExtension.markGenerated)
        }
    }
}
