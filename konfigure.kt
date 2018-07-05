import php.extension.dsl.*

fun main(args: Array<String>) {
    extension("test", "0.1") {
        function("hello") {
            arg(ArgumentType.STRING, "name")
            returnType = ArgumentType.STRING
        }
        function("helloWorld")
        function("multiple2") {
            arg(ArgumentType.DOUBLE, "number")
            returnType = ArgumentType.DOUBLE
        }
    }.make()
}
