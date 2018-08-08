package php.extension.generator

import platform.posix.*
import php.extension.dsl.*

//TODO work with zval (array, mixed, resource, object), zend_bool, zend_class_entry, zend_fcall_info
const val m4File = "config.m4"
const val cFile = "extension.c"
const val constantsFile = "extension_constants_generated.kt"

class Generator(ext: Extension) {
    val name = ext.name
    val version = ext.version
    val constants = ext.constants
    val ini = ext.ini
    val functions = ext.functions

    fun generate() {
        write(m4File, m4Block())
        write(constantsFile, ktConstantsBlock())
        write(cFile, cFileBlock())
    }

    private fun write(fileName: String, vararg blocks: String) {
        val file = fopen(fileName, "wt")
        try {
            blocks.forEach { fputs(it, file) }
        } finally {
            fclose(file)
        }
    }

    private fun cFileBlock() = cFileTemplate.fill(
            "version" to version,
            "extName" to name,
            "iniEntries" to ini.joinToString("\n") { iniEntry(it) },
            "argInfoBlock" to argInfoBlock(),
            "zendFunctionEntries" to functions.joinToString("\n    ") { functionEntry.fill("name" to it.name) },//TODO NULL to argInfo
            "constants" to constantsBlock(),
            "funcDefinitionBlock" to functions.joinToString("\n") { funcDefinition(it) }
    )

    private fun m4Block() = m4FileTemplate.fill(
            "name" to name,
            "upperCaseName" to name.toUpperCase()
    )

    private fun ktConstantsBlock() = constants
            .joinToString("\n") {
                ktConstEntry.fill(
                        "name" to it.name,
                        "value" to it.getValue()
                )
            }

    private fun iniEntry(ini: Ini) = cIniEntry.fill(
            "name" to ini.name,
            "default" to ini.default
    )

    private fun argInfoBlock() = "\n"  //TODO

    private fun funcDefinition(func: Function) = functionDefinition.fill(
            "name" to func.name,
            "vars" to func.arguments.joinToString("\n    ") { Tmpl.varDeclaration(it.type, it.name) },
            "argsParser" to argsParser(func.arguments),
            "return" to Tmpl.functionReturn(func.returnType, callString(func))
    )

    private fun argsParser(args: List<Argument>) = when {
        args.size > 0 -> argsParser.fill(
                "args" to argsString(args),
                "vars" to args.joinToString(", ") { Tmpl.parserArgument(it.type, it.name) }
        )
        else -> ""
    }

    private fun argsString(args: List<Argument>) = args.joinToString("") {
        (if (it.firstOptional) "|" else "") + it.type.code
    }

    private fun callString(func: Function) = kotlinFuncCall.fill(
            "name" to func.name,
            "args" to callArguments(func.arguments)
    )

    private fun callArguments(args: List<Argument>) = args.joinToString(", ") {
        when (it.type) {
            ArgumentType.NULL -> ""
            else -> "${it.name}"
        }
    }

    private fun constantsBlock() = constants
            .filterNot { it.type == ArgumentType.NULL }
            .joinToString("\n    ") {
                cConstEntry.fill(
                        "type" to Tmpl.constantTypeDefinition(it),
                        "name" to it.name,
                        "value" to it.getValue()
                )
            }
}