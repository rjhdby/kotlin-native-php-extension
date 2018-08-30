package php.extension.dsl

import php.extension.share.*

class PhpClass(val name: String) {
    val constants = ArrayList<Constant>()
    val properties = ArrayList<Property>()
    val methods = ArrayList<Method>()
    val modifiers = mutableSetOf<Modifier>()

    fun method(name: String, type: ArgumentType = ArgumentType.PHP_NULL, body: Method.() -> Unit = {}) {
        val method = Method(name, type, this)
        method.body()
        methods.add(method)
    }

    fun constant(name: String, value: Any) {
        val constant = Constant(name, value)
        constants.add(constant)
    }

    fun property(name: String, value: Any, body: Property.() -> Unit = {}) {
        val property = Property(name, value)
        property.body()
        properties.add(property)
    }

    fun abstract() {
        modifiers.add(Modifier.PHP_ABSTRACT)
        modifiers.remove(Modifier.PHP_FINAL)
    }

    fun final() {
        modifiers.remove(Modifier.PHP_ABSTRACT)
        modifiers.add(Modifier.PHP_FINAL)
    }
}

class Property(name: String, value: Any) : Constant(name, value) {
    val modifiers = mutableSetOf(Modifier.PHP_PUBLIC)

    fun static() = modifiers.add(Modifier.PHP_STATIC)

    fun protected() {
        modifiers.remove(Modifier.PHP_PUBLIC)
        modifiers.add(Modifier.PHP_PROTECTED)
        modifiers.remove(Modifier.PHP_PRIVATE)
    }

    fun private() {
        modifiers.remove(Modifier.PHP_PUBLIC)
        modifiers.remove(Modifier.PHP_PROTECTED)
        modifiers.add(Modifier.PHP_PRIVATE)
    }
}

class Method(name: String, returnType: ArgumentType, val parent: PhpClass) : Function(name, returnType) {
    val modifiers = mutableSetOf(Modifier.PHP_PUBLIC)

    fun private() {
        modifiers.remove(Modifier.PHP_PUBLIC)
        modifiers.remove(Modifier.PHP_PROTECTED)
        modifiers.add(Modifier.PHP_PRIVATE)
    }

    fun protected() {
        modifiers.remove(Modifier.PHP_PUBLIC)
        modifiers.add(Modifier.PHP_PROTECTED)
        modifiers.remove(Modifier.PHP_PRIVATE)
    }

    fun static() = modifiers.add(Modifier.PHP_STATIC)

    fun abstract() {
        modifiers.add(Modifier.PHP_ABSTRACT)
        modifiers.remove(Modifier.PHP_FINAL)
        parent.abstract()
    }

    fun final() {
        modifiers.remove(Modifier.PHP_ABSTRACT)
        modifiers.add(Modifier.PHP_FINAL)
    }
}