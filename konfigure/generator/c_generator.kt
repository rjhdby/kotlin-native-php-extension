package php.extension.generator

import php.extension.dsl.*
import php.extension.share.*

class CGenerator : FileGenerator {
    lateinit var ext: Extension
    override val fileName = "extension.c"

    override fun generate(ext: Extension): String {
        this.ext = ext
        return cFileTemplate.fill(
                "version" to ext.version,
                "extName" to ext.name,
                "iniEntries" to ext.ini.joinIndent { iniEntry(it) },
                "argInfoBlock" to argInfoBlock(),
                "zendFunctionEntries" to ext.functions.joinIndent(1) { functionEntry.fill("name" to it.name) },//TODO NULL to argInfo
                "constants" to constantsBlock(ext.constants),
                "funcDefinitionBlock" to ext.functions.joinIndent { funcDefinition(it) }
        )
    }

    private fun iniEntry(ini: Ini) = cIniEntry.fill(
            "name" to ini.name,
            "default" to ini.default
    )

    private fun argInfoBlock() = ext.functions.joinIndent {
        argInfo.fill(
                "func" to it.name,
                "optionalsByRef" to "0", //todo
                "returnByRef" to "0", //todo
                "mandatoryArgsNum" to it.arguments.filterNot { it.isOptional }.size.toString(),
                "entries" to it.arguments.joinIndent(1) { argInfoEntry(it) }
        )
    }

    private fun argInfoEntry(arg: Argument) = argInfoEntry.fill(
            "name" to arg.name,
            "passByRef" to "0" //todo
    )

    private fun funcDefinition(func: Function) = functionDefinition.fill(
            "name" to func.name,
            "vars" to func.arguments.joinIndent(1) { varDeclaration(it.type, it.name) },
            "argsParser" to argsParser(func.arguments),
            "return" to functionReturn(func.returnType, callString(func))
    )

    private fun argsParser(args: List<Argument>) = argsParserNew.fill(
            "minArgs" to args.filterNot { it.isOptional }.size.toString(),
            "maxArgs" to args.size.toString(),
            "entries" to args.joinIndent(2) { parserArgumentType(it) }
    )

    private fun callString(func: Function) = kotlinFuncCall.fill(
            "name" to func.name,
            "args" to callArguments(func.arguments),
            "extName" to ext.name
    )

    private fun callArguments(args: List<Argument>) = args.joinToString(", ") {
        when {
            it.type.isNull()                  -> ""
            it.type == ArgumentType.PHP_ARRAY -> "${ext.name}_symbols()->kotlin.root.php.extension.proxy.hashToArray(${it.name})"
            else                              -> it.name
        }
    }

    private fun constantsBlock(constants: List<Constant>) = constants
            .filterNot { it.type.isNull() }
            .joinIndent(1) {
                cConstEntry.fill(
                        "type" to constantTypeDefinition(it),
                        "name" to it.name,
                        "value" to it.getValue()
                )
            }

    private fun varDeclaration(type: ArgumentType, name: String) = when (type) {
        ArgumentType.PHP_STRICT_LONG, ArgumentType.PHP_LONG -> "zend_long ${name};"
        ArgumentType.PHP_DOUBLE                             -> "double ${name};"
        ArgumentType.PHP_STRING                             -> charDeclaration.fill("name" to name)
        ArgumentType.PHP_BOOL                               -> "zend_bool ${name};"
        ArgumentType.PHP_NULL                               -> ""
        ArgumentType.PHP_MIXED                              -> "zval * ${name};"
        ArgumentType.PHP_ARRAY                              -> "HashTable * ${name};"
    }

    private fun constantTypeDefinition(const: Constant) = when (const.type) {
        ArgumentType.PHP_LONG   -> "REGISTER_LONG_CONSTANT"
        ArgumentType.PHP_DOUBLE -> "REGISTER_DOUBLE_CONSTANT"
        ArgumentType.PHP_STRING -> "REGISTER_STRING_CONSTANT"
        ArgumentType.PHP_BOOL   -> "REGISTER_BOOL_CONSTANT"
        else                    -> ""
        /* can't use for constants
        ArgumentType.PHP_MIXED
        ArgumentType.PHP_NULL
        ArgumentType.PHP_ARRAY    //todo
        */
    }

