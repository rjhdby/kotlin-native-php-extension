#include "php.h"
#include "hellokt_api.h"

#define PHP_MY_EXTENSION_VERSION "1.0"
#define PHP_MY_EXTENSION_EXTNAME "hello"

PHP_FUNCTION(hello);

static zend_function_entry hello_functions[] = {
        PHP_FE(hello, NULL)
};

zend_module_entry hello_module_entry = {
#if ZEND_MODULE_API_NO >= 20010901
        STANDARD_MODULE_HEADER,
#endif
        PHP_MY_EXTENSION_EXTNAME,
        hello_functions,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
#if ZEND_MODULE_API_NO >= 20010901
        PHP_MY_EXTENSION_VERSION,
#endif
        STANDARD_MODULE_PROPERTIES
};

ZEND_GET_MODULE(hello)

void hello_print(char * name);
void hello_print(char * name) {
    hellokt_symbols()->kotlin.root.kt_print(name);
}

PHP_FUNCTION(hello) {
        char * name;
        size_t name_len;
        if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "s", &name, &name_len) == FAILURE) {
            RETURN_NULL();
        }
//        hellokt_kref_hello_kt_HelloKt helloKt = { 0 };

//        if(!helloKt.pinned){
//                helloKt = hellokt_symbols()->kotlin.root.hello.kt.HelloKt.HelloKt();
//        }

//        hellokt_symbols()->kotlin.root.hello.kt.HelloKt.kt_print(helloKt, name);

        hello_print(name);

        efree(name);
};