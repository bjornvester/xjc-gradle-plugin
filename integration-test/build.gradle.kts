plugins {
    id("java")
    id("com.github.bjornvester.xjc")
}

repositories {
    jcenter()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0")
}

tasks.test {
    useJUnitPlatform()
}
