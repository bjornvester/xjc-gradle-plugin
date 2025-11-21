include(
        "test-producer-2x",
        "test-consumer-2x",

        "test-producer-3x",
        "test-producer-3x-binding-files",
        "test-producer-3x-grouped",
        "test-consumer-3x",
        "test-producer-4x",
        "test-consumer-4x",
)

pluginManagement {
    includeBuild("..") // Remove this to test with a binary from mavenLocal

    repositories {
        mavenLocal()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.name == "xjc") {
                // If not using the "includeBuild" and also not a version published to the Gradle Plugin Portal:
                useModule("com.github.bjornvester:xjc-gradle-plugin:1.9.0")
            }
        }
    }
}
