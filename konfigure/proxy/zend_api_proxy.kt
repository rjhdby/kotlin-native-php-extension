package php.extension.proxy

import kotlinx.cinterop.*
import php.*
import php.extension.share.*

typealias PhpMixed = CPointer<zval>
//typealias PhpArray = CPointer<HashTable>

fun getIniString(name: String): String = iniMapping[name]?.invoke()?.toKString() ?: ""

fun PhpMixed.getType() = when (__zp_get_arg_type(this).toInt()) {
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

fun PhpMixed.getString() = __zp_zval_to_string(this)?.toKString() ?: ""
fun PhpMixed.getDouble() = __zp_zval_to_double(this)
fun PhpMixed.getLong() = __zp_zval_to_long(this)
fun PhpMixed.getBool() = __zp_zval_to_bool(this) == 1
fun PhpMixed.getArray() = PhpArray(this)

fun zval.getString() = __zp_zval_to_string(this.ptr)?.toKString() ?: ""
fun zval.getDouble() = __zp_zval_to_double(this.ptr)
fun zval.getLong() = __zp_zval_to_long(this.ptr)
fun zval.getBool() = __zp_zval_to_bool(this.ptr) == 1
fun zval.getArray() = PhpArray(this.ptr)

fun String.toZendString(): CPointer<zend_string> = zend_string_init(this, this.length.toLong(), 0)!!

class PhpArray(array: PhpMixed) : MutableMap<PhpMixed, PhpMixed> {
    override val size: Int = 0
    override val entries: MutableSet<MutableMap.MutableEntry<PhpMixed, PhpMixed>> = mutableSetOf()
    override val keys: MutableSet<PhpMixed> = mutableSetOf()
    override val values: MutableCollection<PhpMixed> = mutableListOf()

    init {
        val hash = __zp_zval_to_hashTable(array)
//        var intKey: Long = 0
//        var stringKey: CPointer<zend_string> = "".toZendString()

//        var pos: CValuesRef<HashPositionVar>? = null

        __zp_hash_reset(hash)
//        zend_hash_internal_pointer_reset_ex(hash, pos)
        (1..hash!!.pointed.nNumUsed).forEach {
            val entry: PhpMixed? = __zp_hash_get_data(hash)
//            val entry: PhpMixed? = zend_hash_get_current_data_ex(hash, pos)
            var key = memScoped {
                val buf = alloc<zval>()
                __zp_hash_key(hash, buf.ptr)
                buf.ptr
            }
//            var key: PhpMixed = NativePlacement.alloc<PhpMixed>()

            if (entry != null) {
                put(key, entry)
            }
//            val res: Int = zend_hash_get_current_key_ex(hash, cValuesOf(stringKey), intKey.toCPointer(), pos)
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
}
