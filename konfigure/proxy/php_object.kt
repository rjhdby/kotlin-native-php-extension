package php.extension.proxy

import kotlinx.cinterop.*
import php.*
import php.extension.share.*

class PhpObject(val context: CPointer<zend_class_entry>, val zval: PhpMixed) {
    companion object {
        fun fromMixed(zval: PhpMixed) = PhpObject(zval.pointed!!.value.obj!!.pointed!!.ce!!, zval)
    }

    val name = __zp_zend_string_to_zval(context.pointed.name)?.string ?: "stdObj"

    fun get(name: String): PhpMixed = getObjectProperty(context, zval, name.cstr)!!
    fun set(name: String, value: PhpMixed) = setObjectProperty(context, zval, name.cstr, value)
    fun getStatic(name: String): PhpMixed = getStaticProperty(context, name.cstr)!!
    fun setStatic(name: String, value: PhpMixed) = setStaticProperty(context, name.cstr, value)

    fun call(name: String, vararg args: PhpMixed) = Unit //todo

    fun callStatic(name: String, vararg args: PhpMixed) = Unit //todo

    override fun toString() = name
}
//todo constructors