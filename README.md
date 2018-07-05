# Kotlin Native PHP extension generator

PHP version >= 7.0

Linux

## Scope
Can
1. Create functions
2. `long(php int)`, `double(php float)`, `string` and `null` arguments and return value

Can't
1. Arguments by reference
2. Arrays, classes, boolean, resources, mixed, callable as arguments or return value
3. Extension constants
## Files

Core files
```
dsl.kt         extension DSL
generator.kr   C source and config.m4 generator
konfigure.def  reserved for future use
konfigure.sh   Shell script for build extension.
```
Work files
```
konfigure.kt   Write your DSL of PHP extension here
example.kt     Extension functions
./tests/*      .phpt tests for example extension
```

## Prerequisites

1. [Install KOTLINC compiler](https://kotlinlang.org/docs/tutorials/native/basic-kotlin-native-app.html)
2. Install `php` and `php-devel` packages
3. Change `PHP_HOME` and `KOTLIN_HOME` variables in `konfigure.sh`

   `PHP_HOME` must point at home PHP directory, where located `./bin` and `./include` directories
   `KOTLIN_HOME` must point at directory, where located `kotlinc` binary

   example:
   ```sh
   #!/bin/sh
   KOTLIN_HOME=/root/kotlin-native-linux-0.7.1/bin
   PHP_HOME=/opt/rh/rh-php71/root/usr
   ```
4. Mark `konfigure.kt` executable

   ```sh
   chmod ug+x ./konfigure.kt
   ```

## Writing extension

Extension consist of two files.

First one is `konfigure.kt`, where you describe your extension with DSL.
Second one is `extension_name.kt`, where you write functions of your extension.

> NOTE! `extension_name` must be equals to name of your extension described in `konfigure.kt`.


Let's make `example` extension with three functions.

First, make file `example.kt`, that contains realization of those functions.

```kotlin
fun hello(name: String) = "Hello $name!!!\n"

fun helloWorld() = println("Hello World!!!")

fun multiple2(num: Double) = num * 2
```

First function receive `String` and return `String`.

Second function receive `String` and return nothing (`NULL` by default).

And third function receive `Double` and return `Double`.


Second, write DSL with description of extension.

```kotlin
import php.extension.dsl.*

fun main(args: Array<String>) {
    extension("example", "0.1") {
        function("hello") {
            arg(ArgumentType.STRING, "name")
            returnType = ArgumentType.STRING
        }
        function("helloWorld")
        function("multiple2") {
            arg(ArgumentType.DOUBLE, "number")
            returnType = ArgumentType.DOUBLE
        }
    }.make()
}
```

Third, just run `./konfigure.sh`.

If no errors occuried then `example.so` will be created inside `./modules` directory.

If you does not want run `phpize`, `configure` and `make` after generation of C-code, just use parameter `./konfigure.sh kt`.

## DSL description

```
extension(extension_name:String, extension_version:String) { functions }

function(function_name:String) {arguments and return value}

arg(argument_type:ArgumentType, argument_name:String) { reserved for future use }
``` 