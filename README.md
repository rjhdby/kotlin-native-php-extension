# Kotlin Native PHP extension generator

PHP version >= 7.0

Linux

## Scope

Can
1. Create functions
2. `long(php int)`, `double(php float)`, `string` and `null` arguments and return value
3. Extension constants

Can't
1. Arguments by reference
2. Arrays, classes, boolean, resources, mixed, callable as arguments or return value

## Files

```
./generator/*       Codegenerator
./tests/*           .phpt tests for example extension
./konfigure.sh      Shell script for build extension.
konfigure.kt        Write your DSL of PHP extension here
example_double.kt   Example extension functions
example_strings.kt  Example extension functions
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

Extension consist of two or mode `.kt` files.

Mandatory one is `konfigure.kt`, where you describe your extension with DSL.

All others contains your extension logic.

Let's make `example` extension with three functions.

#### First, make file `example.kt`, that contains realization of those functions.

```kotlin
fun hello(name: String) = "Hello $name!!!\n"

fun helloWorld() = println("Hello World!!!")

fun multiple2(num: Double) = num * 2
```

First function receive `String` and return `String`.

Second function receive `String` and return nothing (`NULL` by default).

Third function receive `Double` and return `Double`.

Also let's add string constant `EXAMPLE_WORLD="World"`

#### Second, write DSL with description of extension.

```kotlin
import php.extension.dsl.*

fun main(args: Array<String>) {
    extension("example", "0.1") {
        constant("EXAMPLE_WORLD", "World")
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

> NOTE! Your `.kt` files MUST contains functions with names equals to described by DSL.

> NOTE! You can't access created constants from Kotlin code

#### Third, just run `./konfigure.sh`.

If no errors occuried then `example.so` will be created inside `./modules` directory.

If you does not want run `phpize`, `configure` and `make` after generation of C-code, just use parameter `./konfigure.sh kt`.

## DSL description

#### extension(name:String, version:String) { functions and constants }

#### constant(name:String, value:Any)
value must be `Long`, `Double` or `String`. All other types will be silently dropped.

#### function(name:String) {agruments and return value}
By default return type is `ArgumentType.NULL`

You may change return type by assignment `returnType = ArgumentType.STRING`

#### arg(type:ArgumentType, name:String) { }

## Currently supported argument types
```kotlin
ArgumentType.STRING;
enum class ArgumentType(val code: String) {
    ("s"), LONG("l"), DOUBLE("d"), NULL("")
}
```
