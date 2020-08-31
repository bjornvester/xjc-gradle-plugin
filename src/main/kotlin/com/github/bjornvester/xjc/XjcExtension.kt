package com.github.bjornvester.xjc

import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

open class XjcExtension @Inject constructor(project: Project) {
    val xjcVersion: Property<String> = project.objects.property(String::class.java).convention("2.3.3")
}
