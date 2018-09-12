import php.*
import php.extension.proxy.*
import php.extension.share.*

fun multiple2(num: Double) = num * 2

fun multiple2long(num: Long) = num * 2

fun hello(name: String = EXAMPLE_WORLD, lang: String = HELLO_EN) = "$lang $name!!!\n"

fun helloWorld(): Boolean {
    php_printf("Hello %s!!!\n", EXAMPLE_WORLD)
    return true;
}

fun helloOrNotHello(hello: Boolean) = println(if (hello) "Hello!" else "Nop!")

fun iniValueFor(name: String) = getIniString(name)

fun printMixedType(value: PhpMixed) = print(value.type)

fun printMixed(values: PhpArray) = values.values.map {
    when (it.type) {
        ArgumentType.PHP_LONG   -> it.asString()
        ArgumentType.PHP_DOUBLE -> it.asString()
        ArgumentType.PHP_STRING -> it.asString()
        ArgumentType.PHP_BOOL   -> it.asString()
        ArgumentType.PHP_ARRAY  -> it.asString()
        ArgumentType.PHP_NULL   -> "null"
        else                    -> "Mixed" //todo remove "else"
    }
}.forEach(::println)

fun printArray(array: PhpArray) = println(array.toString())

fun getArray(hash: PhpArray, key: String, value: String): PhpArray {
    hash.put(key.mixed, value.mixed)
    return hash
}
