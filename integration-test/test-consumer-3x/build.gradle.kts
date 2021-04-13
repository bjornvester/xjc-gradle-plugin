plugins {
    id("com.github.bjornvester.xjc")
    id("com.github.bjornvester.xjc.internal.java-conventions")
}

repositories {
    jcenter()
}

dependencies {
    implementation(project(":test-producer-3x"))
    //implementation("io.github.threeten-jaxb:threeten-jaxb-core:1.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

tasks.test {
    useJUnitPlatform()
}

xjc {
    xjcVersion.set("3.0.0")
    //bindingFiles = files("$projectDir/src/main/bindings/bindings.xml")
}
