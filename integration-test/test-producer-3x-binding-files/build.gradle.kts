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
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

tasks.test {
    useJUnitPlatform()
}
