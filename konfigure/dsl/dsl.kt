package php.extension.dsl

import php.extension.generator.Generator
import php.extension.share.ArgumentType

fun extension(name: String, version: String, body: Extension.() -> Unit = {}): Extension {
    val ext = Extension(name, version)
    ext.body()
    return ext
}

class Extension(val name: String, val version: String) {
    val functions = ArrayList<Function>()
    val constants = ArrayList<Constant>()
    val ini = ArrayList<Ini>()
    val classes = ArrayList<PhpClass>()

    fun function(name: String, type: ArgumentType = ArgumentType.PHP_NULL, body: Function.() -> Unit = {}) {
        val function = Function(name, type)
        function.body()
        functions.add(function)
    }

    fun constant(name: String, value: Any) {
        val constant = Constant(name, value)
        constants.add(constant)
    }

    fun ini(name: String, default: String) {
        ini.add(Ini(name, default))
    }

    fun phpClass(name: String, body: PhpClass.() -> Unit = {}) {
        val phpClass = PhpClass(name)
        phpClass.body()
        classes.add(phpClass)
    }

    fun make(): String {
        Generator(this).generate()
        return name
    }
}

class Argument(val type: ArgumentType, val name: String, val isOptional: Boolean) {
    var firstOptional = false;
}

open class Constant(val name: String, value: Any?) {
    var type: ArgumentType = ArgumentType.PHP_NULL
    private var stringVal: String = ""
    private var longVal: Long = 0L
    private var doubleVal: Double = 0.0
    private var boolVal: Boolean = false

    init {
        when (value) {
            null       -> {
                type = ArgumentType.PHP_NULL
            }
            is String  -> {
                type = ArgumentType.PHP_STRING
                stringVal = value
            }
            is Long    -> {
                type = ArgumentType.PHP_LONG
                longVal = value
            }
            is Int     -> {
                type = ArgumentType.PHP_LONG
                longVal = value.toLong()
            }
            is Double  -> {
                type = ArgumentType.PHP_DOUBLE
                doubleVal = value
            }
            is Boolean -> {
                type = ArgumentType.PHP_BOOL
                boolVal = value
            }
            else       -> type = ArgumentType.PHP_NULL
        }
    }

    fun getValue() = when (type) {
        ArgumentType.PHP_STRING      -> "\"$stringVal\""
        ArgumentType.PHP_LONG        -> "$longVal"
        ArgumentType.PHP_STRICT_LONG -> "$longVal"
        ArgumentType.PHP_DOUBLE      -> "$doubleVal"
        ArgumentType.PHP_BOOL        -> if (boolVal) "1" else "0"
        ArgumentType.PHP_NULL        -> ""
        ArgumentType.PHP_MIXED       -> ""
        ArgumentType.PHP_ARRAY       -> ""
        ArgumentType.PHP_OBJECT      -> ""
    }
}

class Ini(val name: String, val default: String)  //TODO typed INI