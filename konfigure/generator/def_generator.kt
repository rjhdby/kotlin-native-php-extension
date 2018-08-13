package php.extension.generator

import php.extension.dsl.*

class DefGenerator : FileGenerator {
    override val fileName = "php.def"

    override fun generate(ext: Extension): String = defFileTemplate.fill(
            "iniHelpers" to ext.ini.joinToString("\n") {
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
    return zval_get_type(z_value);
}

{iniHelpers}
"""

const val iniHelper = """
static inline char* zend_helper_get_ini_{niceIni}() {
    return {macro}("{ini}");
}
"""