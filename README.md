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
2. Supported arguments types: `int`, `float`, `string`, `boolean`, `mixed`, `array`, `null`
3. Optional arguments
4. Extension constants
5. Declare and read INI-entries as String
6. Call Zend C-functions from Kotlin
7. Register lifecycle hooks

Can't
1. Arguments by reference
2. Classes, resources and callable as arguments or return value

## Files

```
./konfigure/*       Code generator, DSL, zend interop classes
./tests/*           .phpt tests for example extension
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
    lifeCycleHooks(hooks...)
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
|`lifeCycleHooks`|||Define set of lifecycle hooks, where custom functions will be executed
||`hooks...`||`vararg` argument. List of `LifeCycle` enums

## Types
|ArgumentType|Kotlin type|PHP type|C type|note|
|---|---|---|---|---|
|`PHP_STRING`|`String`|`string`|`char*`||
|`PHP_LONG`|`Long`|`int`|`zend_long`/`int64_t`||
|`PHP_STRICT_LONG`|`Long`|`int`|`zend_long`/`int64_t`|See [description](https://phpinternals.net/docs/z_param_strict_long). Can't be used for return value|
|`PHP_DOUBLE`|`Double`|`float`/`double`|`double`||
|`PHP_BOOL`|`Boolean`|`boolean`|`int`||
|`PHP_NULL`|`PhpMixed`|`null`|`zval*`||
|`PHP_MIXED`|`PhpMixed`|`mixed`|`zval*`||
|`PHP_ARRAY`|`PhpArray`|`array`|`HashTable*`|`PhpArray` is a wrapper class realizing `Map<PhpMixed,PhpMixed>` interface|

## Hooks
You MUST provide corresponding Kotlin function for every enabled hook

|LifeCycle|Lifecycle function|Kotlin function|
|---|---|---|
|`MINIT`|`module_startup_func`|`minit()`|
|`MSHUTDOWN`|`module_shutdown_func`|`mshutdown()`|
|`RINIT`|`request_startup_func`|`rinit()`|
|`RSHUTDOWN`|`request_shutdown_func`|`rshutdown()`|

## Reference

### Classes
|Class|Realize|Description|
|---|---|---|
|`PhpMixed`|`CPointer<zval>`| Type alias for pointer to C-struct `zval`|
|`PhpArray`| `MutableMap<PhpMixed, PhpMixed>`| Wrapper for C-struct `HashTable`. Represents methods for PHP-array manipulations|
|`ArgumentType`|enum class|Represents possible PHP-types|
|`LifeCycle`|enum class|Represents possible PHP-extension lifecycle hook|

### High order functions
|Function|Returns|Description|
|---|---|---|
|`getIniString(name:String)`|`String`|Returns INI-setting for name. You can retrieve only those INI-settings that described by DSL directives `ini` and `externalIni`|
|`createPhpNull()`|`PhpMixed`|Returns `PhpMixed` with type NULL|
|`arrayToHashTable(array: PhpArray)`|`CPointer<HashTable>`|Convert `PhpArray` to pointer to C-struct `HashTable`. Normally you do not need to use this function.|
|`hashToArray(hash: CPointer<HashTable>)`|`PhpArray`|Convert `PhpArray` to pointer to C-struct `HashTable`. Normally you do not need to use this function.|

### Additional properties
|Property|Type|Description|
|---|---|---|
|`String.mixed`|`PhpMixed`|`PhpMixed` representation of `String`|
|`Long.mixed`|`PhpMixed`|`PhpMixed` representation of `Long`|
|`Double.mixed`|`PhpMixed`|`PhpMixed` representation of `Double`|
|`Boolean.mixed`|`PhpMixed`|`PhpMixed` representation of `Boolean`|
|`PhpArray.mixed`|`PhpMixed`|`PhpMixed` representation of `PhpArray`|
|`PhpMixed.string`|`String`|`String` value from `PhpMixed`|
|`PhpMixed.long`|`Long`|`Long` value from `PhpMixed`|
|`PhpMixed.double`|`Double`|`Double` value from `PhpMixed`|
|`PhpMixed.bool`|`Boolean`|`Boolean` value from `PhpMixed`|
|`PhpMixed.array`|`PhpArray`|`PhpArray` value from `PhpMixed`|
|`PhpMixed.type`|`ArgumentType`|Corresponding `ArgumentType` for `PhpMixed`|

### Class `PhpArray`
`PhpArray` implements interface `MutableMap<PhpMixed, PhpMixed>`. Thus, it contains all the required properties and methods.

#### Non-standard properties and methods

|Property|Type|Description|
|---|---|---|
|`hash`|`CPointer<HashTable>`|Pointer to wrapped C-struct `HashTable`|
|`mixed`|`PhpMixed`|`PhpMixed` representation of `PhpArray`|
|`stringKeys`|`Set<String>`|Set of keys converted into `String`. Actually in PHP numeric keys is equals to they string representation. Thus, `11` is equals to `"11"`|

|Constructor|Description|
|---|---|
|`PhpArray(hash: CPointer<HashTable>)`|Default constructor|
|`PhpArray.fromMixed(array:PhpMixed)`|Construct `PhpArray` from `PhpMixed`|
|`PhpArray.createEmpty(size:Int = 8)`|Construct empty `PhpArray` and initialize `hash`|

|Method|Returns|Description|
|---|---|---|
|`put(key: String, value: PhpMixed)`|`PhpMixed?`|Returns old value for `key` or `null`|
|`put(key: Long, value: PhpMixed)`|`PhpMixed?`|Returns old value for `key` or `null`|
|`put(value: PhpMixed)`|`Unit`|Add element to array with next free numeric index|
|`putAll(from: Map<Long, PhpMixed>)`|`Unit`|Put all elements with numeric indexes|
|`putAll(from: Map<String, PhpMixed>)`|`Unit`|Put all elements with string keys|
|`putAll(from: List<PhpMixed>)`|`Unit`|Sequentially add values by calling `put(value: PhpMixed)`|
|`remove(key: String)`|`PhpMixed?`|Remove element and returns old value for `key` or `null`|
|`remove(key: Long)`|`PhpMixed?`|Remove element and returns old value for `key` or `null`|
|`containsKey(key: String)`|`Boolean`|Whether array contains element with this `key`|
|`containsKey(key: Long)`|`Boolean`|Whether array contains element with this `key`|
|`get(key: String)`|`PhpMixed?`|Returns corresponding value or `null`|
|`get(key: Long)`|`PhpMixed?`|Returns corresponding value or `null`|
