package exampleclass

import kotlinx.cinterop.*
import php.extension.proxy.*
import php.*

fun getInstance(obj: PhpObject) : PhpObject {
    val o = newExampleClass().phpObject  //PhpObject
//    print_gc_flags(o.mixed.pointed!!.value.obj!!)
//    print_gc_flags_zval(o.mixed)
    return o
}

fun multipleProperty(obj: PhpObject, m: Long): PhpMixed {
    var property = obj.get("property").long
    obj.set("property", (property * m).mixed)
    return obj.get("property")
}


fun printStatic(obj: PhpObject) = println(obj.getStatic("sProperty").string)

fun printObj(thisObj: PhpObject, obj: PhpObject) = println(obj.toString())

fun __construct(obj: PhpObject) = obj.set("property", 10L.mixed)