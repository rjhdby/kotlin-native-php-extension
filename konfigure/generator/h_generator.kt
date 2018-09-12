package php.extension.generator

import php.extension.dsl.*
import php.extension.share.*

class HGenerator : FileGenerator {
    lateinit var ext: Extension
    override val fileName = "extension.h"

    override fun generate(ext: Extension) = ext.classes.joinIndent() {
        """
            zend_class_entry * ${it.name}_class;
            zend_class_entry * get_${it.name}_class();
            """.trimIndent()
    }
}