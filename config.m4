PHP_ARG_ENABLE(hello, whether to enable hello support,[ --enable-hello   Enable hello support])

if test "$PHP_HELLO" != "no"; then
    PHP_ADD_INCLUDE(/root/simpleExtension)
    PHP_ADD_LIBRARY_WITH_PATH(hellokt, /root/simpleExtension, HELLO_SHARED_LIBADD)
    PHP_SUBST(HELLO_SHARED_LIBADD)
    PHP_NEW_EXTENSION(hello, hello.c, $ext_shared)
fi