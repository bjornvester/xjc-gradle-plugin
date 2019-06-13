# xjc-gradle-plugin
A Gradle plugin for running the XJC binding compiler to generate JAXB Java source code from XSD schemas.

## Requirements
The plugin requires Gradle version 5.4 or later.

It supports the Gradle build cache.

It has been tested with Java 8 and Java 11.

## Configuration
Apply the plugin ID "com.github.bjornvester.xjc" as specific in the [Gradle Plugin portal page](https://plugins.gradle.org/plugin/com.github.bjornvester.xjc), e.g. like this:

```
plugins {
    id("com.github.bjornvester.wsdl2java")
}
```

By default, it will compile all XSD schemas found in the src/main/resource folder.
You can change this folder through the following configuration:

```
xjc {
    xsdDir.set(project.layout.projectDirectory.dir("src/main/xsd"))
}
```

If you don't want to compile all schemas in the folder, you can specify which ones through the xsdFiles property.
Note that they must be located under the directory specified by xsdDir, or up-to-date checking will not work properly.

By default, it will use XJC version 2.3.2 to compile the schemas. You can set another version through the xjcVersion property.

Lastly, if your schemas contain characters that do not match your default platform encoding (on Windows this will be windows-1252),
set the encoding through the file.encoding property for Gradle. For example, to use UTF-8, put this in your gradle.property file:

```
org.gradle.jvmargs=-Dfile.encoding=UTF-8
```    

Note that at this time, the plugin is very limited and you cannot configure much more.
This will be fixed at a later point.
