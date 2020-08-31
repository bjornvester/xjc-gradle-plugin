plugins {
    id("java")
    id("com.github.kad-leeuwg1.xjc")
}

repositories {
    jcenter()
}

xjcPlugin {
    xjcVersion.set("2.3.2")
}

tasks {
    xjc {
        generateEpisode.set(true)
        xsdFiles.setFrom(xsdDir.file("MySchemaWithFunnyChar.xsd"))
    }
}
