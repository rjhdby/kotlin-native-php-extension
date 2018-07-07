import php.extension.dsl.*

val dsl = extension("example", "0.1") {
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
}

fun main(args: Array<String>) = dsl.make()
