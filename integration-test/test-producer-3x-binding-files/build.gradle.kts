plugins {
    id("com.github.bjornvester.xjc")
    id("com.github.bjornvester.xjc.internal.java-conventions")
}

repositories {
    mavenCentral()
}

xjc {
    xsdDir.set(layout.projectDirectory.dir("src/main/xsd"))
    bindingFiles.setFrom(layout.projectDirectory.dir("src/main/xjb").asFileTree.matching { include("**/*.xjb") })
    xjcVersion.set("3.0.2")
    addCompilationDependencies.set(false)
}

dependencies {
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
