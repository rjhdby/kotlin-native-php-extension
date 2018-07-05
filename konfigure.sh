#!/bin/sh
KOTLIN_HOME=/root/kotlin-native-linux-0.7.1/bin
PHP_HOME=/opt/rh/rh-php71/root/usr

PHP_BIN=$PHP_HOME/bin
PHP_LIB=$PHP_HOME/include/php

konfigure(){
    $KOTLIN_HOME/kotlinc ./konfigure.kt ./dsl.kt ./generator.kt -o konfigure
    NAME=`./konfigure.kexe`
    rm ./konfigure.kexe
}

# TODO work with zval (array, mixed, resource, object), zend_bool, zend_class_entry, zend_fcall_info

interop(){
    $KOTLIN_HOME/cinterop -def konfigure.def -o ${NAME}_int \
           -copt -I$PHP_LIB \
           -copt -I$PHP_LIB/main \
           -copt -I$PHP_LIB/Zend \
           -copt -I$PHP_LIB/TSRM
}

compile(){
    $KOTLIN_HOME/kotlinc ./$NAME.kt -o ${NAME}_kt -opt -produce static
# TODO interop "-l ${NAME}_int.klib"
}

phpize(){
    $PHP_BIN/phpize
}

configure(){
    ./configure  --with-php-config=$PHP_BIN/php-config
}

test(){
    $PHP_BIN/php -dextension=./modules/$NAME.so -r "echo hello('JoE');"
    $PHP_BIN/php -dextension=./modules/$NAME.so -r "echo helloWorld();"
    $PHP_BIN/php -dextension=./modules/$NAME.so -r "echo multiple2(10).PHP_EOL;"
}



if [ "$1" = "compile" ]
then
## interop
    konfigure
    compile
    phpize
    configure
fi

NAME=test

make

test