package php.extension.proxy

import kotlinx.cinterop.*
import php.*
import php.extension.share.*

typealias PhpMixed = CPointer<zval>
typealias PhpArray = CPointer<HashTable>

fun getIniString(name: String): String = iniMapping[name]?.invoke()?.toKString() ?: ""

val PhpMixed.type
    get() = when (__zp_get_arg_type(this).toInt()) {
        IS_LONG                     -> ArgumentType.PHP_LONG
        IS_DOUBLE                   -> ArgumentType.PHP_DOUBLE
        IS_STRING                   -> ArgumentType.PHP_STRING
        IS_NULL, IS_UNDEF           -> ArgumentType.PHP_NULL
        IS_FALSE, IS_TRUE, _IS_BOOL -> ArgumentType.PHP_BOOL
        IS_ARRAY                    -> ArgumentType.PHP_ARRAY
        IS_OBJECT                   -> ArgumentType.PHP_MIXED//todo
        IS_RESOURCE                 -> ArgumentType.PHP_MIXED//todo
        IS_REFERENCE                -> ArgumentType.PHP_MIXED//todo
        IS_CONSTANT_AST             -> ArgumentType.PHP_MIXED//todo
        IS_CALLABLE                 -> ArgumentType.PHP_MIXED//todo
        IS_ITERABLE                 -> ArgumentType.PHP_MIXED//todo
        IS_VOID                     -> ArgumentType.PHP_MIXED//todo
        else                        -> ArgumentType.PHP_MIXED
    }

val PhpMixed.string get() = __zp_zval_to_string(this)?.toKString() ?: ""
val PhpMixed.double get() = __zp_zval_to_double(this)
val PhpMixed.long get() = __zp_zval_to_long(this)
val PhpMixed.bool get() = __zp_zval_to_bool(this) == 1
val PhpMixed.array get() = PhpHashTable.fromMixed(this)

//todo
//fun PhpMixed.makeNull() =

fun PhpMixed.asString() = when (__zp_get_arg_type(this).toInt()) {
    IS_STRING       -> string
    IS_LONG         -> long.toString()
    IS_DOUBLE       -> double.toString()
    IS_NULL         -> "null"
    IS_UNDEF        -> "undefined"
    IS_FALSE        -> "false"
    IS_TRUE         -> "true"
    IS_ARRAY        -> array.toString()
    IS_OBJECT       -> "object"
    IS_RESOURCE     -> "resource"
    IS_REFERENCE    -> "reference"
    IS_CONSTANT_AST -> "constant"
    IS_CALLABLE     -> "callable"
    IS_ITERABLE     -> "itearble"
    IS_VOID         -> "void"
    else            -> "unknown"
}

val String.mixed get() = __zp_string_to_zval(this.cstr)!!
val Long.mixed get() = __zp_long_to_zval(this as Int)!!
val Double.mixed get() = __zp_double_to_zval(this)!!
val Boolean.mixed get() = __zp_bool_to_zval(if (this == true) 1 else 0)!!

//fun String.toZendString(): CPointer<zend_string> = zend_string_init(this, this.length.toLong(), 0)!!
