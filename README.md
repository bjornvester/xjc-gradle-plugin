# xjc-gradle-plugin
A Gradle plugin for running the XJC binding compiler to generate Java source code from XML schemas (xsd files) using JAXB.

## Requirements and features
The plugin requires Gradle version 5.4 or later.

It has been tested with Java 8 and Java 11.

It supports the Gradle build cache (enabled by setting "org.gradle.caching=true" in your gradle.properties file).

It supports project relocation for the build cache (e.g. you move your project to a new path, or make a new copy/clone of it).
This is especially useful in a CI context, where you might clone PRs and/or branches for a repository in their own locations. 

It supports parallel execution (enabled with "org.gradle.parallel=true", possibly along with "org.gradle.priority=low", in your gradle.properties file).

## Configuration
Apply the plugin ID "com.github.bjornvester.xjc" as documented in the [Gradle Plugin portal page](https://plugins.gradle.org/plugin/com.github.bjornvester.xjc), e.g. like this (for the Groovy DSL):

```
plugins {
  id "com.github.bjornvester.xjc" version "1.1"
}
```

You can configure the plugin using the "xjc" extension like this:

```
xjc {
    // Set properties here...
}
``` 

Here is a list of all available properties:

| Property               | Type           | Default                                                      | Description                                                                                         |
|------------------------|----------------|--------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| xsdDir                 | Directory      | layout.projectDirectory.dir("src/main/resources")            | The directory holding the xsd files to compile.                                                     |
| xsdFiles               | FileCollection | \[empty\]                                                    | The schemas to compile. If empty, all files in the xsdDir will be compiled.                         |
| outputJavaDir          | Directory      | layout.buildDirectory.dir("generated/sources/xjc/java")      | The output directory for the generated Java sources.                                                |
| outputResourcesDir     | Directory      | layout.buildDirectory.dir("generated/sources/xjc/resources") | The output directory for the generated resources (if any).                                          |
| xjcVersion             | String         | 2.3.2                                                        | The version of XJC to use.                                                                          |
| defaultPackage         | String         | \[not set\]                                                  | The default package for the generated Java classes. If empty, XJC will infer it from the namespace. |
| generateEpisode        | Boolean        | false                                                        | Whether to generate an Episode file for the generated Java classes.                                 |
| bindingFiles           | FileCollection | \[empty\]                                                    | The binding files to use in the schema compiler                                                     |

### Choosing which schemas to generate source code for
By default, it will compile all XML schemas (xsd files) found in the src/main/resource folder.
You can change to another folder through the following configuration:

```
xjc {
    xsdDir.set(layout.projectDirectory.dir("src/main/xsd"))
}
```

If you don't want to compile all schemas in the folder, you can specify which ones through the xsdFiles property.
Note that they must still be located under the directory specified by xsdDir, or up-to-date checking might not work properly.

### Customizing the build dependencies
By default, it will use XJC version 2.3.2 to compile the schemas. You can set another version through the xjcVersion property like this:

```
xjc {
    xjcVersion.set("2.3.2")
}
```

As it uses the Jakarta version of the tool with new Maven coordinates, the older versions from Oracle are not supported.
You can check if there is a newer version of the tool either on the official [Github repository](https://github.com/eclipse-ee4j/jaxb-ri/releases)
or by searching for the group and name "org.glassfish.jaxb:jaxb-xjc", e.g. through [MvnRepository](https://mvnrepository.com/artifact/org.glassfish.jaxb/jaxb-xjc).

By applying the plugin, it will register the Java plugin as well if it isn't there already (so the generated source code can be compiled).
It will also add the dependency "jakarta.xml.bind:jakarta.xml.bind-api" to your implementation configuration, as this is needed to compile the source code.
If your project is going to be deployed on a Java/Jakarta EE application server, you may want to exclude this dependency from your runtime and instead use whatever your application server is providing.

### Choosing the file encoding
If your schemas contain characters that do not match your default platform encoding (on Windows this will probably be CP-1252),
set the encoding through the file.encoding property for Gradle. For example, to use UTF-8, put this in your gradle.property file:

```
org.gradle.jvmargs=-Dfile.encoding=UTF-8
```    

### Generating episode files
XJC can generate an episode file, which is basically an extended binding file that specifies how the the schema types are associated with the generated Java classes.

You can enable the generation using the generateEpisode property like this:

```
xjc {
    generateEpisode.set(true)
}
```

The file will be generated at META-INF/sun-jaxb.episode and added as a resource to the main source set.

### Consuming episode files
XJC can consume the episode files so that it is possible to compile java classes from a schema in one project, and consume it in XJC generators in other projects so you don't have to compile the same schemas multiple times.
To do this, you need to add the jar file to the configuration named "xjcBind".

For multi-projects, assuming the episode file is generated in a project called "test-producer", you can do this like this:

```
dependencies {
    implementation(project(":test-producer"))
    xjcBind(project(":test-producer", "apiElements"))
}
```

### Consuming binding files
You can also provide your own binding files (or custom episode files). To to this,  

## Alternatives
If you need to be able to configure the schema compiler in more ways that is currently possible by this plugin, you may want to try the one from [rackerlabs](https://github.com/rackerlabs/gradle-jaxb-plugin).
Here you will be able to pass arguments directly to the underlying XJC task as well as a few other nice things. Just be aware that the "Gradle plumbing" of that plugin is a bit leaky, and at least I have had a lot of issues with up-to-date checking, caching and more.

There are also a few other plugins for XJC out there, but they also seem to completely ignore the caching aspect.
(This is also why I wrote this plugin in the first place.)
