//stub package for php.h
import php.*
//import kotlinx.cinterop.*
import php.extension.proxy.*
import php.extension.share.*

fun multiple2(num: Double) = num * 2

fun multiple2long(num: Long) = num * 2

fun hello(name: String, lang: String?) = "${if (lang ?: "" == "") HELLO_EN else lang} $name!!!\n"

fun helloWorld(): Boolean {
    php_printf("Hello %s!!!\n", EXAMPLE_WORLD)   //internal PHP function from "import php.*"
    return true;
}

fun helloOrNotHello(hello: Boolean) = println(if (hello) "Hello!" else "Nop!")

fun iniValueFor(name: String) = getIniString(name)

fun printMixedType(value: PhpMixed) = print(value.type)

fun printMixed(value: PhpMixed) = println(
        when (value.type) {
            ArgumentType.PHP_LONG   -> value.long
            ArgumentType.PHP_DOUBLE -> value.double
            ArgumentType.PHP_STRING -> value.string
            ArgumentType.PHP_BOOL   -> value.bool
            else                    -> "Mixed"
        }
)

fun printArray(array: PhpMixed) = println(array.asString())