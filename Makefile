KOTLIN_HOME=/root/kotlin-native-linux-0.7.1/bin
PHP_HOME=/opt/rh/rh-php71/root/usr
PHP_LIB_ROOT=${PHP_HOME}/include/php

PHP_BIN=${PHP_HOME}/bin

PHP_LIB_INCLUDE=-I${PHP_LIB_ROOT} \
-I${PHP_LIB_ROOT}/main \
-I${PHP_LIB_ROOT}/Zend \
-I${PHP_LIB_ROOT}/TSRM

ROOT=./konfigure

COPT_PHP_LIB_INCLUDE=-copt -I${PHP_LIB_ROOT} \
-copt -I${PHP_LIB_ROOT}/main \
-copt -I${PHP_LIB_ROOT}/Zend \
-copt -I${PHP_LIB_ROOT}/TSRM

GENERATOR=${ROOT}/generator/*.kt
ZEND_INTEROP=${ROOT}/proxy/*.kt
SHARE=${ROOT}/share/*.kt
DSL=${ROOT}/dsl/*.kt

GENERATOR_FILESET=${SHARE} ${GENERATOR} ${DSL}

OUTPUT=./phpmodule

TESTS=./tests

SOURCES=`ls ./*.kt | sed 's/.\/extension.kt//g'`

KLIB_NAME=libphp
KLIB=./${KLIB_NAME}.klib
KEXE_NAME=extension
KEXE=./${KEXE_NAME}.kexe
DEF=./php.def

NAME_HOLDER=./extension.name

ARTEFACTS=${KEXE} \
${OUTPUT}/*.h \
${OUTPUT}/*.a \
${DEF} \
./extension_constants_generated.kt \
./extension_ini_mapper_generated.kt \
${OUTPUT}/config.m4 \
${OUTPUT}/*.c \
./*.o \
./*.a

all: kotlin php

prepare:
	mkdir -p ${OUTPUT}
	cp -pR ${TESTS} ${OUTPUT}/

kotlin: prepare make_generator generate interop compile

php:
	[ -f ${OUTPUT}/config.m4 ] || exit 1
	cd ${OUTPUT};${PHP_BIN}/phpize
	cd ${OUTPUT};./configure --with-php-config=${PHP_BIN}/php-config
	cd ${OUTPUT};make

interop:
ifneq ($(KLIB),$(wildcard $(KLIB)))
	${KOTLIN_HOME}/cinterop -def ${DEF} -o ${KLIB_NAME} ${COPT_PHP_LIB_INCLUDE}
else
	@echo "Skip interop"
endif

make_generator:
ifneq ($(KEXE),$(wildcard $(KEXE)))
	${KOTLIN_HOME}/kotlinc ./${KEXE_NAME}.kt ${GENERATOR_FILESET} -o ${KEXE_NAME}
else
	@echo "Skip make_generator"
endif

generate:
	${KEXE} > ${NAME_HOLDER}
	mv ./config.m4 ${OUTPUT}/
	mv ./extension.c ${OUTPUT}/

compile:
ifneq (${OUTPUT}/lib`cat ${NAME_HOLDER}`.a,$(wildcard ${OUTPUT}/lib`cat ${NAME_HOLDER}`.a))
	${KOTLIN_HOME}/kotlinc -opt -produce static ${SOURCES} ${ZEND_INTEROP} ${SHARE} -l ${KLIB} -o `cat ${NAME_HOLDER}`
	mv ./`cat ${NAME_HOLDER}`_api.h ${OUTPUT}/
	mv ./lib`cat ${NAME_HOLDER}`.a ${OUTPUT}/
else
	@echo "Skip compiling"
endif

test:
ifeq ($(OUTPUT)/Makefile,$(wildcard $(OUTPUT)/Makefile))
	cd ${OUTPUT};echo "n" | make test -s
	@echo "n"
endif

install:
ifeq ($(OUTPUT)/Makefile,$(wildcard $(OUTPUT)/Makefile))
	cd ${OUTPUT};make install
endif

clean: clean_keep_interop
	rm ${KLIB}

clean_keep_interop:
	rm ${ARTEFACTS} || true
ifeq ($(OUTPUT)/Makefile,$(wildcard $(OUTPUT)/Makefile))
	cd ${OUTPUT};make clean || true
endif

recompile:
	${KOTLIN_HOME}/kotlinc -opt -produce static ${SOURCES} ${ZEND_INTEROP} ${SHARE} -l ${KLIB} -o `cat ${NAME_HOLDER}`
	mv ./`cat ${NAME_HOLDER}`_api.h ${OUTPUT}/
	mv ./lib`cat ${NAME_HOLDER}`.a ${OUTPUT}/

run:
	${PHP_BIN}/php -dextension=`ls ${OUTPUT}/modules/*.so` -r "${php}"