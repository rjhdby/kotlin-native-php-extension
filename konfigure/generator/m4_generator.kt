package php.extension.generator

import php.extension.dsl.*
import php.extension.share.*

class M4Generator : FileGenerator {
    override val fileName = "config.m4"

    override fun generate(ext: Extension): String = m4FileTemplate.fill(
            "name" to ext.name,
            "upperCaseName" to ext.name.toUpperCase()
    )
}

const val m4FileTemplate = """
PHP_ARG_ENABLE({name}, whether to enable {name} support,[ --enable-{name}   Enable {name} support])

if test "${'$'}PHP_{upperCaseName}" != "no"; then
    PHP_ADD_INCLUDE(.)
    PHP_ADD_LIBRARY_WITH_PATH({name}, ., {upperCaseName}_SHARED_LIBADD)
    PHP_SUBST({upperCaseName}_SHARED_LIBADD)
    PHP_NEW_EXTENSION({name}, extension.c, ${'$'}ext_shared)
fi
"""