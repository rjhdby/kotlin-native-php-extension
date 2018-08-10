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
./zend_interop/*    Zend macro interop classes
Makefile            Makefile
extension.kt        Write your DSL of PHP extension here
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

Mandatory one is `extension.kt`, where you describe your extension with DSL.

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

Also let's add som constants and INI-setting.

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

    function("hello", ArgumentType.PHP_STRING) {
        arg(ArgumentType.PHP_STRING, "name")
        arg(ArgumentType.PHP_STRING, "lang", true)
    }

    function("helloWorld")

    function("multiple2", ArgumentType.PHP_DOUBLE) {
        arg(ArgumentType.PHP_DOUBLE, "number")
    }
}

fun main(args: Array<String>) = dsl.make()
```

> NOTE! Your `.kt` files MUST contains functions with names equals to described by DSL. Also they MUST receive all described arguments of corresponding types in described order.

> NOTE! Corresponding K/N constants will generated automatically. Do not declare them in `.kt` files.

#### Third, just run `make`.

If no errors occuried then `example.so` will be created inside `./modules` directory.

If you does not want run `phpize`, `configure` and `make` after generation of C-code, just use parameter `make kotlin`.

Don't forget to run `make test`.

## DSL description

```
extension(name, version){
    ini(name, defaultValue)
    ...
    externalIni(name)
    ...
    constant(name, value)
    ...
    function(name, returnType){
        arg(type, name [, isOptional = false])
    }
    ...
}
```

|node|parameter|type|note|
|---|---|---|---|
|`extension`|
||`name`|`String`|
||`version`|`String`|
|`ini`|||Extension INI-setting
||`name`|`String`|
||`value`|`String`|
|`externalIni`|||INI-setting that do not related to your extension, but you need it to use inside them. This needed for creation proxy retriever function.
||`name`|`String`|
|`constant`|
||`name`|`String`|
||`value`|`Any`|String, Long (or Int), Double and Boolean will be converted to corresponding PHP types. All other will be silently dropped|
|`function`|
||`name`|`String`|Name of resulting PHP-function. Note that you MUST provide corresponding K/N function with exact same name|
||`returnType`|`ArgumentType`|Optional return type. By default `ArgumentType.PHP_NULL`
|`arg`|||Note that argument addition order is important. After adding first optional argument, all arguments below MUST be optional
||`type`|`ArgumentType`|
||`name`|`String`|
||`isOptional`|`Boolean`|Optional flag decides that argument is optional. By default `FALSE`

## enum class ArgumentType
|ArgumentType|PHP-type|note|
|---|---|---|
|`PHP_STRING`|`string`||
|`PHP_LONG`|`int`||
|`PHP_STRICT_LONG`|`int`|See [description](https://phpinternals.net/docs/z_param_strict_long). Can't be used for return value|
|`PHP_DOUBLE`|`float`/`double`||
|`PHP_BOOL`|`boolean`||
|`PHP_NULL`|`null`||

## Zend Api macro interop
Proxy functions for zend api macro located in package `zend.api.proxy`

Following functions are currently supported

|macro|proxy function|note|
|:---|:---|:---|
|**INI_STR&nbsp;(name)**|**getIniString&nbsp;(name:&nbsp;String):&nbsp;String**|You can retrieve only those INI-settings that described by DSL directives `ini` and `externalIni`|
