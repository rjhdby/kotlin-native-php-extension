package php.extension.generator

import php.extension.dsl.*

class KtConstGenerator : FileGenerator {
    override val fileName = "extension_constants_generated.kt"

    override fun generate(ext: Extension): String = ext.constants
            .joinIndent() {
                ktConstEntry.fill(
                        "name" to it.name,
                        "value" to it.getValue()
                )
            }
}

const val ktConstEntry = "const val {name} = {value}"