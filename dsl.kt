package php.extension.dsl

import php.extension.generator.*

fun extension(name: String, version: String, body: Extension.() -> Unit = {}): Extension {
    val ext = Extension(name, version)
    ext.body()
    return ext
}

class Extension(val name: String, val version: String) {
    val functions = ArrayList<Function>()

    fun function(name: String, body: Function.() -> Unit = {}) {
        val function = Function(name)
        function.body()
        functions.add(function)
    }

    fun make() {
        print(Generator.generate(this))
    }
}

class Function(val name: String) {
    val arguments = ArrayList<Argument>()
    var returnType = ArgumentType.NULL

    fun arg(type: ArgumentType, name: String, body: Argument.() -> Unit = {}) {
        val arg = Argument(type, name)
        arg.body()
        arguments.add(arg)
    }
}

class Argument(val type: ArgumentType, val name: String)

enum class ArgumentType(val code: String) {
    STRING("s"), LONG("l"), DOUBLE("d"), NULL("")
}