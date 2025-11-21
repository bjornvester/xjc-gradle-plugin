plugins {
    id("com.github.bjornvester.xjc")
    id("com.github.bjornvester.xjc.internal.java-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":test-producer-3x"))
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

xjc {
    xjcVersion.set("3.0.2")
    addCompilationDependencies.set(false)

    groups {
        register("group1") {
            includes.set(listOf("MySchemaWithFunnyChar.xsd"))
            defaultPackage.set("com.example.group1")
        }
        register("group2") {
            includes.set(listOf("MySchemaWithFunnyChar.xsd"))
            defaultPackage.set("com.example.group2")
        }
    }
}
