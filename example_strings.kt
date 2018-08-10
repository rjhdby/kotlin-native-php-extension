//stub package for php.h
import php.*
//import kotlinx.cinterop.*
import zend.api.proxy.*

fun hello(name: String, lang: String?) = "${if (lang ?: "" == "") HELLO_EN else lang} $name!!!\n"

fun helloWorld(): Boolean {
    php_printf("Hello %s!!!\n", EXAMPLE_WORLD)   //internal PHP function from "import php.*"
    return true;
}

fun helloOrNotHello(hello: Boolean) = println(if (hello) "Hello!" else "Nop!")

fun iniValueFor(name:String) = getIniString(name)

fun printMixedType(value:Mixed) = print(getMixedType(value))