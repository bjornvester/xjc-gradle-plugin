plugins {
    id("java")
    id("com.github.bjornvester.xjc")
}

repositories {
    jcenter()
}

xjc {
    generateEpisode.set(true)
}
