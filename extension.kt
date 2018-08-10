import php.extension.dsl.*

val dsl = extension("example", "0.1") {
    ini("example.count","10")
    ini("example.name","example")

    constant("EXAMPLE_WORLD", "World")
    constant("EXAMPLE_LONG", 10L)

    constant("HELLO_EN", "Hello")
    constant("HELLO_ES", "Hola")
    constant("HELLO_RU", "Привет")

    constant("OK_HELLO", true)

    function("hello", ArgumentType.PHP_STRING) {
        arg(ArgumentType.PHP_STRING, "name")
        arg(ArgumentType.PHP_STRING, "lang", true)
    }

    function("helloWorld", ArgumentType.PHP_BOOL)

    function("multiple2", ArgumentType.PHP_DOUBLE) {
        arg(ArgumentType.PHP_DOUBLE, "number")
    }

    function("multiple2long", ArgumentType.PHP_LONG) {
        arg(ArgumentType.PHP_LONG, "number")
    }

    function("helloOrNotHello") {
        arg(ArgumentType.PHP_BOOL, "hello")
    }

    function("iniValueFor", ArgumentType.PHP_STRING){
        arg(ArgumentType.PHP_STRING, "name")
    }

    function("printMixedType"){
        arg(ArgumentType.PHP_MIXED, "value")
    }
}

fun main(args: Array<String>) = dsl.make()
