[![Gradle Plugin Release](https://img.shields.io/badge/Gradle%20plugin-1.6.0-blue.svg?logo=data:image/svg+xml;base64,PHN2ZyBpZD0iTGF5ZXJfMSIgZGF0YS1uYW1lPSJMYXllciAxIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA5MCA2Ni4wNiI+PGRlZnM+PHN0eWxlPi5jbHMtMXtmaWxsOiNmZmY7fTwvc3R5bGU+PC9kZWZzPjx0aXRsZT5ncmFkbGUtZWxlcGhhbnQtaWNvbi13aGl0ZS1wcmltYXJ5PC90aXRsZT48cGF0aCBjbGFzcz0iY2xzLTEiIGQ9Ik04NS4xMSw0LjE4YTE0LjI3LDE0LjI3LDAsMCwwLTE5LjgzLS4zNCwxLjM4LDEuMzgsMCwwLDAsMCwyTDY3LDcuNmExLjM2LDEuMzYsMCwwLDAsMS43OC4xMkE4LjE4LDguMTgsMCwwLDEsNzkuNSwyMC4wNkM2OC4xNywzMS4zOCw1My4wNS0uMzYsMTguNzMsMTZhNC42NSw0LjY1LDAsMCwwLTIsNi41NGw1Ljg5LDEwLjE3YTQuNjQsNC42NCwwLDAsMCw2LjMsMS43M2wuMTQtLjA4LS4xMS4wOEwzMS41MywzM2E2MC4yOSw2MC4yOSwwLDAsMCw4LjIyLTYuMTMsMS40NCwxLjQ0LDAsMCwxLDEuODctLjA2aDBhMS4zNCwxLjM0LDAsMCwxLC4wNiwyQTYxLjYxLDYxLjYxLDAsMCwxLDMzLDM1LjM0bC0uMDksMC0yLjYxLDEuNDZhNy4zNCw3LjM0LDAsMCwxLTMuNjEuOTQsNy40NSw3LjQ1LDAsMCwxLTYuNDctMy43MWwtNS41Ny05LjYxQzQsMzItMi41NCw0Ni41NiwxLDY1YTEuMzYsMS4zNiwwLDAsMCwxLjMzLDEuMTFIOC42MUExLjM2LDEuMzYsMCwwLDAsMTAsNjQuODdhOS4yOSw5LjI5LDAsMCwxLDE4LjQyLDAsMS4zNSwxLjM1LDAsMCwwLDEuMzQsMS4xOUgzNS45YTEuMzYsMS4zNiwwLDAsMCwxLjM0LTEuMTksOS4yOSw5LjI5LDAsMCwxLDE4LjQyLDBBMS4zNiwxLjM2LDAsMCwwLDU3LDY2LjA2SDYzLjFhMS4zNiwxLjM2LDAsMCwwLDEuMzYtMS4zNGMuMTQtOC42LDIuNDYtMTguNDgsOS4wNy0yMy40M0M5Ni40MywyNC4xNiw5MC40MSw5LjQ4LDg1LjExLDQuMThaTTYxLjc2LDMwLjA1bC00LjM3LTIuMTloMGEyLjc0LDIuNzQsMCwxLDEsNC4zNywyLjJaIi8+PC9zdmc+)](https://plugins.gradle.org/plugin/com.github.bjornvester.xjc)
[![GitHub Actions status](https://github.com/bjornvester/xjc-gradle-plugin/workflows/CI/badge.svg)](https://github.com/bjornvester/xjc-gradle-plugin/actions)

# xjc-gradle-plugin

A Gradle plugin for running the XJC binding compiler to generate Java source code from XML schemas (xsd files) using JAXB.

## Requirements and features

* The plugin requires Gradle version 6.7 or later.
* It has been tested with Java 8 and Java 17.
* It has been tested with XJC version 2.3.3 (using the `javax` namespace) and 3.0.2 (using the `jakarta` namespace). Defaults to the jakarta variant.
* It supports the Gradle build cache (enabled by setting "org.gradle.caching=true" in your gradle.properties file).
* It supports the Gradle configuration cache (enabled by setting `org.gradle.configuration-cache=true` in your gradle.properties file").
* It supports project relocation for the build cache (e.g. you move your project to a new path, or make a new copy/clone of it).
  This is especially useful in a CI context, where you might clone PRs and/or branches for a repository in their own locations.
* It supports parallel execution (enabled with "org.gradle.parallel=true", possibly along with "org.gradle.priority=low", in your gradle.properties file).
* It supports _most_, but not all (yet), of the functionality provided by XJC. Check the configuration section and the road map section to get an idea of what
  is possible.

## Configuration

Apply the plugin ID "com.github.bjornvester.xjc" as documented in the [Gradle Plugin portal page](https://plugins.gradle.org/plugin/com.github.bjornvester.xjc),
e.g. like this:

```kotlin
plugins {
    id("com.github.bjornvester.xjc") version "1.8.1"
}
```

You can configure the plugin using the "xjc" extension like this:

```kotlin
xjc {
    // Set properties here...
}
``` 

Here is a list of all available properties:

| Property                   | Type                       | Default                                                | Description                                                                                                                                                                                                   |
|----------------------------|----------------------------|--------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| xsdDir                     | DirectoryProperty          | "$projectDir/src<br>/main/resources"                   | The directory holding the xsd files to compile.                                                                                                                                                               |
| includes                   | ListProperty\<String>      | \[empty\]                                              | An optional include pattern for the files in the xsdDir property                                                                                                                                              |
| excludes                   | ListProperty\<String>      | \[empty\]                                              | An optional exclude pattern for the files in the xsdDir property                                                                                                                                              |
| bindingFiles               | ConfigurableFileCollection | xsdDir.asFileTree.matching<br> { include("**/*.xjb") } | The binding files to use in the schema compiler                                                                                                                                                               |
| outputJavaDir              | DirectoryProperty          | "$buildDir/generated<br>/sources/xjc/java"             | The output directory for the generated Java sources.<br>Note that it will be deleted when running XJC.                                                                                                        |
| outputResourcesDir         | DirectoryProperty          | "$buildDir/generated<br>/sources/xjc/resources"        | The output directory for the generated resources (if any).<br>Note that it will be deleted when running XJC.                                                                                                  |
| useJakarta                 | Provider\<Boolean>         | true                                                   | Set to use the `jakarta` namespace. If false, uses the `javax` namespace. This value determines the default version of XJC and the JAXB binding provider.                                                     |
| xjcVersion                 | Provider\<String>          | "3.0.2" for jakarta /<br> "2.3.8" for javax            | The version of XJC to use.                                                                                                                                                                                    |
| defaultPackage             | Provider\<String>          | \[not set\]                                            | The default package for the generated Java classes.<br>If empty, XJC will infer it from the namespace.                                                                                                        |
| generateEpisode            | Provider\<Boolean>         | false                                                  | If true, generates an Episode file for the generated Java classes.                                                                                                                                            |
| markGenerated              | Provider\<Boolean>         | false                                                  | If true, marks the generated code with the annotation `@javax.annotation.Generated`.                                                                                                                          |
| options                    | ListProperty\<String>      | \[empty\]                                              | Options to pass to either the XJC core, or to third party plugins in the `xjcPlugins` configuration                                                                                                           |
| groups                     | NamedDomainObjectContainer | \[empty\]                                              | Allows you to group a set of XSDs and generate sources with different configurations. Requires Gradle 7.0 or higher. See below for details.                                                                   |
| addCompilationDependencies | Provider\<Boolean>         | true                                                   | Adds dependencies to the `implementation` configuration for compiling the generated sources. These includes `jakarta.xml.bind:jakarta.xml.bind-api` and possibly `jakarta.annotation:jakarta.annotation-api`. |

### Choosing which schemas to generate source code for

By default, it will compile all XML schemas (xsd files) found in the src/main/resource folder.
You can change to another folder through the following configuration:

```kotlin
xjc {
    xsdDir.set(layout.projectDirectory.dir("src/main/xsd"))
}
```

All referenced resources must be located under this directory, or up-to-date checking might not work properly.

If you don't want to compile all schemas in the folder, you can specify which ones through the `includes` and `excludes` properties.
These are both Ant patterns.
Here is an example where all schema files are referenced relative to the xsdDir property:

```kotlin
xjc {
    xsdFiles = project.files(
        xsdDir.file("MySchema1.xsd"),
        xsdDir.file("MySchema2.xsd")
    )
    // Or
    xsdFiles = xsdDir.asFileTree.matching { include("subfolder/**/*.xsd") }
}
```

### Customizing the build dependencies

By default, it will use XJC version 3.0.2 to compile the schemas.
You can set another version through the xjcVersion property like this:

```kotlin
xjc {
    xjcVersion.set("2.3.8")
    useJakarta.set(false)
}
```

If you specify a version in the 2.x range, which generates source code with the javax namespace, you should also set the `useJakarta` configuration to false.
Note that setting `useJakarta` to false, it will by default select an appropriate version in the 2.x range.

By applying the plugin, it will register the Java plugin as well if it isn't there already (so the generated source code can be compiled).
It will also by default add the dependency `jakarta.xml.bind:jakarta.xml.bind-api` to your implementation configuration, as this is needed to compile the source
code.
If your project is going to be deployed on a Java/Jakarta EE application server, you may want to exclude this dependency from your runtime and instead use
whatever your application server is providing.
This can be done by setting the configuration `addCompilationDependencies` to false.

### Choosing the file encoding

If your schemas contain characters that do not match your default platform encoding (on western versions of Windows, this will probably be CP-1252),
set the encoding through the file.encoding property for Gradle.
For example, to use UTF-8, put this in your gradle.property file:

```properties
org.gradle.jvmargs=-Dfile.encoding=UTF-8
```

If you are on a POSIX operating system (e.g. Linux), you may in addition to this need to set your operating system locale to one that supports your encoding.
Otherwise, Java (and therefore also Gradle and XJC) may not be able to create files with names outside of what your default locale supports.
Especially some Docker images, like the Java ECR images from AWS, are by default set to a locale supporting ASCII only.
If this is the case for you, and you want to use UTF-8, you could export an environment variable like this:

```shell script
export LANG=C.UTF-8
```

### Enabling the use of the @Generated annotation

If you like to have the generated source code marked with the `@javax.annotation.Generated` annotation, set the `markGenerated` property to true like this:

```kotlin
xjc {
    markGenerated.set(true)
}
``` 

Note that while this annotation is found in the Java 8 SDK, it is not present in Java 9 and later.
(However, there is a `@javax.annotation.processing.Generated` annotation, notice the `processing` sub-package, but this is not yet supported by this plugin.)

### Generating episode files

XJC can generate an episode file, which is basically an extended binding file that specifies how the schema types are associated with the generated Java
classes.

You can enable the generation using the generateEpisode property like this:

```kotlin
xjc {
    generateEpisode.set(true)
}
```

The file will be generated at META-INF/sun-jaxb.episode and added as a resource to the main source set.

### Consuming episode files

XJC can consume the episode files so that it is possible to compile java classes from a schema in one project, and consume it in XJC generators in other
projects, so you don't have to compile the same schemas multiple times.
To do this, you need to add the jar file to the configuration named "xjcBindings".
This is done using normal Gradle dependency management.

For multi-projects, assuming the episode file is generated in a project called "test-producer", you can do this like this:

```kotlin
dependencies {
    implementation(project(":test-producer"))
    xjcBindings(project(":test-producer"))
}
```

### Consuming binding files

You can also provide your own binding files (or custom episode files) through the bindingFiles property:

```kotlin
xjc {
    bindingFiles.setFrom(layout.projectDirectory.dir("src/main/xjb").asFileTree.matching { include("**/*.xjb") }) // Files with an .xjb extension in the "src/main/xjb" directory
}
```

### Activating (third party) XJC plugins

To use third party plugins, supply the relevant dependencies to the `xjcPlugins` configuration.
Then set the plugin options through the `options` property.

For example, to use the "Copyable" plugin from the [JAXB2 Basics](https://github.com/highsource/jaxb2-basics) project, configure the following:

```kotlin
dependencies {
    xjcPlugins("org.jvnet.jaxb2_commons:jaxb2-basics:0.13.1")
}

xjc {
    options.add("-Xcopyable")
}
```

Note that the above plugin is only compatible with JAXB 2.x, at least at the time of this writing.
There is a fork [here](https://github.com/patrodyne/hisrc-basicjaxb) that you may try for JAXB 3.x and the `jakarta` namespace.

If you have trouble activating a plugin and is unsure whether it has been registered, you can run Gradle with the --debug option.
This will print additional information on what plugins were found, what their option names are, and what plugins were activated.
Note that in order to activate a third-party plugin, you must always provide at least one option (and usually just one) from the plugin.

### Supporting Date/Time APIs introduced in Java 8

By default, XJC will map date and time types to difficult-to-use Java types like XMLGregorianCalendar.
If you like to use the newer Data/Time APIs from package java.time, you must use a mapper and write a custom binding file.

An example of a mapper project is [threeten-jaxb](https://github.com/threeten-jaxb/threeten-jaxb), but there are others as well.
If you like to use this one, include it as a dependency:

```kotlin
dependencies {
    implementation("io.github.threeten-jaxb:threeten-jaxb-core:2.2.0") // This version is for Jakarta
}
```

Then create a binding file with content with the types you like to map:

```xml

<bindings xmlns="https://jakarta.ee/xml/ns/jaxb"
          xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"
          version="3.0"
          extensionBindingPrefixes="xjc">
<globalBindings>
    <xjc:javaType name="java.time.LocalDate" xmlType="xs:date" adapter="io.github.threetenjaxb.core.LocalDateXmlAdapter"/>
    <xjc:javaType name="java.time.LocalDateTime" xmlType="xs:dateTime" adapter="io.github.threetenjaxb.core.LocalDateTimeXmlAdapter"/>
    <xjc:javaType name="java.time.YearMonth" xmlType="xs:gYearMonth" adapter="io.github.threetenjaxb.core.YearMonthXmlAdapter"/>
    <xjc:javaType name="java.time.Duration" xmlType="xs:duration" adapter="io.github.threetenjaxb.core.DurationXmlAdapter"/>
    <xjc:javaType name="java.time.OffsetTime" xmlType="xs:date" adapter="io.github.threetenjaxb.core.OffsetTimeXmlAdapter"/>
    <xjc:javaType name="java.time.OffsetDateTime" xmlType="xs:dateTime" adapter="io.github.threetenjaxb.core.OffsetDateTimeXmlAdapter"/>
</globalBindings>
</bindings>
```

The above configuration is for `jakarta`.
For `javax`, use version 1.2.0 and the binding attributes to `xmlns="http://java.sun.com/xml/ns/jaxb" version="2.1"`.

Lastly, for both `jakarta` and `javax`, configure the plugin to use the binding file (in this case it is called `src/main/xjb/bindings.xml`):

```kotlin
xjc {
    bindingFiles.setFrom(layout.projectDirectory.dir("src/main/xjb").asFileTree.matching { include("**/*.xjb") })
}
```

Note that some use `.xml` as the extension for the binding files, and some use `.xjb`.
This makes no difference to XJC and you can choose whatever you like.

## Generate resources with different configurations

_The grouping functionality described here requires Gradle 7.0 or higher_

If you require building a subset of XSDs with different configurations (e.g. package names), you can use `group` property.
Each group has the same configuration properties as otherwise, except for the XJC version as this can't be controlled individually.
The configurations in the outermost block are considered defaults, and you can then override them in each group.

Example:

```kotlin
xjc {
    // Defaults
    markGenerated.set(true)

    groups {
        register("group1") {
            includes.set(listOf("MySchemaWithFunnyChar.xsd"))
            defaultPackage.set("com.example.group1")
        }
        register("group2") {
            includes.set(listOf("MySchemaWithFunnyChar.xsd"))
            defaultPackage.set("com.example.group2")
        }
    }
}
```

Note that if you enable episode generation, XJC will generate multiple files (one for each group) but with the same name.
You will have to merge these yourself.
You will also not be able to reference between groups.
If you require this, you should instead separate the build into individual projects.

## Road map

Here are some of the features I like to implement at some point.

* Support the "-npa" option in XJC, suppressing the generation of package level annotations.
* Support for catalog files.
* Support for schemas in wsdl files.
* Support for optionally adding "if-exists" attributes to generated episode files to prevent failures on unreferenced schemas.
* Document how to register multiple XJC tasks yourself in the same project (for instance for testing only)

You are welcome to create issues and PRs for anything else.

## Alternatives

If you need additional functionality than what is provided here, you may want to try the one from [unbroken-dome](https://github.com/unbroken-dome/gradle-xjc-plugin).
Just be aware that, at least at the time of this writing, the latest version is 2.0.0, which has some known issues with the jakarta dependencies and the Gradle configuration cache.
