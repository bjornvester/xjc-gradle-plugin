plugins {
    id("com.github.bjornvester.xjc")
    id("com.github.bjornvester.xjc.internal.java-conventions")
}

repositories {
    mavenCentral()
}

xjc {
    generateEpisode.set(true)
    includes.set(listOf("MySchemaWithFunnyChar.xsd"))
    xjcVersion.set("3.0.2")
    addCompilationDependencies.set(false)
}

dependencies {
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
}
