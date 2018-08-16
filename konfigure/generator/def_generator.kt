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

/* todo
static inline zval* __zp_zend_long_to_zval(zval *z_value, zend_string *string){
    zval* out;
    ZVAL_LONG(out, string);
    return out;
}
*/

static inline zval* __zp_hash_get_data(HashTable* ht){
    return zend_hash_get_current_data_ex(ht, &(ht)->nInternalPointer);
}
static inline void __zp_hash_reset(HashTable* ht){
    zend_hash_internal_pointer_reset_ex(ht, &(ht)->nInternalPointer);
}

static inline void __zp_hash_key(HashTable* ht, zval* key){
    zend_hash_get_current_key_zval_ex(ht, key, &(ht)->nInternalPointer);
}

{iniHelpers}
"""

const val iniHelper = """
static inline char* zend_helper_get_ini_{niceIni}() {
    return {macro}("{ini}");
}
"""