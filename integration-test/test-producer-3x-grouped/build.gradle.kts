plugins {
    id("com.github.bjornvester.xjc")
    id("com.github.bjornvester.xjc.internal.java-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":test-producer-3x"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

tasks.test {
    useJUnitPlatform()
}

xjc {
    xjcVersion.set("3.0.0")

    groups {
        register("group1") {
            xsdFiles = files(xsdDir.file("MySchemaWithFunnyChar.xsd"))
            defaultPackage.set("com.example.group1")
        }
        register("group2") {
            xsdFiles = files(xsdDir.file("MySchemaWithFunnyChar.xsd"))
            defaultPackage.set("com.example.group2")
        }
    }
}
