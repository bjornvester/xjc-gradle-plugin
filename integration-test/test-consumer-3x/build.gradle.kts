plugins {
    id("com.github.bjornvester.xjc")
    id("com.github.bjornvester.xjc.internal.java-conventions")
}

repositories {
    mavenCentral()
}

xjc {
    xjcVersion.set("3.0.2")
    addCompilationDependencies.set(false)
}

dependencies {
    xjcBindings(project(":test-producer-3x"))

    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
    implementation(project(":test-producer-3x"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
