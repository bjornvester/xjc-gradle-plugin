plugins {
    id("java")
    id("com.github.bjornvester.xjc")
}

repositories {
    jcenter()
}

dependencies {
    implementation(project(":test-producer"))
    xjcBind(project(":test-producer", "apiElements"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.1")
}

tasks.test {
    useJUnitPlatform()
}
