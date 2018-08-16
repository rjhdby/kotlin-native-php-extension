package php.extension.share

enum class ArgumentType {
    PHP_STRING,
    PHP_LONG,
    PHP_DOUBLE,
    PHP_BOOL,
    PHP_NULL,
    PHP_STRICT_LONG,
    PHP_MIXED,
    PHP_ARRAY;

    fun isNull() = this == PHP_NULL
}

/*
Expecting Type	Old API	New API
Array	    a	Z_PARAM_ARRAY(zval *)
Array	    h	Z_PARAM_ARRAY_HT(HashTable *)
Array or object	A	Z_PARAM_ARRAY_OR_OBJECT(zval * )
Array or object	H	Z_PARAM_ARRAY_OR_OBJECT_HT(HashTable *)
+Boolean	b	Z_PARAM_BOOL(zend_bool)
Callable	f	Z_PARAM_FUNC(zend_fcall_info, zend_fcall_info_cache)
+Float	d	Z_PARAM_DOUBLE(double)
+Integer	l	Z_PARAM_LONG(zend_long)
+Integer	L	Z_PARAM_STRICT_LONG(zend_long)
+Mixed	z	Z_PARAM_ZVAL(zval *)
Mixed	N/a	Z_PARAM_ZVAL_DEREF(zval *) †
Object	o	Z_PARAM_OBJECT(zval *)
Object	O	Z_PARAM_OBJECT_OF_CLASS(zval *, zend_class_entry *)
Resource	r	Z_PARAM_RESOURCE(zval *)
+String	s	Z_PARAM_STRING(char *, size_t)
-String	S	Z_PARAM_STR(zend_string *)
String (class name)	C	Z_PARAM_CLASS(zend_class_entry *)
?String (file path)	p	Z_PARAM_PATH(char *, size_t)
-String (file path)	P	Z_PARAM_PATH_STR(zend_string *)

*/
