plugins {
    id("com.github.bjornvester.xjc")
    id("com.github.bjornvester.xjc.internal.java-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":test-producer-2x"))
    implementation("org.jvnet.jaxb2_commons:jaxb2-basics-runtime:1.11.1") // Though called "runtime", it is required at compile time...

    xjcBindings(project(":test-producer-2x", "apiElements"))
    xjcPlugins("org.jvnet.jaxb2_commons:jaxb2-basics:1.11.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

tasks.test {
    useJUnitPlatform()
}

xjc {
    xsdDir.set(project.file("$projectDir/src/main/custom-xsd-folder"))
    options.add("-Xcopyable")
    useJakarta.set(false)
}
