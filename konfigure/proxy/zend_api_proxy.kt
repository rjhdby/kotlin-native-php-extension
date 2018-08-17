package php.extension.proxy

import kotlinx.cinterop.*
import php.*
import php.extension.share.*

typealias PhpMixed = CPointer<zval>

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
val PhpMixed.array get() = PhpArray(this)

fun PhpMixed.asString() = when (__zp_get_arg_type(this).toInt()) {
    IS_STRING       -> """"$string""""
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


fun String.toZendString(): CPointer<zend_string> = zend_string_init(this, this.length.toLong(), 0)!!

class PhpArray(array: PhpMixed) : MutableMap<PhpMixed, PhpMixed> {
    override val size: Int = 0
    override val entries: MutableSet<MutableMap.MutableEntry<PhpMixed, PhpMixed>> = mutableSetOf()
    override val keys: MutableSet<PhpMixed> = mutableSetOf()
    override val values: MutableCollection<PhpMixed> = mutableListOf()

    init {
        val hash = __zp_zval_to_hashTable(array)
        __zp_hash_reset(hash)
        (1..hash!!.pointed.nNumUsed).forEach {
            val entry: PhpMixed? = __zp_hash_get_data(hash)
            val key: PhpMixed? = __zp_hash_key(hash)

            if (entry != null && key != null) {
                put(key, entry)
            }
            __zp_hash_forward(hash)
        }
    }

    override fun clear() {
        entries.clear()
        keys.clear()
        values.clear()
    }

    override fun put(key: PhpMixed, value: PhpMixed): PhpMixed? {
        val old = get(key)
        putAll(mapOf(key to value))
        return old
    }

    override fun putAll(from: Map<out PhpMixed, PhpMixed>) {
        entries.addAll(from.entries.map { it as MutableMap.MutableEntry })
        keys.addAll(from.keys)
        values.addAll(from.values)
    }

    override fun remove(key: PhpMixed): PhpMixed? {
        val old = get(key) ?: return null
        keys.remove(key)
        entries.remove(entries.find { it.key == key })
        values.remove(old)
        return old
    }

    override fun containsKey(key: PhpMixed): Boolean {
        return keys.contains(key)
    }

    override fun containsValue(value: PhpMixed): Boolean {
        return values.contains(value)
    }

    override fun get(key: PhpMixed): PhpMixed? {
        return entries.find { it.key == key }?.value
    }

    override fun isEmpty(): Boolean {
        return keys.isEmpty()
    }

    override fun toString(): String = "[" + entries.joinToString(", ") {
        "${it.key.asString()} => ${it.value.asString()}"
    } + "]"
}
