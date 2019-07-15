# xjc-gradle-plugin
A Gradle plugin for running the XJC binding compiler to generate JAXB Java source code from XSD schemas.

## Requirements and features
The plugin requires Gradle version 5.4 or later.

It has been tested with Java 8 and Java 11.

It supports the Gradle build cache (enabled by setting "org.gradle.caching=true" in your gradle.properties file; this should become the default in a later version of Gradle).

It supports parallel execution (enabled with "org.gradle.caching=true", possibly along with "org.gradle.priority=low", in your gradle.properties file).

## Configuration
Apply the plugin ID "com.github.bjornvester.xjc" as documented in the [Gradle Plugin portal page](https://plugins.gradle.org/plugin/com.github.bjornvester.xjc), e.g. like this (for the Groovy DSL):

```
plugins {
  id "com.github.bjornvester.xjc" version "1.0"
}
```

Note that at this time, the plugin is very limited and you cannot configure too much yet.
The priority until now has been to construct a plugin out that can compile the schemas in a way that supports the Gradle build cache, up-to-date checking, project relocation (e.g. if checking out code in different folders corresponding to a branch or PR name on a build server) etc.
The plan is to make the plugin more flexible in terms of configuration in the near future.

Here is what you can currently do.

### Choosing which schemas to generate source code for
By default, it will compile all XML schemas (xsd files) found in the src/main/resource folder.
You can change this folder through the following configuration:

```
xjc {
    xsdDir.set(project.layout.projectDirectory.dir("src/main/xsd"))
}
```

If you don't want to compile all schemas in the folder, you can specify which ones through the xsdFiles property.
Note that they must still be located under the directory specified by xsdDir, or up-to-date checking might not work properly.

### Customizing the build dependencies
By default, it will use XJC version 2.3.2 to compile the schemas. You can set another version through the xjcVersion property.
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

## Generated warnings
When building on INFO level or lower, you will likely see exceptions thrown on the form:

```
Property "http://javax.xml.XMLConstants/property/accessExternalSchema" is not supported by used JAXP implementation.
org.xml.sax.SAXNotRecognizedException: Property 'http://javax.xml.XMLConstants/property/accessExternalSchema' is not recognized.
```

These are caused by the bundled Xerces parser, which does not support these properties.
They can be ignored.
If anyone know how to suppress them, please let me know.

## Alternatives
If you need to be able to configure the schema compiler in more ways that is currently possible by this plugin, you may want to try the one from [rackerlabs](https://github.com/rackerlabs/gradle-jaxb-plugin).
Here you will be able to pass arguments directly to the underlying XJC task as well as a few other nice things. Just be aware that the "Gradle plumbing" of that plugin is a bit leaky, and at least I have had a lot of issues with up-to-date checking, caching and more.

There are also a few other plugins for XJC out there, but they also seem to completely ignore the caching aspect.
(This is also why I wrote this plugin in the first place.)
