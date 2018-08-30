package exampleclass

import php.extension.proxy.*

fun helloWorld(obj: PhpObject) = println("Hello World!!!")

fun multipleProperty(obj: PhpObject, m: Long): PhpMixed {
    var property = obj.get("property").long
    obj.set("property", (property * m).mixed)
    return obj.get("property")
}


fun printStatic(obj: PhpObject) = println(obj.getStatic("sProperty").string)

fun printObj(thisObj: PhpObject, obj: PhpObject) = println(obj.toString())
