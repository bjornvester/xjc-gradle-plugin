plugins {
    id("com.github.bjornvester.xjc")
    id("com.github.bjornvester.xjc.internal.java-conventions")
}

repositories {
    jcenter()
}

xjc {
    generateEpisode.set(true)
    xsdFiles = files(xsdDir.file("MySchemaWithFunnyChar.xsd"))
    xjcVersion.set("3.0.0")
}
