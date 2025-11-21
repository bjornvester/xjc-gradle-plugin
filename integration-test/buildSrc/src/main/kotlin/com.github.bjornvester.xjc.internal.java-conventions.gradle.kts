plugins {
    id("java")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
