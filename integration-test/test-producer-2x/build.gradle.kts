plugins {
    id("com.github.bjornvester.xjc")
    id("com.github.bjornvester.xjc.internal.java-conventions")
}

repositories {
    mavenCentral()
}

xjc {
    generateEpisode.set(true)
    xsdFiles = files(xsdDir.file("MySchemaWithFunnyChar.xsd"))
}
