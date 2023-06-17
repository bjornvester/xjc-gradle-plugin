plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.5.0")
}

includeBuild("..")

include(
        "test-producer-2x",
        "test-producer-3x",
        "test-producer-3x-binding-files",
        "test-producer-3x-grouped",
        "test-consumer-2x",
        "test-consumer-3x"
)
