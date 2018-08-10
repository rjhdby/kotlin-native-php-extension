package zend.api.proxy

import kotlinx.cinterop.*
import php.*
import php.extension.dsl.*

typealias Mixed = CPointer<zval>

fun getIniString(name: String): String = iniMapping[name]?.invoke()?.toKString() ?: ""

fun getMixedType(zval: Mixed) = when (zend_helper_get_arg_type(zval).toInt()) {
    IS_LONG -> ArgumentType.PHP_LONG
    IS_DOUBLE -> ArgumentType.PHP_DOUBLE
    IS_STRING -> ArgumentType.PHP_STRING
    IS_NULL, IS_UNDEF -> ArgumentType.PHP_NULL
    IS_FALSE, IS_TRUE, _IS_BOOL -> ArgumentType.PHP_BOOL
    IS_ARRAY -> ArgumentType.PHP_MIXED//todo
    IS_OBJECT -> ArgumentType.PHP_MIXED//todo
    IS_RESOURCE -> ArgumentType.PHP_MIXED//todo
    IS_REFERENCE -> ArgumentType.PHP_MIXED//todo
    IS_CONSTANT_AST -> ArgumentType.PHP_MIXED//todo
    IS_CALLABLE -> ArgumentType.PHP_MIXED//todo
    IS_ITERABLE -> ArgumentType.PHP_MIXED//todo
    IS_VOID -> ArgumentType.PHP_MIXED//todo
    else -> ArgumentType.PHP_MIXED
}