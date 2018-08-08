KOTLIN_HOME=/root/kotlin-native-linux-0.7.1/bin
PHP_HOME=/opt/rh/rh-php71/root/usr

PHP_BIN=${PHP_HOME}/bin
PHP_LIB=${PHP_HOME}/include/php

GENERATOR_LIBS=./generator/php_constants.kt \
./generator/dsl.kt \
./generator/generator.kt \
./generator/generator_templates.kt

MAKE_LIBS=./generator/php_constants.kt

OUTPUT=./phpmodule

TESTS=./tests

SOURCES=`ls ./*.kt | sed 's/.\/konfigure.kt//g'`

KLIB_NAME=libphp
KLIB=./${KLIB_NAME}.klib
KEXE=./konfigure.kexe

ARTEFACTS=${KEXE} \
./extension_constants_generated.kt \
${OUTPUT}/extension_kt_api.h \
${OUTPUT}/config.m4 \
${OUTPUT}/libextension_kt.a \
${OUTPUT}/extension.c

all: prepare kotlin php

prepare:
	mkdir -p ${OUTPUT}
	cp -pR ${TESTS} ${OUTPUT}/

kotlin: interop make_generator generate compile

php:
	cd ${OUTPUT};pwd
	cd ${OUTPUT};phpize
	cd ${OUTPUT};./configure  --with-php-config=${PHP_BIN}/php-config
	cd ${OUTPUT};make

interop:
ifneq ($(KLIB),$(wildcard $(KLIB)))
	${KOTLIN_HOME}/cinterop -def ./generator/php.def -o ${KLIB_NAME} \
-copt -I${PHP_LIB} \
-copt -I${PHP_LIB}/main \
-copt -I${PHP_LIB}/Zend \
-copt -I${PHP_LIB}/TSRM
else
	@echo "Skip interop"
endif

make_generator:
ifneq ($(KEXE),$(wildcard $(KEXE)))
	${KOTLIN_HOME}/kotlinc ./konfigure.kt ${GENERATOR_LIBS} -o konfigure
else
	@echo "Skip make_generator"
endif

generate:
ifneq (${OUTPUT}/config.m4,$(wildcard ${OUTPUT}/config.m4))
	${KEXE}
	mv ./config.m4 ${OUTPUT}/
	mv ./extension.c ${OUTPUT}/
else
	@echo "Skip generate"
endif

compile:
ifneq (${OUTPUT}/libextension_kt.a,$(wildcard ${OUTPUT}/libextension_kt.a))
	${KOTLIN_HOME}/kotlinc -opt -produce static ${SOURCES} ${MAKE_LIBS} -l ${KLIB} -o extension_kt
	mv ./extension_kt_api.h ${OUTPUT}/
	mv ./libextension_kt.a ${OUTPUT}/
else
	echo "Skip compile"
endif

test:
	cd ${OUTPUT};echo "n" | make test -s
	@echo "\n"

install:
	cd ${OUTPUT}
	make install

clean: clean_keep_interop
	rm ${KLIB}

clean_keep_interop:
	rm ${ARTEFACTS} || true
ifeq ($(OUTPUT)/Makefile,$(wildcard $(OUTPUT)/Makefile))
	cd ${OUTPUT};make clean || true
endif