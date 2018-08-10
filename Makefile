KOTLIN_HOME=/root/kotlin-native-linux-0.7.1/bin
PHP_HOME=/opt/rh/rh-php71/root/usr

PHP_BIN=${PHP_HOME}/bin
PHP_LIB=${PHP_HOME}/include/php

GENERATOR_FILES=./generator/*.kt
ZEND_INTEROP_FILES=./zend_interop/*.kt

OUTPUT=./phpmodule

TESTS=./tests

SOURCES=`ls ./*.kt | sed 's/.\/extension.kt//g'`

LIB_NAME=extension_kt
KLIB_NAME=libphp
KLIB=./${KLIB_NAME}.klib
KEXE_NAME=extension
KEXE=./${KEXE_NAME}.kexe
DEF=./php.def

ARTEFACTS=${KEXE} \
${OUTPUT}/${LIB_NAME}_api.h \
${OUTPUT}/lib${LIB_NAME}.a \
${DEF} \
./extension_constants_generated.kt \
./extension_ini_mapper_generated.kt \
${OUTPUT}/config.m4 \
${OUTPUT}/extension.c

all: prepare kotlin php

prepare:
	mkdir -p ${OUTPUT}
	cp -pR ${TESTS} ${OUTPUT}/

kotlin: make_generator generate interop compile

php:
ifeq ($(OUTPUT),$(wildcard $(OUTPUT)))
	cd ${OUTPUT};phpize
	cd ${OUTPUT};./configure  --with-php-config=${PHP_BIN}/php-config
	cd ${OUTPUT};make
endif

interop:
ifneq ($(KLIB),$(wildcard $(KLIB)))
	${KOTLIN_HOME}/cinterop -def ${DEF} -o ${KLIB_NAME} \
-copt -I${PHP_LIB} \
-copt -I${PHP_LIB}/main \
-copt -I${PHP_LIB}/Zend \
-copt -I${PHP_LIB}/TSRM
else
	@echo "Skip interop"
endif

make_generator:
ifneq ($(KEXE),$(wildcard $(KEXE)))
	${KOTLIN_HOME}/kotlinc ./${KEXE_NAME}.kt ${GENERATOR_FILES} -o ${KEXE_NAME}
else
	@echo "Skip make_generator"
endif

generate:
ifneq (${OUTPUT}/config.m4,$(wildcard ${OUTPUT}/config.m4))
	${KEXE}
	mv ./config.m4 ${OUTPUT}/
	mv ./extension.c ${OUTPUT}/
else
	@echo "Skip generating"
endif

compile:
ifneq (${OUTPUT}/lib${LIB_NAME}.a,$(wildcard ${OUTPUT}/lib${LIB_NAME}.a))
	${KOTLIN_HOME}/kotlinc -opt -produce static ${SOURCES} ${ZEND_INTEROP_FILES} -l ${KLIB} -o ${LIB_NAME}
	mv ./${LIB_NAME}_api.h ${OUTPUT}/
	mv ./lib${LIB_NAME}.a ${OUTPUT}/
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
	cd ${OUTPUT}
	make install
endif

clean: clean_keep_interop
	rm ${KLIB}

clean_keep_interop:
	rm ${ARTEFACTS} || true
ifeq ($(OUTPUT)/Makefile,$(wildcard $(OUTPUT)/Makefile))
	cd ${OUTPUT};make clean || true
endif