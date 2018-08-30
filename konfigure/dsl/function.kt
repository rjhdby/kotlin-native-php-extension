package php.extension.dsl

import php.extension.share.*

open class Function(val name: String, val returnType: ArgumentType) {
    val arguments = ArrayList<Argument>()
    var hasOptional = false

    fun arg(type: ArgumentType, name: String, optional: Boolean = false) {
        val arg = Argument(type, name, optional)
        if (optional && !hasOptional) {
            hasOptional = true
            arg.firstOptional = true;
        }
        arguments.add(arg)
    }
}