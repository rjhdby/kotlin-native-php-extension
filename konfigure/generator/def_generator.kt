package php.extension.generator

import php.extension.dsl.*

class DefGenerator : FileGenerator {
    override val fileName = "php.def"

    override fun generate(ext: Extension): String = defFileTemplate.fill(
            "iniHelpers" to ext.ini.joinIndent() {
//            "iniHelpers" to ext.ini.joinToString("\n") {
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

static inline zend_uchar zend_helper_get_arg_type(zval *z_value) {
    return Z_TYPE_P(z_value);
}

static inline char* zend_helper_zval_string(zval *z_value){
    return Z_STRVAL_P(z_value);
}

static inline int32_t zend_helper_zval_long(zval *z_value){
    return Z_LVAL_P(z_value);
}

static inline double zend_helper_zval_double(zval *z_value){
    return Z_DVAL_P(z_value);
}

static inline int zend_helper_zval_bool(zval *z_value){
    if(Z_TYPE_P(z_value) == IS_TRUE) {
        return 1;
    } else {
        return 0;
    }
}

{iniHelpers}
"""

const val iniHelper = """
static inline char* zend_helper_get_ini_{niceIni}() {
    return {macro}("{ini}");
}
"""