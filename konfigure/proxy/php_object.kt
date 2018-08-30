package php.extension.proxy

import kotlinx.cinterop.*
import php.*
import php.extension.share.*

class PhpObject(val context: CPointer<zend_class_entry>, val obj: PhpMixed) {
    fun get(name: String): PhpMixed = getObjectProperty(context, obj, name.cstr)!!
    fun set(name: String, value: PhpMixed) = setObjectProperty(context, obj, name.cstr, value)
    fun getStatic(name: String): PhpMixed = getStaticProperty(context, name.cstr)!!
    fun setStatic(name: String, value: PhpMixed) = setStaticProperty(context, name.cstr, value)

    override fun toString() = __zp_zend_string_to_zval(context.pointed.name)?.string ?: "unknown object"
}