plugins {
    id("java")
    id("com.github.kad-leeuwg1.xjc")
}

repositories {
    jcenter()
}

dependencies {
    implementation(project(":test-producer"))
    implementation("org.jvnet.jaxb2_commons:jaxb2-basics-runtime:1.11.1") // Though called "runtime", it is required at compile time...

    xjcBindings(project(":test-producer", "apiElements"))
    xjcPlugins("org.jvnet.jaxb2_commons:jaxb2-basics:1.11.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.1")
}

xjcPlugin {
   xjcVersion.set("2.3.2")
}

tasks {
    xjc {
        xsdDir.set(project.file("$projectDir/src/main/custom-xsd-folder"))
        options.add("-Xcopyable")
    }

    test {
        useJUnitPlatform()
    }
}
