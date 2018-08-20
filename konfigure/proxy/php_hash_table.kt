package php.extension.proxy

import kotlinx.cinterop.*
import php.*
import php.extension.share.*

class PhpHashTable(var hash: CPointer<HashTable>) : MutableMap<PhpMixed, PhpMixed> {

    override val size: Int
        get() = hash.pointed.nNumUsed

    override val entries: MutableSet<MutableMap.MutableEntry<PhpMixed, PhpMixed>>
        get() {
            val out: MutableSet<MutableMap.MutableEntry<PhpMixed, PhpMixed>> = mutableSetOf()
            __zp_hash_reset(hash)
            (1..hash.pointed.nNumUsed).forEach {
                val key: PhpMixed? = __zp_hash_key(hash)
                val value: PhpMixed? = __zp_hash_get_data(hash)
                if (key != null && value != null) out.addAll(mutableMapOf(key to value).entries)
                __zp_hash_forward(hash)
            }
            return out
        }

    override val keys: MutableSet<PhpMixed> get() = entries.map { it.key }.toMutableSet()

    val stringKeys: MutableSet<String>
        get() = keys.map { it.asString() }.toMutableSet()

    override val values: MutableCollection<PhpMixed> = entries.map { it.value }.toMutableList()

    companion object {
        fun fromMixed(array: PhpMixed) = PhpHashTable(__zp_zval_to_hashTable(array)!!)

//        fun fromHashTable(array: CPointer<HashTable>) = PhpHashTable(array)

        fun createEmpty(size: Int = 8) = PhpHashTable(__zp_new_hast_table(size)!!)
    }

    val mixed: PhpMixed
        get() = __zp_hash_table_to_zval(hash)!!

    override fun clear() {
        hash = __zp_clear_hast_table(hash)!!
    }

    override fun put(key: PhpMixed, value: PhpMixed): PhpMixed? {
        val old = get(key)
        when (key.type) {
            ArgumentType.PHP_LONG   -> __zp_hash_update_int_key(hash, key.long, value)
            ArgumentType.PHP_STRING -> __zp_hash_update_string_key(hash, key.string.cstr, value)
        }
        return old
    }

    fun add(value: PhpMixed) = __zp_hash_insert(hash, value)

    override fun putAll(from: Map<out PhpMixed, PhpMixed>) = from.forEach { put(it.key, it.value) }

    override fun remove(key: PhpMixed): PhpMixed? {
        val old = get(key)
        when (key.type) {
            ArgumentType.PHP_LONG   -> __zp_hash_remove_int_key(hash, key.long)
            ArgumentType.PHP_STRING -> __zp_hash_remove_string_key(hash, key.string.cstr)
        }
        return old
    }

    override fun containsKey(key: PhpMixed): Boolean = if (
            0 == when (key.type) {
                ArgumentType.PHP_LONG   -> __zp_hash_has_int_key(hash, key.long)
                ArgumentType.PHP_STRING -> __zp_hash_has_string_key(hash, key.string.cstr)
                else                    -> 0
            }
    ) false else true

    override fun containsValue(value: PhpMixed): Boolean = values.contains(value)

    override fun get(key: PhpMixed): PhpMixed? = when (key.type) {
        ArgumentType.PHP_LONG   -> __zp_hash_get_int_key(hash, key.long)
        ArgumentType.PHP_STRING -> __zp_hash_get_string_key(hash, key.string.cstr)
        else                    -> null
    }

    override fun isEmpty(): Boolean = size == 0


    private fun prettyPrint(value: PhpMixed) = when (value.type) {
        ArgumentType.PHP_STRING -> """"${value.asString()}""""
        else                    -> value.asString()
    }

    override fun toString(): String = "[" + entries.joinToString(", ") {
        "${prettyPrint(it.key)} => ${prettyPrint(it.value)}"
    } + "]"
}