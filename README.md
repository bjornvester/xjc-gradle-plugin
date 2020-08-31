[![Gradle Plugin Release](https://img.shields.io/badge/Gradle%20plugin-1.5-blue.svg?logo=data:image/svg+xml;base64,PHN2ZyBpZD0iTGF5ZXJfMSIgZGF0YS1uYW1lPSJMYXllciAxIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA5MCA2Ni4wNiI+PGRlZnM+PHN0eWxlPi5jbHMtMXtmaWxsOiNmZmY7fTwvc3R5bGU+PC9kZWZzPjx0aXRsZT5ncmFkbGUtZWxlcGhhbnQtaWNvbi13aGl0ZS1wcmltYXJ5PC90aXRsZT48cGF0aCBjbGFzcz0iY2xzLTEiIGQ9Ik04NS4xMSw0LjE4YTE0LjI3LDE0LjI3LDAsMCwwLTE5LjgzLS4zNCwxLjM4LDEuMzgsMCwwLDAsMCwyTDY3LDcuNmExLjM2LDEuMzYsMCwwLDAsMS43OC4xMkE4LjE4LDguMTgsMCwwLDEsNzkuNSwyMC4wNkM2OC4xNywzMS4zOCw1My4wNS0uMzYsMTguNzMsMTZhNC42NSw0LjY1LDAsMCwwLTIsNi41NGw1Ljg5LDEwLjE3YTQuNjQsNC42NCwwLDAsMCw2LjMsMS43M2wuMTQtLjA4LS4xMS4wOEwzMS41MywzM2E2MC4yOSw2MC4yOSwwLDAsMCw4LjIyLTYuMTMsMS40NCwxLjQ0LDAsMCwxLDEuODctLjA2aDBhMS4zNCwxLjM0LDAsMCwxLC4wNiwyQTYxLjYxLDYxLjYxLDAsMCwxLDMzLDM1LjM0bC0uMDksMC0yLjYxLDEuNDZhNy4zNCw3LjM0LDAsMCwxLTMuNjEuOTQsNy40NSw3LjQ1LDAsMCwxLTYuNDctMy43MWwtNS41Ny05LjYxQzQsMzItMi41NCw0Ni41NiwxLDY1YTEuMzYsMS4zNiwwLDAsMCwxLjMzLDEuMTFIOC42MUExLjM2LDEuMzYsMCwwLDAsMTAsNjQuODdhOS4yOSw5LjI5LDAsMCwxLDE4LjQyLDAsMS4zNSwxLjM1LDAsMCwwLDEuMzQsMS4xOUgzNS45YTEuMzYsMS4zNiwwLDAsMCwxLjM0LTEuMTksOS4yOSw5LjI5LDAsMCwxLDE4LjQyLDBBMS4zNiwxLjM2LDAsMCwwLDU3LDY2LjA2SDYzLjFhMS4zNiwxLjM2LDAsMCwwLDEuMzYtMS4zNGMuMTQtOC42LDIuNDYtMTguNDgsOS4wNy0yMy40M0M5Ni40MywyNC4xNiw5MC40MSw5LjQ4LDg1LjExLDQuMThaTTYxLjc2LDMwLjA1bC00LjM3LTIuMTloMGEyLjc0LDIuNzQsMCwxLDEsNC4zNywyLjJaIi8+PC9zdmc+)](https://plugins.gradle.org/plugin/com.github.kad-leeuwg1.xjc)
[![GitHub Actions status](https://github.com/kad-leeuwg1/xjc-gradle-plugin/workflows/CI/badge.svg)](https://github.com/kad-leeuwg1/xjc-gradle-plugin/actions)

# xjc-gradle-plugin
A Gradle plugin for running the XJC binding compiler to generate Java source code from XML schemas (xsd files) using JAXB.

## Requirements and features
* **The plugin requires Gradle version 5.6 or later**.

* It has been tested with Java 8 and Java 12.

* It supports XJC version 2.3.2 and later (from Jakarta EE).

* It supports the Gradle build cache (enabled by setting "org.gradle.caching=true" in your gradle.properties file).

* It supports project relocation for the build cache (e.g. you move your project to a new path, or make a new copy/clone of it).
This is especially useful in a CI context, where you might clone PRs and/or branches for a repository in their own locations. 

* It supports parallel execution (enabled with "org.gradle.parallel=true", possibly along with "org.gradle.priority=low", in your gradle.properties file).

* It supports _most_, but not all (yet), of the functionality provided by XJC. Check the configuration section and the road map section to get an idea of what is possible.

## Configuring the plugin
Apply the plugin ID `com.github.kad-leeuwg1.xjc` as documented in the [Gradle Plugin portal page](https://plugins.gradle.org/plugin/com.github.kad-leeuwg1.xjc), e.g. like this (for the Kotlin DSL):

```
plugins {
  id("com.github.kad-leeuwg1.xjc") version "1.5"
}
```

You can configure the plugin using the `xjcPlugin` extension like this:

```
xjcPlugin {
    // Set properties here...
}
``` 

Here is a list of all available properties:

| Property           | Type                  | Default                                                                            | Description                                                                                                  |
|--------------------|-----------------------|------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| xjcVersion         | Provider\<String>     | "2.3.2"                                                                            | The version of XJC to use.                                                                                   |

### Customizing the build dependencies
By default, it will use XJC version 2.3.2 to compile the schemas.
You can set another version through the `xjcVersion` property like this:

```
xjcPlugin {
    xjcVersion.set("2.3.2")
}
```

## Configuring the XJC task

You can configure the default `xjc` task like this:

```
tasks {
    xjc {
        // Set properties here...
    }
}
``` 

Here is a list of all available properties:

| Property           | Type                  | Default                                                                            | Description                                                                                                  |
|--------------------|-----------------------|------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| xsdDir             | DirectoryProperty     | "$projectDir/src<br>/main/resources"                                               | The directory holding the xsd files to compile.                                                              |
| xsdFiles           | FileCollection        | xsdDir<br>&nbsp;&nbsp;.asFileTree<br>&nbsp;&nbsp;.matching { include("**/*.xsd") } | The schemas to compile.<br>If empty, all files in the xsdDir will be compiled.                               |
| outputJavaDir      | DirectoryProperty     | "$buildDir/generated<br>/sources/xjc/java"                                         | The output directory for the generated Java sources.<br>Note that it will be deleted when running XJC.       |
| outputResourcesDir | DirectoryProperty     | "$buildDir/generated<br>/sources/xjc/resources"                                    | The output directory for the generated resources (if any).<br>Note that it will be deleted when running XJC. |
| defaultPackage     | Provider\<String>     | \[not set\]                                                                        | The default package for the generated Java classes.<br>If empty, XJC will infer it from the namespace.       |
| generateEpisode    | Provider\<Boolean>    | false                                                                              | If true, generates an Episode file for the generated Java classes.                                           |
| markGenerated      | Provider\<Boolean>    | true                                                                               | If true, marks the generated code with the annotation `@javax.annotation.Generated`.                           |
| bindingFiles       | FileCollection        | \[empty\]                                                                          | The binding files to use in the schema compiler                                                              |
| options            | ListProperty\<String> | \[empty\]                                                                          | Options to pass to either the XJC core, or to third party plugins in the `xjcPlugins` configuration          |

### Choosing which schemas to generate source code for
By default, it will compile all XML schemas (xsd files) found in the src/main/resource folder.
You can change to another folder through the following configuration:

```
tasks {
    xjc {
        xsdDir.set(layout.projectDirectory.dir("src/main/xsd"))
        // Or
        xsdDir.set(project.file("$projectDir/src/main/xsd"))
    }
}
```

If you don't want to compile all schemas in the folder, you can specify which ones through the xsdFiles property.
Note that they must still be located under the directory specified by xsdDir, or up-to-date checking might not work properly.
Here is an example where all schema files are referenced relative to the xsdDir property:

```
tasks {
    xjc {
        xsdFiles.setFrom(project.files(xsdDir.file("MySchema1.xsd"), xsdDir.file("MySchema2.xsd")))
        // Or
        xsdFiles.setFrom(xsdDir.asFileTree.matching { include("subfolder/**/*.xsd") })
    }
}
```

As it uses the Jakarta version of the tool with new Maven coordinates, the older versions from Oracle are not supported.
You can check if there is a newer version of the tool either on the official [Github repository](https://github.com/eclipse-ee4j/jaxb-ri/releases)
or by searching for the group and name `org.glassfish.jaxb:jaxb-xjc`, e.g. through [MvnRepository](https://mvnrepository.com/artifact/org.glassfish.jaxb/jaxb-xjc).

By applying the plugin, it will register the Java plugin as well if it isn't there already (so the generated source code can be compiled).
It will also add the dependency `jakarta.xml.bind:jakarta.xml.bind-api` to your implementation configuration, as this is needed to compile the source code.
If your project is going to be deployed on a Java/Jakarta EE application server, you may want to exclude this dependency from your runtime and instead use whatever your application server is providing.

### Choosing the file encoding
If your schemas contain characters that do not match your default platform encoding (on English versions of Windows, this will probably be CP-1252),
set the encoding through the file.encoding property for Gradle.
For example, to use UTF-8, put this in your gradle.property file:

```
org.gradle.jvmargs=-Dfile.encoding=UTF-8
```

If you are on a POSIX operating system (e.g. Linux), you may in addition to this need to set your operating system locale to one that supports your encoding.
Otherwise, Java (and therefore also Gradle and XJC) may not be able to create files with names outside of what your default locale supports.
Especially some Docker images, like the Java ECR images from AWS, is by default set to a locale supporting ASCII only.
If this is the case for you, and you want to use UTF-8, you could export an environment variable like this:

```
export LANG=en_US.UTF-8
```

### Enabling the use of the @Generated annotation
If you like to have the generated source code marked with the `@javax.annotation.Generated` annotation, set the `markGenerated` property to true like this:

```
tasks {
    xjc {
        markGenerated.set(false)
    }
}
``` 

Note that while this annotation is found in the Java 8 SDK, it is not present in Java 9 and later.
(However, there is a `@javax.annotation.processing.Generated` annotation, notice the `processing` sub-package, but this is not yet supported by this plugin.)

### Generating episode files
XJC can generate an episode file, which is basically an extended binding file that specifies how the the schema types are associated with the generated Java classes.

You can enable the generation using the generateEpisode property like this:

```
tasks {
    xjc {
        generateEpisode.set(true)
    }
}
```

The file will be generated at META-INF/sun-jaxb.episode and added as a resource to the main source set.

### Consuming episode files
XJC can consume the episode files so that it is possible to compile java classes from a schema in one project, and consume it in XJC generators in other projects so you don't have to compile the same schemas multiple times.
To do this, you need to add the jar file to the configuration named "xjcBindings".

For multi-projects, assuming the episode file is generated in a project called "test-producer", you can do this like this:

```
dependencies {
    implementation(project(":test-producer"))
    xjcBindings(project(":test-producer", "apiElements"))
}
```

### Consuming binding files
You can also provide your own binding files (or custom episode files) through the bindingFiles property:

```
tasks {
    xjc {
        bindingFiles.setFrom(project.files("$projectDir/src/main/bindings/mybinding.xjb"))
    }
}
```

### Activating (third party) XJC plugins
To use third party plugins, supply the relevant dependencies to the `xjcPlugins` configuration.
Then set the plugin options through the `options` property.

For example, to use the "Copyable" plugin from the [JAXB2 Basics](https://github.com/highsource/jaxb2-basics) project, configure the following: 

```
dependencies {
    xjcPlugins("org.jvnet.jaxb2_commons:jaxb2-basics:1.11.1")
}

tasks {
    xjc {
        options.add("-Xcopyable")
    }
}
``` 

If you have trouble activating a plugin and is unsure whether it has been registered, you can run Gradle with the --debug option.
This will print additional information on what plugins were found, what their option names are, and what plugins were activated.
Note that in order to activate a third-party plugin, you must always provide at least one option (and usually just one) from the plugin.

## Road map
Here are some of the features not yet implemented but I have planned for whenever I get the time.

Consumer facing:
* Support the `-npa` option in XJC, suppressing the generation of package level annotations.
* Support for catalog files.
* Support for schemas in wsdl files.
* Support for optionally adding "if-exists" attributes to generated episode files to prevent failures on unreferenced schemas. 
* Support for choosing which, if any, Gradle configuration to add the required dependencies to (e.g. `implementation`, `compileOnly` or none).
* Support for the `@Generated` annotation on Java 9+
* Document how to use the XJC task directly (for having multiple XJC tasks in the same Gradle project)

Internal:
* ~~Automatic test of the integration-test project (it is currently tested manually)~~
* Add additional tests or additional assertions to the current ones

You are welcome to create issues and PRs for anything else.
 
## Alternatives
If you need additional functionality than what is provided here, you may want to try the one from [rackerlabs](https://github.com/rackerlabs/gradle-jaxb-plugin).
There are also a few other plugins for XJC out there.

However, the reason I started this plugin in the first place was because the others ignored the caching aspect, wasn't compatible with Java 11, had file leaks or other problems.
All this may have been solved since, but this is something you may want to consider and be aware of. 
