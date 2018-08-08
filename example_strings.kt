//stub package for php.h
import php.*
import kotlinx.cinterop.*

fun hello(name: String, lang: String?) = "${if (lang ?: "" == "") HELLO_EN else lang} $name!!!\n"

fun helloWorld(): Boolean {
    php_printf("Hello %s!!!\n", EXAMPLE_WORLD)   //internal PHP function from "import php.*"
    return true;
}

fun helloOrNotHello(hello: Boolean) = println(if (hello) "Hello!" else "Nop!")

//fun getStringIni(name:String) = php_helper_ini_string(name.cstr)?.toKString()?:"WARRRRGGGGHHHHH"
