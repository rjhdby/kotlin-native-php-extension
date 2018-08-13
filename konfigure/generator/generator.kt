package php.extension.generator

import platform.posix.*
import php.extension.dsl.*

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

fun String.fill(vararg map: Pair<String, String>): String {
    var out = this
    map.forEach { out = out.replace("{${it.first}}", it.second) }
    return out
}

fun ArrayList<T>.joinIndent(indent: Int = 0, transform: (T) -> CharSequence = null) = this.joinToString(
        separator = "\n",
        prefix = "    ".repeat(indent),
        transform = { transform() }
)