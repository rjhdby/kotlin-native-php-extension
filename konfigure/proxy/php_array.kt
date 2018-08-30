package php.extension.proxy

import kotlinx.cinterop.*
import php.*
import php.extension.share.*

class PhpArray(var hash: CPointer<HashTable>) : MutableMap<PhpMixed, PhpMixed> {

    override val size: Int get() = hash.pointed.nNumUsed

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

    val stringKeys: Set<String> get() = keys.map { it.asString() }.toSet()

    override val values: MutableCollection<PhpMixed> = entries.map { it.value }.toMutableList()

    companion object {
        fun fromMixed(array: PhpMixed) = PhpArray(__zp_zval_to_hashTable(array)!!)
        fun createEmpty(size: Int = 8) = PhpArray(__zp_new_hash_table(size.toLong())!!)
    }

    override fun clear() {
        hash = __zp_clear_hash_table(hash)!!
    }

    override fun put(key: PhpMixed, value: PhpMixed): PhpMixed? {
        val old = get(key)
        when (key.type) {
            ArgumentType.PHP_LONG   -> updateIntKey(key.long, value)
            ArgumentType.PHP_STRING -> updateStringKey(key.string, value)
            else                    -> zend_error(E_WARNING, "Illegal offset type")
        }
        return old
    }

    fun put(key: String, value: PhpMixed) = updateStringKey(key, value)

    fun put(key: Long, value: PhpMixed) = updateIntKey(key, value)

    fun put(value: PhpMixed) = __zp_hash_insert(hash, value)

    override fun putAll(from: Map<out PhpMixed, PhpMixed>) = from.forEach { put(it.key, it.value) }

    fun putAll(from: Map<Long, PhpMixed>) = from.forEach { put(it.key, it.value) }

    fun putAll(from: Map<String, PhpMixed>) = from.forEach { put(it.key, it.value) }

    fun putAll(from: List<PhpMixed>) = from.forEach { put(it) }

    override fun remove(key: PhpMixed): PhpMixed? {
        val old = get(key)
        when (key.type) {
            ArgumentType.PHP_LONG   -> removeIntKey(key.long)
            ArgumentType.PHP_STRING -> removeStringKey(key.string)
            else                    -> zend_error(E_WARNING, "Illegal offset type")
        }
        return old
    }

    fun remove(key: String) = remove(key.mixed)

    fun remove(key: Long) = remove(key.mixed)

    override fun containsKey(key: PhpMixed): Boolean = when (key.type) {
        ArgumentType.PHP_LONG   -> hasIntKey(key.long)
        ArgumentType.PHP_STRING -> hasStringKey(key.string)
        else                    -> 0L
    } != 0L

    fun containsKey(key: String) = hasStringKey(key)

    fun containsKey(key: Long) = hasIntKey(key)

    override fun containsValue(value: PhpMixed): Boolean = values.contains(value)

    override fun get(key: PhpMixed): PhpMixed? = when (key.type) {
        ArgumentType.PHP_LONG   -> getIntKey(key.long)
        ArgumentType.PHP_STRING -> getStringKey(key.string)
        else                    -> {
            zend_error(E_WARNING, "Illegal offset type")
            null
        }
    }

    fun get(key: String) = getStringKey(key)

    fun get(key: Long) = getIntKey(key)

    override fun isEmpty(): Boolean = size == 0

    override fun toString(): String = "[" + entries.joinToString(", ") {
        "${prettyPrint(it.key)} => ${prettyPrint(it.value)}"
    } + "]"

    private fun updateStringKey(key: String, value: PhpMixed) = __zp_hash_update_string_key(hash, key.cstr, value)

    private fun updateIntKey(key: Long, value: PhpMixed) = __zp_hash_update_int_key(hash, key, value)

    private fun removeStringKey(key: String) = __zp_hash_remove_string_key(hash, key.cstr)

    private fun removeIntKey(key: Long) = __zp_hash_remove_int_key(hash, key)

    private fun hasStringKey(key: String) = __zp_hash_has_string_key(hash, key.cstr)

    private fun hasIntKey(key: Long) = __zp_hash_has_int_key(hash, key)

    private fun getStringKey(key: String) = __zp_hash_get_string_key(hash, key.cstr)

    private fun getIntKey(key: Long) = __zp_hash_get_int_key(hash, key)

    private fun prettyPrint(value: PhpMixed) = when (value.type) {
        ArgumentType.PHP_STRING -> """"${value.asString()}""""
        else                    -> value.asString()
    }
}