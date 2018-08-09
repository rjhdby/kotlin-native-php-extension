# Kotlin Native PHP extension generator

PHP version >= 7.0

Linux

## How it works
You describe PHP extension by provided DSL and run `make`.

Build script do:
1. Compile and run code-generator utility
2. Generate template `.c` file for PHP extension
3. Generate `config.m4` file for `autoconf`
4. Bind Kotlin function to php functions calls
5. Generate `.kt` file with declared constants
6. Compile static library from `.kt` sources
7. Run `phpize`, `configure` and `make` to produce shared PHP library from generated `.c` file and the resultant Kotlin static library

## Scope

Can
1. Functions
2. Supported arguments types: `long(php int)`, `double(php float)`, `string`, `boolean` (and `null` for return value)
2. Optional arguments
3. Extension constants
3. Declare and read INI-entries as String

Can't
1. Arguments by reference
2. Arrays, classes, resources, mixed, callable as arguments or return value
3. Call PHP API from Kotlin

## Files

```
./generator/*       Code generator
./tests/*           .phpt tests for example extension
Makefile            Makefile
konfigure.kt        Write your DSL of PHP extension here
example_double.kt   Example extension functions
example_strings.kt  Example extension functions
```

## Prerequisites

1. [Install KOTLINC compiler](https://kotlinlang.org/docs/tutorials/native/basic-kotlin-native-app.html)
2. Install `php` and `php-devel` packages
3. Change `PHP_HOME` and `KOTLIN_HOME` variables in `Makefile`

   `PHP_HOME` must point at home PHP directory, where located `./bin` and `./include` directories

   `KOTLIN_HOME` must point at directory, where located `kotlinc` binary

   example:
   ```sh
   KOTLIN_HOME=/root/kotlin-native-linux-0.7.1/bin
   PHP_HOME=/opt/rh/rh-php71/root/usr
   ```

## Writing extension

Extension consist of two or mode `.kt` files.

Mandatory one is `konfigure.kt`, where you describe your extension with DSL.

All others contains your extension logic.

Let's make `example` extension with three functions.

#### First, make file `example.kt`, that contains realization of those functions.

```kotlin
fun hello(name: String, lang: String?) = "${if (lang ?: "" == "") HELLO_EN else lang} $name!!!\n"

fun helloWorld() = println("Hello $EXAMPLE_WORLD!!!")

fun multiple2(num: Double) = num * 2
```

> NOTE! In current version of KN have a bug with unitialized `char *`. Because of this, you must handle an empty string instead of null.

First function receive `String` and return `String`.

Second function receive `String` and return nothing (`NULL` by default).

Third function receive `Double` and return `Double`.

Also let's add string constant `EXAMPLE_WORLD="World"`

#### Second, write DSL with description of extension.

```kotlin
import php.extension.dsl.*

val dsl = extension("example", "0.1") {
    ini("example.count","10")

    constant("EXAMPLE_WORLD", "World")
    constant("EXAMPLE_LONG", 10L)

    constant("HELLO_EN", "Hello")
    constant("HELLO_ES", "Hola")
    constant("HELLO_RU", "Привет")

    function("hello", ArgumentType.STRING) {
        arg(ArgumentType.STRING, "name")
        arg(ArgumentType.STRING, "lang", true)
    }

    function("helloWorld")

    function("multiple2", ArgumentType.DOUBLE) {
        arg(ArgumentType.DOUBLE, "number")
    }
}

fun main(args: Array<String>) = dsl.make()
```

> NOTE! Your `.kt` files MUST contains functions with names equals to described by DSL. Also they MUST receive all described arguments of corresponding types in described order.

> NOTE! Kotlin constants will be generated automatically. Do not declare them in `.kt` files.

> NOTE! After definition of optional arguments, all remains arguments also must be optional

#### Third, just run `make`.

If no errors occuried then `example.so` will be created inside `./modules` directory.

If you does not want run `phpize`, `configure` and `make` after generation of C-code, just use parameter `./konfigure.sh kt`.

Don't forget to run `make test`.

## DSL description

#### extension(name:String, version:String) { functions and constants }

#### ini(name:String, value:String)

#### constant(name:String, value:Any)
value must be `Long`, `Double` or `String`. All other types will be silently dropped.

#### function(name:String, returnType:ArgumentType = ArgumentType.NULL) { agruments }

#### arg(type:ArgumentType, name:String, optional:Boolean = false)

## Zend Api interop
Mapping functions for internal zend api located in package `zend.api.mapper`

Following functions are currently supported:
- `getIniString(name: String): String`

## Currently supported argument types
```kotlin
ArgumentType.STRING;
enum class ArgumentType(val code: String) {
    ("s"), LONG("l"), DOUBLE("d"), BOOL("b"), NULL("")
}
```
