package php.extension.generator

import php.extension.dsl.*

class DefGenerator : FileGenerator {
    override val fileName = "php.def"

    override fun generate(ext: Extension): String = defFileTemplate.fill(
            "iniHelpers" to ext.ini.joinIndent() {
                iniHelper.fill(
                        "macro" to "INI_STR",
                        "ini" to it.name,
                        "niceIni" to it.name.replace('.', '_')
                )
            }
    )
}

const val defFileTemplate = """headers = php.h

---


static inline zend_uchar __zp_get_arg_type(zval *z_value) {
    return Z_TYPE_P(z_value);
}


static inline char* __zp_zval_to_string(zval *z_value){
    return Z_STRVAL_P(z_value);
}

static inline int32_t __zp_zval_to_long(zval *z_value){
    return Z_LVAL_P(z_value);
}

static inline double __zp_zval_to_double(zval *z_value){
    return Z_DVAL_P(z_value);
}

static inline int __zp_zval_to_bool(zval *z_value){
    if(Z_TYPE_P(z_value) == IS_TRUE) {
        return 1;
    } else {
        return 0;
    }
}

static inline HashTable* __zp_zval_to_hashTable(zval *z_value){
    return Z_ARRVAL_P(z_value);
}

static inline void __zp_zend_string_to_zval(zval *z_value, zend_string *string){
    ZVAL_STR(z_value, string);
}

static inline void __zp_zend_long_to_zval(zval *z_value, zend_string *string){
    ZVAL_LONG(z_value, string);
}

static inline zval * __zp_string_to_zval(char* value){
    zval * z_value = malloc(sizeof(zval));
    zend_string * z_string;
    z_string = zend_string_init(value, strlen(value), 0);
    ZVAL_STR(z_value, z_string);
    return z_value;
}

static inline zval * __zp_long_to_zval(int value){
    zval * z_value = malloc(sizeof(zval));
    ZVAL_LONG(z_value, value);
    return z_value;
}

static inline zval * __zp_double_to_zval(double value){
    zval * z_value = malloc(sizeof(zval));
    ZVAL_DOUBLE(z_value, value);
    return z_value;
}

static inline zval * __zp_bool_to_zval(int value){
    zval * z_value = malloc(sizeof(zval));
    ZVAL_BOOL(z_value, value);
    return z_value;
}

static inline zval * __zp_hash_table_to_zval(HashTable* value){
    zval * z_value = malloc(sizeof(zval));
    ZVAL_ARR(z_value, value);
    return z_value;
}

static inline zend_string * __zp_char_to_zend_string(char * value){
    zend_string * z_string;
    return zend_string_init(value, strlen(value), 0);
}

/*
 * HashTable proxy
 */

static inline HashTable* __zp_new_hast_table(int size){
    HashTable *hash;
    ALLOC_HASHTABLE(hash);
    zend_hash_init(hash, size, NULL, NULL, 0);
    return hash;
}

static inline HashTable* __zp_clear_hast_table(HashTable* ht){
    zend_hash_destroy(ht);
    FREE_HASHTABLE(ht);
    return __zp_new_hast_table(8);
}

static inline zval* __zp_hash_key(HashTable* ht){
    zval * key = malloc(sizeof(zval));
    zend_hash_get_current_key_zval_ex(ht, key, &(ht)->nInternalPointer);
    return key;
}

static inline zval* __zp_hash_get_data(HashTable* ht){
    return zend_hash_get_current_data_ex(ht, &(ht)->nInternalPointer);
}

static inline void __zp_hash_reset(HashTable* ht){
    zend_hash_internal_pointer_reset_ex(ht, &(ht)->nInternalPointer);
}

static inline void __zp_hash_forward(HashTable* ht){
	zend_hash_move_forward_ex(ht, &(ht)->nInternalPointer);
}

static inline zval* __zp_hash_get_int_key(HashTable* ht, int32_t key){
    return zend_hash_index_find(ht, key);
}

static inline void __zp_hash_remove_int_key(HashTable* ht, int32_t key){
    zend_hash_index_del(ht, key);
}

static inline int __zp_hash_has_int_key(HashTable* ht, int32_t key){
    return zend_hash_index_exists(ht, key);
}

static inline int __zp_hash_update_int_key(HashTable* ht, int32_t key, zval* value){
    zend_hash_index_update(ht, key, value);
}

static inline int __zp_hash_insert(HashTable* ht, zval* value){
    zend_hash_next_index_insert(ht, value);
}

static inline zval* __zp_hash_get_string_key(HashTable* ht, char* key){
    return zend_hash_find(ht, __zp_char_to_zend_string(key));
}

static inline void __zp_hash_remove_string_key(HashTable* ht, char* key){
    zend_hash_del(ht, __zp_char_to_zend_string(key));
}

static inline int __zp_hash_has_string_key(HashTable* ht, char* key){
    return zend_hash_exists(ht, __zp_char_to_zend_string(key));
}

static inline int __zp_hash_update_string_key(HashTable* ht, char* key, zval* value){
    zend_hash_update(ht, __zp_char_to_zend_string(key), value);
}

{iniHelpers}
"""

const val iniHelper = """
static inline char* zend_helper_get_ini_{niceIni}() {
    return {macro}("{ini}");
}
"""