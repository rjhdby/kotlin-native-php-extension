package php.extension.generator

import platform.posix.*
import php.extension.dsl.*

//TODO work with zval (array, mixed, resource, object), zend_bool, zend_class_entry, zend_fcall_info
//TODO REGISTER_LONG_CONSTANT

object Generator {
    lateinit var ext: Extension;

    fun generate(ext: Extension): String {
        this.ext = ext;
        generateM4()
        generateC()
        return ext.name
    }

    private fun generateM4() {
        val file = fopen("config.m4", "wt")
        try {
            fputs(m4Block(), file)
        } finally {
            fclose(file)
        }
    }

    private fun m4Block() = """
PHP_ARG_ENABLE(${ext.name}, whether to enable ${ext.name} support,[ --enable-${ext.name}   Enable hello support])

if test "${'$'}PHP_${ext.name.toUpperCase()}" != "no"; then
    PHP_ADD_INCLUDE(.)
    PHP_ADD_LIBRARY_WITH_PATH(${ext.name}_kt, ., ${ext.name.toUpperCase()}_SHARED_LIBADD)
    PHP_SUBST(${ext.name.toUpperCase()}_SHARED_LIBADD)
    PHP_NEW_EXTENSION(${ext.name}, ${ext.name}.c, ${'$'}ext_shared)
fi
"""

    private fun generateC() {
        val file = fopen("${ext.name}.c", "wt")
        try {
            fputs(includeBlock(), file)
            fputs(argInfoBlock(), file)
            fputs(funcDeclarationBlock(), file)
            fputs(funcEntryBlock(), file)
            fputs(moduleEntryBlock(), file)
            fputs(funcDefinitionBlock(), file)
        } finally {
            fclose(file)
        }
    }

    private fun includeBlock() = """//Autogenerated from konfigure.kt
#include "php.h"
#include "${ext.name}_kt_api.h"
"""

    private fun argInfoBlock() = "\n"  //TODO

    private fun funcDeclarationBlock() = ext.functions
            .map { "PHP_FUNCTION(${it.name});" }
            .joinToString("\n")

    private fun funcEntryBlock() = """
static zend_function_entry ${ext.name}_functions[] = {
    ${funcEntries()}
    {NULL,NULL,NULL}
};
"""

    private fun funcEntries() = ext.functions
            .map { "PHP_FE(${it.name}, NULL)" }   //TODO NULL to argInfo
            .joinToString("\n    ")

    private fun moduleEntryBlock() = """
zend_module_entry ${ext.name}_module_entry = {
#if ZEND_MODULE_API_NO >= 20010901
        STANDARD_MODULE_HEADER,
#endif
        "${ext.name}",
        ${ext.name}_functions,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
#if ZEND_MODULE_API_NO >= 20010901
        "${ext.version}",
#endif
        STANDARD_MODULE_PROPERTIES
};
ZEND_GET_MODULE(${ext.name})
"""

    private fun funcDefinitionBlock() = ext.functions
            .map { funcDefinition(it) }
            .joinToString("\n")

    private fun funcDefinition(func: Function) = """
PHP_FUNCTION(${func.name}){
    ${argsVarsDeclaration(func.arguments)}
    ${parserLineIfNeeded(func.arguments)}
    ${returnDeclaration(func)}
}
"""

    private fun argsVarsDeclaration(args: List<Argument>) = args.map {
        when (it.type) {
            ArgumentType.LONG -> "long ${it.name};"
            ArgumentType.DOUBLE -> "double ${it.name};"
            ArgumentType.STRING -> "char *${it.name};\n    size_t ${it.name}_len;"
            ArgumentType.NULL -> ""
        }
    }.joinToString("\n    ")

    fun parserLineIfNeeded(args: List<Argument>) = if (args.size != 0) """
if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "${argsString(args)}", ${parseArguments(args)}) != SUCCESS) {
    return;
}
""" else ""

    fun argsString(args: List<Argument>) = args.map { it.type.code }.joinToString("")

    fun parseArguments(args: List<Argument>) = args.map {
        when (it.type) {
            ArgumentType.LONG, ArgumentType.DOUBLE -> "&${it.name}"
            ArgumentType.STRING -> "&${it.name}, &${it.name}_len"
            ArgumentType.NULL -> ""
        }
    }.joinToString(", ")

    private fun returnDeclaration(func: Function) = when (func.returnType) {
        ArgumentType.LONG -> "RETURN_LONG(${callString(func)});"
        ArgumentType.DOUBLE -> "RETURN_DOUBLE(${callString(func)});"
        ArgumentType.STRING -> "RETURN_STRING(${callString(func)});"
        ArgumentType.NULL -> "${callString(func)};\n    RETURN_NULL()"
    }

    fun callString(func: Function) = "${ext.name}_kt_symbols()->kotlin.root.${func.name}(${callArguments(func.arguments)})"

    fun callArguments(args: List<Argument>) = args.map {
        when (it.type) {
            ArgumentType.NULL -> ""
            else -> "${it.name}"
        }
    }.joinToString(", ")
}