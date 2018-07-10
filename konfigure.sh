#!/bin/sh
KOTLIN_HOME=/root/kotlin-native-linux-0.7.1/bin
PHP_HOME=/opt/rh/rh-php71/root/usr

PHP_BIN=${PHP_HOME}/bin
PHP_LIB=${PHP_HOME}/include/php

GENERATOR_LIBS="./generator/php_constants.kt ./generator/dsl.kt ./generator/generator.kt"
MAKE_LIBS="./generator/php_constants.kt"

konfigure(){
    ${KOTLIN_HOME}/kotlinc ./konfigure.kt ${GENERATOR_LIBS} -o konfigure
    NAME=`./konfigure.kexe`
}

interop(){
    ${KOTLIN_HOME}/cinterop -def konfigure.def -o ${NAME}_int \
           -copt -I${PHP_LIB} \
           -copt -I${PHP_LIB}/main \
           -copt -I${PHP_LIB}/Zend \
           -copt -I${PHP_LIB}/TSRM
}

compile(){
    SOURCES=`ls ./*.kt`
    SOURCES=`echo ${SOURCES} | sed 's/.\/konfigure.kt//g'`
    ${KOTLIN_HOME}/kotlinc ${SOURCES} ${MAKE_LIBS} -o ${NAME}_kt -opt -produce static
# TODO interop "-l ${NAME}_int.klib"
}

phpize(){
    ${PHP_BIN}/phpize
}

configure(){
    ./configure  --with-php-config=${PHP_BIN}/php-config
}

# interop # TODO work with zval (array, mixed, resource, object), zend_class_entry, zend_fcall_info
konfigure

if [ ! "$1" = "kt" ]
then
    compile
    phpize
    configure
    make
    echo "n" | make test -s
    echo "\n"
fi
