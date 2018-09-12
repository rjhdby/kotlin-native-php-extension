package php.extension.generator

import php.extension.dsl.*
import php.extension.share.*

class KtClassGenerator(val classEntry: PhpClass) : FileGenerator {
    override val fileName = "kt_${classEntry.name}_generated.kt"

    override fun generate(ext: Extension): String = """
package {package}

import php.*

{constants}

fun new{className}() = zend_helper_new_{className}()!!
""".fill(
            "package" to classEntry.name.toLowerCase(),
            "constants" to constants(classEntry.constants),
            "className" to classEntry.name
    )

    private fun constants(constants: ArrayList<Constant>) = constants
            .joinIndent {
                "const val {name} = {value}".fill(
                        "name" to it.name,
                        "value" to it.getValue()
                )
            }
}
