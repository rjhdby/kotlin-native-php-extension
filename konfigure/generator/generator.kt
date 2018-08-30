package php.extension.generator

import platform.posix.*
import php.extension.dsl.*
import php.extension.share.*

//TODO work with zval (array, mixed, resource, object), zend_bool, zend_class_entry, zend_fcall_info

interface FileGenerator {
    val fileName: String
    fun generate(ext: Extension): String
}

class Generator(val ext: Extension) {
    fun generate() {
        write(M4Generator())
        write(KtConstGenerator())
        write(CGenerator())
        write(DefGenerator())
        write(IniMappingGenerator())
    }

    private fun write(generator: FileGenerator) {
        val file = fopen(generator.fileName, "wt")
        try {
            fputs(generator.generate(ext), file)
        } finally {
            fclose(file)
        }
    }
}
