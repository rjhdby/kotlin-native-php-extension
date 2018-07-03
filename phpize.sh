#!/bin/sh
export PHP_PATH=/opt/rh/rh-php71/root/usr/bin
export KOTLINC=/root/kotlin-native-linux-0.7.1/bin/konanc

erase() {
    find . ! -name 'config.m4' \
    ! -name '*.c' \
    ! -name '*.kt' \
    ! -name '*.def' \
    ! -name '*.php' \
    ! -name 'phpize.sh' \
    -type f -exec rm {} +
}

compile_kotlin() {
   $KOTLINC ./hellokt.kt -o hellokt -produce static -verbose -g
  #  $KOTLINC -opt ./hellokt.kt -o hellokt -produce dynamic
}

phpize() {
    $PHP_PATH/phpize
}

configure() {
    ./configure  --with-php-config=$PHP_PATH/php-config
}

make clean
erase
compile_kotlin
phpize
configure

#exit 0

make

$PHP_PATH/php -dextension=./modules/hello.so -r "echo hello('JoE');"
