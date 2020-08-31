buildscript {
    dependencies {
        // Uncomment the line below for highlighting & code completion during local development
//        classpath(fileTree("../build/libs/"))
    }
}

plugins {
    base
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "6.6.1"
    }
}