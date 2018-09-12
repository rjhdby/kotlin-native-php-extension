package php.extension.generator

import php.extension.dsl.*
import php.extension.share.*

class KtConstGenerator : FileGenerator {
    override val fileName = "extension_constants_generated.kt"

    override fun generate(ext: Extension): String = ext.constants
            .joinIndent {
                "const val {name} = {value}".fill(
                        "name" to it.name,
                        "value" to it.getValue()
                )
            }
}
