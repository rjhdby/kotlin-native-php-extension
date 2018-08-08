import php.extension.dsl.*

val dsl = extension("example", "0.1") {
    ini("example.count","10")
    constant("EXAMPLE_WORLD", "World")
    constant("EXAMPLE_LONG", 10L)

    constant("HELLO_EN", "Hello")
    constant("HELLO_ES", "Hola")
    constant("HELLO_RU", "Привет")

    constant("OK_HELLO", true)

    function("hello", ArgumentType.STRING) {
        arg(ArgumentType.STRING, "name")
        arg(ArgumentType.STRING, "lang", true)
    }

    function("helloWorld", ArgumentType.BOOL)

    function("multiple2", ArgumentType.DOUBLE) {
        arg(ArgumentType.DOUBLE, "number")
    }

    function("multiple2long", ArgumentType.LONG) {
        arg(ArgumentType.LONG, "number")
    }

    function("helloOrNotHello") {
        arg(ArgumentType.BOOL, "hello")
    }

    function("getIntIni", ArgumentType.LONG){
        arg(ArgumentType.STRING, "name")
    }

    function("getStringIni", ArgumentType.STRING){
        arg(ArgumentType.STRING, "name")
    }
}

fun main(args: Array<String>) = dsl.make()
