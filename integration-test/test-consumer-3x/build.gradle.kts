plugins {
    id("com.github.bjornvester.xjc")
    id("com.github.bjornvester.xjc.internal.java-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    xjcBindings(project(":test-producer-3x"))

    implementation(project(":test-producer-3x"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

tasks.test {
    useJUnitPlatform()
}
