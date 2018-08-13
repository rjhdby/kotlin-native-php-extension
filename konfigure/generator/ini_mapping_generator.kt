package php.extension.generator

import php.extension.dsl.*

class IniMappingGenerator : FileGenerator {
    override val fileName = "extension_ini_mapper_generated.kt"

    override fun generate(ext: Extension): String = iniMapperBody.fill(
            "entries" to ext.ini.joinToString(",") { iniEntry(it) }
    )

    private fun iniEntry(ini: Ini) = iniEntry.fill(
            "ini" to ini.name,
            "niceIni" to ini.name.replace('.', '_')
    )
}

const val iniMapperBody = """package php.extension.proxy

import php.*

val iniMapping = mapOf({entries})
"""

const val iniEntry = """"{ini}" to {zend_helper_get_ini_{niceIni}()}"""