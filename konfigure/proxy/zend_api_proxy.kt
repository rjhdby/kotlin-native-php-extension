package php.extension.proxy

import kotlinx.cinterop.*
import php.*
import php.extension.share.*

typealias PhpMixed = CPointer<zval>

fun hashToArray(hash: CPointer<HashTable>) = PhpArray(hash)

fun arrayToHashTable(array: PhpArray) = array.hash

fun phpObj(context: CPointer<zend_class_entry>, obj: PhpMixed) = PhpObject(context, obj)

fun objectToZval(obj: PhpObject) = obj.zval

fun zendObject(obj: PhpObject) = obj.zval.pointed!!.value.obj!!

fun getIniString(name: String): String = iniMapping[name]?.invoke()?.toKString() ?: ""

val PhpMixed.type
    get() = when (__zp_get_arg_type(this).toInt()) {
        IS_LONG                     -> ArgumentType.PHP_LONG
        IS_DOUBLE                   -> ArgumentType.PHP_DOUBLE
        IS_STRING                   -> ArgumentType.PHP_STRING
        IS_NULL, IS_UNDEF           -> ArgumentType.PHP_NULL
        IS_FALSE, IS_TRUE, _IS_BOOL -> ArgumentType.PHP_BOOL
        IS_ARRAY                    -> ArgumentType.PHP_ARRAY
        IS_OBJECT                   -> ArgumentType.PHP_OBJECT
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
val PhpMixed.bool get() = __zp_zval_to_bool(this) == 1L
val PhpMixed.array get() = PhpArray.fromMixed(this)
val PhpMixed.phpObject get() = PhpObject.fromMixed(this)

fun createPhpNull() = __zp_null_zval()!!

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
val Long.mixed get() = __zp_long_to_zval(this)!!
val Double.mixed get() = __zp_double_to_zval(this)!!
val Boolean.mixed get() = __zp_bool_to_zval(if (this == true) 1 else 0)!!
val PhpArray.mixed get() = __zp_hash_table_to_zval(hash)!!
val PhpObject.mixed get() = objectToZval(this)
