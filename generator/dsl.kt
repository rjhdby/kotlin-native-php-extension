package php.extension.dsl

import php.extension.generator.*

fun extension(name: String, version: String, body: Extension.() -> Unit = {}): Extension {
    val ext = Extension(name, version)
    ext.body()
    return ext
}

class Extension(val name: String, val version: String) {
    val functions = ArrayList<Function>()
    val constants = ArrayList<Constant>()

    fun function(name: String, body: Function.() -> Unit = {}) {
        val function = Function(name)
        function.body()
        functions.add(function)
    }

    fun constant(name: String, value: Any) {
        val constant = Constant(name)
        constant.setValue(value)
        constants.add(constant)
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

class Argument(val type: ArgumentType, val name: String, val optional: Boolean = false)

class Constant(val name: String) {
    var type: ArgumentType = ArgumentType.NULL
    private var stringVal: String = ""
    private var longVal: Long = 0L
    private var doubleVal: Double = 0.0

    fun setValue(value: Any) {
        when (value) {
            is String -> {
                type = ArgumentType.STRING
                stringVal = value
            }
            is Long, is Int -> {
                type = ArgumentType.LONG
                longVal = value as Long
            }
            is Double -> {
                type = ArgumentType.DOUBLE
                doubleVal = value
            }
            else -> type = ArgumentType.NULL
        }
    }

    fun getValue() = when (type) {
        ArgumentType.STRING -> "\"$stringVal\""
        ArgumentType.LONG -> "$longVal"
        ArgumentType.DOUBLE -> "$doubleVal"
        else -> ""
    }
}

enum class ArgumentType(val code: String) {
    STRING("s"), LONG("l"), DOUBLE("d"), NULL("")
}
