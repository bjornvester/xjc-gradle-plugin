plugins {
    id("java")
    id("com.github.bjornvester.xjc")
}

repositories {
    jcenter()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
}

tasks.test {
    useJUnitPlatform()
}

xjc {
    xsdDir.set(project.layout.projectDirectory.dir("src/main/resources"))
}
