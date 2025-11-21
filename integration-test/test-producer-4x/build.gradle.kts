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
}