    private fun functionReturn(type: ArgumentType, call: String) = when (type) {
        ArgumentType.PHP_STRICT_LONG, ArgumentType.PHP_LONG -> "RETURN_LONG(${call});"
        ArgumentType.PHP_DOUBLE                             -> "RETURN_DOUBLE(${call});"
        ArgumentType.PHP_STRING                             -> "RETURN_STRING(${call});"
        ArgumentType.PHP_BOOL                               -> "RETURN_BOOL(${call});"
        ArgumentType.PHP_NULL                               -> "${call};\n    RETURN_NULL();"
        ArgumentType.PHP_MIXED                              -> "RETURN_ZVAL(${call},1,1);"
        ArgumentType.PHP_ARRAY                              -> "RETURN_ARR(${ext.name}_symbols()->kotlin.root.php.extension.proxy.arrayToHashTable(${call}));"
    }

    private fun parserArgumentType(arg: Argument): String {
        val type = when (arg.type) {
            ArgumentType.PHP_LONG        -> "Z_PARAM_LONG(${arg.name})"
            ArgumentType.PHP_STRICT_LONG -> "Z_PARAM_STRICT_LONG(${arg.name})"
            ArgumentType.PHP_DOUBLE      -> "Z_PARAM_DOUBLE(${arg.name})"
            ArgumentType.PHP_STRING      -> "Z_PARAM_STRING(${arg.name}, ${arg.name}_len)"
            ArgumentType.PHP_BOOL        -> "Z_PARAM_BOOL(${arg.name})"
            ArgumentType.PHP_MIXED       -> "Z_PARAM_ZVAL(${arg.name})"
            ArgumentType.PHP_ARRAY       -> "Z_PARAM_ARRAY_HT(${arg.name})"
            else                         -> ""
            /* can't use argument type
            ArgumentType.PHP_NULL
             */
        }

        return if (arg.firstOptional) "Z_PARAM_OPTIONAL\n    $type" else type
    }
}

const val cFileTemplate = """//Autogenerated from extension.kt
#include "php.h"
#include "{extName}_api.h"

PHP_INI_BEGIN()
{iniEntries}
PHP_INI_END()

{argInfoBlock}

PHP_MINIT_FUNCTION({extName})
{
    REGISTER_INI_ENTRIES();
    {constants}
    return SUCCESS;
}

{funcDefinitionBlock}

PHP_MSHUTDOWN_FUNCTION({extName})
{
    UNREGISTER_INI_ENTRIES();
    return SUCCESS;
}

PHP_MINFO_FUNCTION({extName}){
    DISPLAY_INI_ENTRIES();
}

static zend_function_entry {extName}_functions[] = {
    {zendFunctionEntries}
    {NULL,NULL,NULL}
};

zend_module_entry {extName}_module_entry = {
#if ZEND_MODULE_API_NO >= 20010901
        STANDARD_MODULE_HEADER,
#endif
        "{extName}",
        {extName}_functions,
        PHP_MINIT({extName}),
        PHP_MSHUTDOWN({extName}),
        NULL,
        NULL,
        PHP_MINFO({extName}),
#if ZEND_MODULE_API_NO >= 20010901
        "{version}",
#endif
        STANDARD_MODULE_PROPERTIES
};
ZEND_GET_MODULE({extName})
"""

const val cIniEntry = """PHP_INI_ENTRY("{name}", "{default}", PHP_INI_ALL, NULL)"""

const val argInfo = """
ZEND_BEGIN_ARG_INFO_EX(arginfo_{func}, {optionalsByRef}, {returnByRef}, {mandatoryArgsNum})
    {entries}
ZEND_END_ARG_INFO()
"""

const val argInfoEntry = "ZEND_ARG_INFO({passByRef}, {name})"

const val functionEntry = "PHP_FE({name}, arginfo_{name})"

const val functionDefinition = """
PHP_FUNCTION({name}){
    {vars}
    {argsParser}
    {return}
}
"""

const val kotlinFuncCall = "{extName}_symbols()->kotlin.root.{name}({args})"

const val cConstEntry = """{type}("{name}", {value}, CONST_CS|CONST_PERSISTENT);"""

/*
 * Nikolay Igotti [JB]
 * it’s likely K/N  bug, in `CreateCStringFromString`, combined with different behavior
 * of `strlen` with null arg. Pass “” for now, we will fix it.
 */
const val charDeclaration = """
    char *{name} = malloc(1);
    {name}[0] = '\0';
    size_t {name}_len=0;
"""

const val charParserArgument = "&{name}, &{name}_len"
/* ----------------------------------------------------------------------- */

const val argsParserNew = """
    ZEND_PARSE_PARAMETERS_START({minArgs}, {maxArgs})
        {entries}
    ZEND_PARSE_PARAMETERS_END();
"""
